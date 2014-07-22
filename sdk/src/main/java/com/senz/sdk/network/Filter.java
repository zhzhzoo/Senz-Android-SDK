package com.senz.sdk.network;

import android.os.SystemClock;
import java.util.Collection;
import java.util.HashMap;
import java.util.ArrayList;
import com.senz.sdk.Beacon;

public class Filter {
    HashMap<Beacon, Long> current, previous;
    long lastSwapped;
    final long expire;

    public Filter(long expire) {
        this.current = new HashMap<Beacon, Long>();
        this.previous = new HashMap<Beacon, Long>();
        this.lastSwapped = SystemClock.uptimeMillis();
        this.expire = expire;
    }

    private void swap() {
        long now = SystemClock.uptimeMillis();
        if (now - lastSwapped <= expire)
            return;
        HashMap t = previous;
        previous = current;
        current = t;
        current.clear();
        lastSwapped = now;
    }

    public ArrayList<Beacon> filterTime(Collection<Beacon> beacons) {
        ArrayList<Beacon> result = new ArrayList<Beacon>();
        Long lastVisited;
        long now = SystemClock.uptimeMillis();

        swap();
        for (Beacon beacon : beacons) {
            lastVisited = this.current.get(beacon);
            if (lastVisited != null)
                continue;
            lastVisited = this.previous.get(beacon);
            if (lastVisited != null)
                if (now - lastVisited <= expire)
                    continue;
                else
                    this.previous.remove(beacon);
            this.current.put(beacon, Long.valueOf(now));
            result.add(beacon);
        }

        return result;
    }
}
