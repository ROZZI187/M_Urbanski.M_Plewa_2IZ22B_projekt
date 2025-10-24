package com.example.hammingnet.net;

import com.example.hammingnet.core.BitVector;
import com.example.hammingnet.core.ErrorInjector;
import com.example.hammingnet.core.ErrorType;
import com.example.hammingnet.core.HammingModel;

import java.util.Objects;

public final class Supervisor {
    private final HammingModel hamming = new HammingModel();
    private final ErrorInjector injector = new ErrorInjector();
    private final NodeServer entryNode;

    public Supervisor(NodeServer entryNode) {
        this.entryNode = Objects.requireNonNull(entryNode, "entryNode");
    }

    /* jak wyżej, ale przed wysyłką wstrzykuje wskazany błąd. */
    public void sendValueWithError(int dstId, int value16, ErrorType type) {
        int v = value16 & 0xFFFF;
        BitVector frame21 = hamming.encode16to21(v);
        injector.inject(frame21, type);
        entryNode.sendFrame(dstId, frame21);
    }

    /* koduje i wysyła bez usterek. */
    public void sendValueNoErrors(int dstId, int value16) {
        int v = value16 & 0xFFFF;
        BitVector frame21 = hamming.encode16to21(v);
        entryNode.sendFrame(dstId, frame21);
    }
}
