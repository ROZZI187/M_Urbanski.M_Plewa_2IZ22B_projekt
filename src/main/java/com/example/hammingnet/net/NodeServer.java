package com.example.hammingnet.net;

import java.net.ServerSocket;
import java.net.Socket;

/* Serwer węzła – nasłuchuje na porcie i przyjmuje połączenia */
public class NodeServer implements AutoCloseable {

    private final int id;
    private final Graph graph;
    private volatile boolean running = false;
    private Thread acceptThread;

    public NodeServer(int id, Graph graph) {
        if (id < 0 || id >= Graph.NODES) throw new IllegalArgumentException("node id out of range");
        this.id = id;
        this.graph = graph;
    }

    /*  startuje pętlę nasłuchującą na porcie przypisanym do węzła. */
    public synchronized void start() {
        if (running) return;
        running = true;
        acceptThread = new Thread(this::acceptLoop, "Node-" + id + "-accept");
        acceptThread.start();
    }

    private void acceptLoop() {
        try (ServerSocket ss = new ServerSocket(graph.port(id))) {
            while (running) {
                try {
                    Socket s = ss.accept();
                    s.close(); // jeszcze nic nie robimy, tylko akceptujemy
                } catch (Exception ignored) {}
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void close() {
        running = false;
        if (acceptThread != null) acceptThread.interrupt();
    }

    public int id() { return id; }
}
