package com.example.hammingnet.net;

import java.util.*;

public final class Graph {

    public static final int NODES = 8;
    public static final int BASE_PORT = 6000;

    private static final int MAX_STEPS = 64;
    private final Map<Integer, List<Integer>> adj = new HashMap<>();
    private final int[] ports = new int[NODES];

    public Graph() {
        Arrays.fill(ports, 0);
        for (int i = 0; i < NODES; i++) adj.put(i, new ArrayList<>());
        addUndirected(0,1); addUndirected(1,2); addUndirected(2,3); addUndirected(3,4);
        addUndirected(4,5); addUndirected(5,6); addUndirected(6,7); addUndirected(7,0);
        addUndirected(0,4); addUndirected(2,6);
    }

    private void addUndirected(int a, int b) {
        if (!adj.get(a).contains(b)) adj.get(a).add(b);
        if (!adj.get(b).contains(a)) adj.get(b).add(a);
    }

    /* zwraca niemodyfikowalną listę sąsiadów węzła id. */
    public List<Integer> neighbors(int id) {
        checkId(id);
        return Collections.unmodifiableList(adj.get(id));
    }

    /* zwraca port TCP dla węzła id (localhost). */
    public synchronized int port(int id) {
        checkId(id);
        int p = ports[id];
        return (p != 0) ? p : candidatePortFor(id, 0);
    }

    public synchronized void setPort(int id, int port) {
        checkId(id);
        ports[id] = port;
    }

    public synchronized void clearPort(int id) {
        checkId(id);
        ports[id] = 0;
    }

    public int candidatePortFor(int id, int step) {
        checkId(id);
        if (step < 0) step = 0;
        return BASE_PORT + id + (step * NODES);
    }

    public int maxSteps() {
        return MAX_STEPS;
    }

    /* prosta walidacja identyfikatora węzła. */
    private void checkId(int id) {
        if (id < 0 || id >= NODES) throw new IllegalArgumentException("node id out of range: " + id);
    }
}
