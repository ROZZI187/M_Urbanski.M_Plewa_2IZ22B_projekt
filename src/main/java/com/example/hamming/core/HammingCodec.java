package com.example.hamming.core;

public class HammingCodec {

    /**
     * Metoda do zakodowania wiadomości 16-bitowej
     */
    public int[] encode(short data) {
        return new int[0];
    }

    /**
     * Metoda do symulowania błędu w jednym bicie.
     */
    public int[] introduceError(int[] code, int bitPosition) {
        return code;
    }

    /**
     * Metoda do dekodowania wiadomości i korekcji ewentualnego błędu.
     */
    public short decode(int[] code) {
        return 0;
    }
}
