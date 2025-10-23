package com.example.hammingnet.net;

import java.util.*;

public final class Graph {

    public static final int NODES = 8;
    public static final int BASE_PORT = 6000;

    private final Map<Integer, List<Integer>> adj = new HashMap<>();

    public Graph() {
        for (int i = 0; i < NODES; i++) adj.put(i, new ArrayList<>());

        // przykładowy graf – pierścień + dwie przekątne (0-4 i 2-6).
        addUndirected(0,1); addUndirected(1,2); addUndirected(2,3); addUndirected(3,4);
        addUndirected(4,5); addUndirected(5,6); addUndirected(6,7); addUndirected(7,0);
        addUndirected(0,4); addUndirected(2,6);
    }

    /* dodaje krawędź nieskierowaną między węzłami a-b (bez duplikatów). */
    private void addUndirected(int a, int b) {
        if (!adj.get(a).contains(b)) adj.get(a).add(b);
        if (!adj.get(b).contains(a)) adj.get(b).add(a);
    }
}
