package com.example.hammingnet.net;

import com.example.hammingnet.core.BitVector;
import com.example.hammingnet.core.ErrorInjector;
import com.example.hammingnet.core.ErrorType;

/* Konfiguracja usterek dla pojedynczego węzła.
   Dla każdego typu błędu trzymamy: włączony/wyłączony oraz prawdopodobieństwo p. */
public final class FaultConfig {

    public static final class Entry {
        public boolean enabled;
        public double p; // 0..1

        public Entry() { this(false, 0.0); }
        public Entry(boolean enabled, double p) {
            this.enabled = enabled;
            this.p = p;
        }
    }

    private final Entry flip = new Entry();
    private final Entry burst2 = new Entry();
    private final Entry drop = new Entry();

    public Entry flip()   { return flip;   }
    public Entry burst2() { return burst2; }
    public Entry drop()   { return drop;   }


    /*   To jest "symulacja" zachowania – nawet jeśli element "nie toleruje" rodzaju błędu,
      i tak go wpuszczam */
    public void apply(BitVector frame21, ErrorInjector injector) {
        if (flip.enabled)   injector.injectWithProbability(frame21, ErrorType.BIT_FLIP,   clamp(flip.p));
        if (burst2.enabled) injector.injectWithProbability(frame21, ErrorType.BURST_2,    clamp(burst2.p));
        if (drop.enabled)   injector.injectWithProbability(frame21, ErrorType.DROP_PACKET, clamp(drop.p));
    }

    private double clamp(double p) {
        if (p < 0) return 0;
        if (p > 1) return 1;
        return p;
    }

    public static FaultConfig disabled() { return new FaultConfig(); }
}
