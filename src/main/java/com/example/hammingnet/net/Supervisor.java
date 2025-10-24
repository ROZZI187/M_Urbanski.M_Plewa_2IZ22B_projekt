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

    /* wariant z seedem dla powtarzalności testów. */
    public Supervisor(NodeServer entryNode, long seed) {
        this.entryNode = Objects.requireNonNull(entryNode, "entryNode");
        this.injector = new ErrorInjector(seed);
        this.rnd = new Random(seed);
    }

    /* wstrzykuje błąd z prawdopodobieństwem p (0..1). */
    public void sendValueMaybeError(int dstId, int value16, ErrorType type, double p) {
        int v = value16 & 0xFFFF;
        BitVector frame21 = hamming.encode16to21(v);
        injector.injectWithProbability(frame21, type, p);
        entryNode.sendFrame(dstId, frame21);
    }

    /* wysyła losową wartość 16-bit bez usterek. */
    public void sendRandom(int dstId) {
        int value = rnd.nextInt(1 << 16);
        sendValueNoErrors(dstId, value);
    }

    public void sendValueWithError(int dstId, int value16, ErrorType type) {
        int v = value16 & 0xFFFF;
        BitVector frame21 = hamming.encode16to21(v);
        injector.inject(frame21, type);
        entryNode.sendFrame(dstId, frame21);
    }

    public void sendValueNoErrors(int dstId, int value16) {
        int v = value16 & 0xFFFF;
        BitVector frame21 = hamming.encode16to21(v);
        entryNode.sendFrame(dstId, frame21);
    }
}
