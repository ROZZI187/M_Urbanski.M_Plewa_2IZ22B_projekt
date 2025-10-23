package com.example.hammingnet.core;

public final class BitVector {
    private final boolean[] bits;

    public BitVector(int size) {
        if (size <= 0) throw new IllegalArgumentException("size");
        this.bits = new boolean[size];
    }

    public int size() { return bits.length; }

    public boolean get(int i) { return bits[i]; }

    public void set(int i, boolean v) { bits[i] = v; }

    // ułatwia pakowanie ramek do prostych typów
    public int toInt() {
        int v = 0;
        for (int i = 0; i < bits.length; i++) if (bits[i]) v |= (1 << i);
        return v;
    }

    public static BitVector fromInt(int value, int size) {
        BitVector bv = new BitVector(size);
        for (int i = 0; i < size; i++) bv.set(i, ((value >>> i) & 1) == 1);
        return bv;
    }
}
