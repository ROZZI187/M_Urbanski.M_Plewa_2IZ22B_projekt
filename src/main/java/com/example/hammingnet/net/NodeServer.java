package com.example.hammingnet.net;

import com.example.hammingnet.core.BitVector;
import com.example.hammingnet.core.ErrorInjector;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

/* Serwer węzła z routingiem BFS i możliwością wstrzykiwania usterek wg FaultConfig. */
public class NodeServer implements AutoCloseable {

    public interface NodeEventListener {
        void onReceived(int nodeId, int srcId, int dstId, BitVector payload21);
        void onForwarded(int nodeId, int nextHopId, int srcId, int dstId, BitVector payload21);
        void onDelivered(int nodeId, int srcId, int dstId, BitVector payload21);
        void onError(int nodeId, String message, Exception ex);
    }

    private final int id;
    private final Graph graph;
    private volatile boolean running = false;
    private Thread acceptThread;
    private volatile NodeEventListener listener;

    // konfiguracja usterek + injector
    private volatile FaultConfig faultConfig = FaultConfig.disabled();
    private final ErrorInjector injector = new ErrorInjector();

    private volatile ServerSocket serverSocket;

    public NodeServer(int id, Graph graph) {
        if (id < 0 || id >= Graph.NODES) throw new IllegalArgumentException("node id out of range");
        this.id = id;
        this.graph = Objects.requireNonNull(graph, "graph");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try { this.close(); } catch (Exception ignored) {}
        }, "Node-" + id + "-shutdown"));
    }

    public void setListener(NodeEventListener l) { this.listener = l; }

    /* ustaw/zmień konfigurację usterek dla tego węzła. */
    public void setFaultConfig(FaultConfig cfg) {
        if (cfg == null) cfg = FaultConfig.disabled();
        this.faultConfig = cfg;
    }

    public synchronized void start() {
        if (running) return;
        running = true;
        acceptThread = new Thread(this::acceptLoop, "Node-" + id + "-accept");
        acceptThread.start();
    }

    private void acceptLoop() {
        try {
            bindWithRecovery();
            if (serverSocket == null) {
                fireError("bind/listen failed", new IllegalStateException("no free port for node " + id));
                running = false;
                return;
            }
            while (running) {
                try {
                    Socket s = serverSocket.accept();
                    handleClientAsync(s);
                } catch (java.net.SocketException se) {
                    break;
                } catch (Exception ex) {
                    fireError("accept failed", ex);
                }
            }
        } catch (Exception e) {
            fireError("bind/listen failed", e);
        } finally {
            safeCloseServer();
        }
    }

    private void bindWithRecovery() {
        for (int k = 0; k < graph.maxSteps() && running; k++) {
            int candidate = graph.candidatePortFor(id, k);
            try {
                ServerSocket ss = new ServerSocket();
                ss.setReuseAddress(true);
                ss.bind(new InetSocketAddress("127.0.0.1", candidate), 50);
                this.serverSocket = ss;
                graph.setPort(id, candidate);
                return;
            } catch (Exception ignored) {
                sleepQuiet(40);
            }
        }
        this.serverSocket = null;
    }

    private void handleClientAsync(Socket s) {
        new Thread(() -> handleClient(s), "Node-" + id + "-client").start();
    }

    private void handleClient(Socket s) {
        try (s; InputStream in = s.getInputStream()) {
            byte[] buf = readExactly(in, 5);
            if (buf == null) return;

            int src = buf[0] & 0xFF;
            int dst = buf[1] & 0xFF;
            int v = ((buf[2] & 0xFF)) | ((buf[3] & 0xFF) << 8) | ((buf[4] & 0xFF) << 16);
            BitVector payload = BitVector.fromInt(v, 21);

            // wstrzyknięcie usterek NA WEJŚCIU do węzła (symulacja usterek elementu).
            var cfg = faultConfig; // volatile → bezpieczny snapshot
            if (cfg != null) {
                try {
                    cfg.apply(payload, injector);
                } catch (RuntimeException drop) {
                    fireError("packet dropped at node " + id, null);
                    return;
                }
            }

            var l = listener;
            if (l != null) l.onReceived(id, src, dst, payload);

            if (dst != id) {
                int next = chooseNextHop(dst);
                if (next >= 0) {
                    byte[] outMsg = new byte[5];
                    outMsg[0] = (byte) (src & 0xFF);
                    outMsg[1] = (byte) (dst & 0xFF);
                    int val = payload.toInt();
                    outMsg[2] = (byte) (val & 0xFF);
                    outMsg[3] = (byte) ((val >>> 8) & 0xFF);
                    outMsg[4] = (byte) ((val >>> 16) & 0xFF);

                    sendRaw(next, outMsg);
                    if (l != null) l.onForwarded(id, next, src, dst, payload);
                }
            } else {
                if (l != null) l.onDelivered(id, src, dst, payload);
            }
        } catch (Exception ex) {
            fireError("handleClient failed", ex);
        }
    }

    private byte[] readExactly(InputStream in, int n) throws java.io.IOException {
        byte[] buf = new byte[n];
        int off = 0, got;
        while (off < n && (got = in.read(buf, off, n - off)) != -1) off += got;
        if (off < n) return null;
        return buf;
    }

    private void sendRaw(int toId, byte[] msg5) {
        int port = graph.port(toId);
        for (int attempt = 1; attempt <= 3; attempt++) {
            try (Socket s = new Socket("127.0.0.1", port);
                 OutputStream out = s.getOutputStream()) {
                out.write(msg5);
                out.flush();
                return;
            } catch (Exception ex) {
                if (attempt == 3) {
                    fireError("sendRaw failed toId=" + toId, ex);
                } else {
                    sleepQuiet(60);
                }
            }
        }
    }

    private int chooseNextHop(int dstId) {
        if (dstId == id) return -1;
        boolean[] vis = new boolean[Graph.NODES];
        int[] prev = new int[Graph.NODES];
        Arrays.fill(prev, -1);
        Deque<Integer> q = new ArrayDeque<>();
        q.add(id);
        vis[id] = true;
        while (!q.isEmpty()) {
            int u = q.removeFirst();
            if (u == dstId) break;
            for (int v : graph.neighbors(u)) {
                if (!vis[v]) { vis[v] = true; prev[v] = u; q.addLast(v); }
            }
        }
        if (!vis[dstId]) return -1;
        int cur = dstId, before = prev[cur];
        while (before != -1 && before != id) { cur = before; before = prev[cur]; }
        return cur;
    }

    public void sendFrame(int dstId, BitVector frame21) {
        byte[] msg = new byte[5];
        msg[0] = (byte) (id & 0xFF);
        msg[1] = (byte) (dstId & 0xFF);
        int v = frame21.toInt();
        msg[2] = (byte) (v & 0xFF);
        msg[3] = (byte) ((v >>> 8) & 0xFF);
        msg[4] = (byte) ((v >>> 16) & 0xFF);

        int next = chooseNextHop(dstId);
        if (next >= 0) sendRaw(next, msg);
    }

    public int id() { return id; }

    private void fireError(String msg, Exception ex) {
        var l = listener;
        if (l != null) l.onError(id, msg, ex);
    }

    @Override
    public synchronized void close() {
        running = false;
        safeCloseServer();
        if (acceptThread != null) {
            try { acceptThread.join(500); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
        }
    }

    private void safeCloseServer() {
        try {
            ServerSocket ss = this.serverSocket;
            if (ss != null && !ss.isClosed()) ss.close();
        } catch (Exception ignored) {}
        this.serverSocket = null;
        graph.clearPort(id);
    }

    private static void sleepQuiet(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
    }
}
