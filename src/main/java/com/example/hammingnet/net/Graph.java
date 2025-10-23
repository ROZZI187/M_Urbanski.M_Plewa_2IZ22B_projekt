package com.example.hammingnet.net;

import java.util.*;

public final class Graph {

    public static final int NODES = 8;
    public static final int BASE_PORT = 6000;

    private final Map<Integer, List<Integer>> adj = new HashMap<>();

    /* inicjalizuje strukturę sąsiedztw dla 8 węzłów */
    public Graph() {
        for (int i = 0; i < NODES; i++) {
            adj.put(i, new ArrayList<>());
        }
    }
}
