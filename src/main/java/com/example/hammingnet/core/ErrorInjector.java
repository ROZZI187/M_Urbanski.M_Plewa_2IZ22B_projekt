package com.example.hammingnet.core;

import java.util.Random;

public final class ErrorInjector {
    private final Random rnd;

    public ErrorInjector() { this.rnd = new Random(); }
    public ErrorInjector(long seed) { this.rnd = new Random(seed); }

    /* wstrzykuje błąd do ramki (modyfikacja in-place). */
    public void inject(BitVector frame, ErrorType type) {
        switch (type) {
            case BIT_FLIP -> flipOne(frame);
            case BURST_2  -> flipTwoAdjacent(frame);
            case DROP_PACKET -> dropPacket(frame);
        }
    }

    /* wariant z prawdopodobieństwem — przyda się w GUI do "ciągłego" zakłócania. */
    public void injectWithProbability(BitVector frame, ErrorType type, double p) {
        if (p <= 0) return;
        if (p >= 1 || rnd.nextDouble() < p) inject(frame, type);
    }

    private void flipOne(BitVector frame) {
        int pos = rnd.nextInt(frame.size());
        frame.set(pos, !frame.get(pos));
    }

    private void flipTwoAdjacent(BitVector frame) {
        if (frame.size() < 2) { flipOne(frame); return; }
        int pos = rnd.nextInt(frame.size() - 1);
        frame.set(pos, !frame.get(pos));
        frame.set(pos + 1, !frame.get(pos + 1));
    }

    // "zgubienie" pakietu – przyjmujemy umowę, że wszystkie bity idą na 0.
    private void dropPacket(BitVector frame) {
        for (int i = 0; i < frame.size(); i++) frame.set(i, false);
    }
}
