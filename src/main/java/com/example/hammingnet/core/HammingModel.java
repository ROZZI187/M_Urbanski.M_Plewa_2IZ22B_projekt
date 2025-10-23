package com.example.hammingnet.core;

public final class HammingModel {

    private boolean isParityPos(int i) {
        return i == 1 || i == 2 || i == 4 || i == 8 || i == 16;
    }

    public BitVector encode16to21(int data16) {
        boolean[] code = new boolean[22]; // 1..21
        int dataBit = 0;
        for (int i = 1; i <= 21 && dataBit < 16; i++) {
            if (!isParityPos(i)) {
                boolean bit = ((data16 >>> dataBit) & 1) == 1;
                code[i] = bit;
                dataBit++;
            }
        }
        for (int p = 1; p <= 16; p <<= 1) {
            boolean parity = false;
            for (int i = 1; i <= 21; i++) {
                if ((i & p) != 0) {
                    if (i != p) parity ^= code[i];
                }
            }
            code[p] = parity;
        }
        BitVector out = new BitVector(21);
        for (int i = 1; i <= 21; i++) out.set(i - 1, code[i]);
        return out;
    }

    public int computeSyndrome(BitVector frame21) {
        int syndrome = 0;
        for (int p = 1; p <= 16; p <<= 1) {
            boolean parity = false;
            for (int i = 1; i <= 21; i++) {
                boolean bit = frame21.get(i - 1);
                if ((i & p) != 0) parity ^= bit;
            }
            if (parity) syndrome |= p;
        }
        return syndrome;
    }

    /* koryguje pojedynczy błąd na pozycji wskazanej przez syndrom (1..21).
       Zwraca NOWĄ skorygowaną ramkę (oryginał bez zmian). */
    public BitVector correctSingleError(BitVector in21) {
        BitVector out = new BitVector(21);
        for (int i = 0; i < 21; i++) out.set(i, in21.get(i));
        int s = computeSyndrome(in21);
        if (s >= 1 && s <= 21) {
            out.set(s - 1, !out.get(s - 1));
        }
        return out;
    }

    /* wyodrębnia 16 bitów danych z ramki 21-bitowej (po korekcji). */
    public int extractData(BitVector frame21) {
        int value = 0;
        int dataBit = 0;
        for (int i = 1; i <= 21 && dataBit < 16; i++) {
            if (!isParityPos(i)) {
                if (frame21.get(i - 1)) value |= (1 << dataBit);
                dataBit++;
            }
        }
        return value;
    }
}
