package com.example.hammingnet.core;

import java.util.Random;

public final class ErrorInjector {
    private final Random rnd;

    public ErrorInjector() {
        this.rnd = new Random();
    }

    public ErrorInjector(long seed) {
        this.rnd = new Random(seed);
    }

    /* wstrzykuje błąd do ramki (modyfikacja in-place). */
    public void inject(BitVector frame, ErrorType type) {
        switch (type) {
            case BIT_FLIP -> flipOne(frame);
            default -> {  }
        }
    }

    // losowo odwraca pojedynczy bit.
    private void flipOne(BitVector frame) {
        int pos = rnd.nextInt(frame.size());
        frame.set(pos, !frame.get(pos));
    }
}
