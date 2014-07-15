package com.senz.sdk.service;

public class TelepathyPeriod {
    public final long scanMillis;
    public final long waitMillis;
    public final long GPSMillis;

    public TelepathyPeriod(long scanMillis, long waitMillis, long GPSMillis) {
        this.scanMillis = scanMillis;
        this.waitMillis = waitMillis;
        this.GPSMillis = GPSMillis;
    }
}
