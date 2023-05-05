package com.baxolino.apps.floats.core;

import java.io.IOException;
import java.io.InputStream;

public class ByteDivider {

    private static final int MAXIMUM_ALLOCATION = 15;

    private static final int CHUNK_SIZE = 5;

    private final byte channel;
    private final InputStream input;

    public ByteDivider(byte channel, InputStream input) {
        this.channel = channel;
        this.input = input;
    }

    public boolean pending() throws IOException {
        return input.available() > 0;
    }

    public byte[][] divide() throws IOException {
        int available = input.available();
        int allocateSize = available / CHUNK_SIZE;
        if (available % 5 != 0)
            allocateSize++;
        allocateSize = Math.min(allocateSize, MAXIMUM_ALLOCATION);
        byte[][] chunks = new byte[allocateSize][];

        for (int i = 0; i < allocateSize; i++) {
            available = input.available();
            if (available == 0 || available == -1)
                break;

            int size = Math.min(CHUNK_SIZE, available);

            byte[] chunk = new byte[size + 1]; // +1 for port
            chunk[0] = channel;
            input.read(chunk, 1, size);

            chunks[i] = chunk;
        }
        return chunks;
    }
}
