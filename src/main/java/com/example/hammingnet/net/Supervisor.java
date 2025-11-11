package com.example.hammingnet.net;

import com.example.hammingnet.core.BitVector;
import com.example.hammingnet.core.ErrorInjector;
import com.example.hammingnet.core.ErrorType;
import com.example.hammingnet.core.HammingModel;

import java.util.Objects;
import java.util.Random;

public final class Supervisor {
    private final HammingModel hamming = new HammingModel();
    private final ErrorInjector injector;
    private final NodeServer entryNode;
    private final Random rnd;

    public Supervisor(NodeServer entryNode) {
        this(entryNode, System.nanoTime());
    }

    public Supervisor(NodeServer entryNode, long seed) {
        this.entryNode = Objects.requireNonNull(entryNode, "entryNode");
        this.injector = new ErrorInjector(seed);
        this.rnd = new Random(seed);
    }

    /* pomocniczo – dba o zakres identyfikatora i wartości. */
    private void checkDst(int dstId) {
        if (dstId < 0 || dstId >= Graph.NODES) throw new IllegalArgumentException("dstId out of range: " + dstId);
    }

    /* koduje i wysyła bez usterek (z walidacją). */
    public boolean sendValueNoErrors(int dstId, int value16) {
        checkDst(dstId);
        int v = value16 & 0xFFFF;
        BitVector frame21 = hamming.encode16to21(v);
        entryNode.sendFrame(dstId, frame21);
        return true;
    }

    /* koduje, wstrzykuje konkretny błąd i wysyła (z walidacją). */
    public boolean sendValueWithError(int dstId, int value16, ErrorType type) {
        checkDst(dstId);
        int v = value16 & 0xFFFF;
        BitVector frame21 = hamming.encode16to21(v);
        try {
            injector.inject(frame21, type);
        } catch (RuntimeException drop) {
            return false;
        }
        entryNode.sendFrame(dstId, frame21);
        return true;
    }

    /* wstrzykuje błąd z prawdopodobieństwem p (0..1). */
    public boolean sendValueMaybeError(int dstId, int value16, ErrorType type, double p) {
        checkDst(dstId);
        if (p < 0 || p > 1) throw new IllegalArgumentException("p must be in [0,1]");
        int v = value16 & 0xFFFF;
        BitVector frame21 = hamming.encode16to21(v);
        try {
            injector.injectWithProbability(frame21, type, p);
        } catch (RuntimeException drop) {
            return false;
        }
        entryNode.sendFrame(dstId, frame21);
        return true;
    }

    /* losowa wartość 16-bit – szybki test ścieżki. */
    public boolean sendRandom(int dstId) {
        checkDst(dstId);
        int value = rnd.nextInt(1 << 16);
        return sendValueNoErrors(dstId, value);
    }
}
