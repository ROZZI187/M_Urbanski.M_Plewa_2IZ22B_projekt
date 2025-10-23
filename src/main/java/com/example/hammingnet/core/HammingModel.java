package com.example.hammingnet.core;

/*  Model symulacyjny kodu Hamminga (21,16).
   Indeksy po stronie "matematycznej" liczymy 1..21.
   W BitVector przechowujemy bity 0..20 (LSB-first). */
public final class HammingModel {

    // sprawdza, czy i to pozycja bitu parzystości (1,2,4,8,16).
    private boolean isParityPos(int i) {
        return i == 1 || i == 2 || i == 4 || i == 8 || i == 16;
    }

    /* umieszcza 16 bitów danych na pozycjach nie-parzystych (nie 1,2,4,8,16).
       Na tym etapie bity parzystości ustawiamy tymczasowo na 0 */
    public BitVector encode16to21(int data16) {
        boolean[] code = new boolean[22]; // używamy 1..21
        int dataBit = 0;
        for (int i = 1; i <= 21 && dataBit < 16; i++) {
            if (!isParityPos(i)) {
                boolean bit = ((data16 >>> dataBit) & 1) == 1;
                code[i] = bit;
                dataBit++;
            }
        }
        // parity na razie 0 (false)
        BitVector out = new BitVector(21);
        for (int i = 1; i <= 21; i++) out.set(i - 1, code[i]);
        return out;
    }
}
