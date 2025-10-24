package com.example.hammingnet.net;

import com.example.hammingnet.core.BitVector;
import com.example.hammingnet.core.HammingModel;

import java.util.Objects;

public final class Supervisor {
    private final HammingModel hamming = new HammingModel();
    private final NodeServer entryNode;

    public Supervisor(NodeServer entryNode) {
        this.entryNode = Objects.requireNonNull(entryNode, "entryNode");
    }

    /* koduje 16-bit do 21-bit (Hamming) i wysyła bez usterek. */
    public void sendValueNoErrors(int dstId, int value16) {
        int v = value16 & 0xFFFF;
        BitVector frame21 = hamming.encode16to21(v);
        entryNode.sendFrame(dstId, frame21);
    }
}
