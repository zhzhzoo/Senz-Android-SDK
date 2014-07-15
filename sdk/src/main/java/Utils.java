package com.senz.sdk;

protected class Utils {
    protected static long longFrom8Bytes(byte b7, byte b6, byte b5, byte b4, byte b3, byte b2, byte b1, byte b0) {
        long l7 = unsignedByteToLong(b7);
        long l6 = unsignedByteToLong(b6);
        long l5 = unsignedByteToLong(b5);
        long l4 = unsignedByteToLong(b4);
        long l3 = unsignedByteToLong(b3);
        long l2 = unsignedByteToLong(b2);
        long l1 = unsignedByteToLong(b1);
        long l0 = unsignedByteToLong(b0);

        return (l7 << 56) + (l6 << 48) + (l5 << 40) + (l4 << 32) + (l3 << 24) + (l2 << 16) + (l1 << 8) + l0;
    }

    protected static long unsignedByteToLong(byte b) {
        return b & 0xFFL;
    }

    protected static int intFrom2Bytes(byte b1, byte b0) {
        int i1 = unsignedByteToInt(b1);
        int i0 = unsignedByteToInt(b0);

        return (i1 << 8) + i0;
    }

    protected static long unsignedByteToInt(byte b) {
        return b & 0xFF;
    }
};
