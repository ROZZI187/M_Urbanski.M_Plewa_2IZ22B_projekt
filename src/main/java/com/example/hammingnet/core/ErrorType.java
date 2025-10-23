package com.example.hammingnet.core;

public enum ErrorType {
    BIT_FLIP,   // odwrócenie pojedynczego bitu
    BURST_2,    // odwrócenie dwóch sąsiednich bitów
    DROP_PACKET // zgubienie całej ramki (symulowane)
}
