package com.senz.sdk.utils;

import java.lang.Runnable;
import java.lang.Thread;

public class Asyncfied {
    public interface Asyncfiable<T> {
        public T runAndReturn();
        public void onReturn(T t);
    }

    private Asyncfiable mAsyncfied;

    public Asyncfied(Asyncfiable a) {
        if (a == null)
            throw new NullPointerException();
        this.mAsyncfied = a;
    }

    public void run() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Asyncfied.this.mAsyncfied.onReturn(Asyncfied.this.mAsyncfied.runAndReturn());
            }
        }).start();
    }

    public static void runAsyncfiable(Asyncfiable a) {
        Asyncfied as = new Asyncfied(a);
        as.run();
    }
}
