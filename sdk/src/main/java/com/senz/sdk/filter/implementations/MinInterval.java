package com.senz.sdk.network;

import android.os.SystemClock;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import com.senz.sdk.Senz;
import com.senz.sdk.filter.FilterImplementation;

public class MinInterval implements FilterImplementation {
    HashMap<Senz, Long> current, previous;
    long lastSwapped;
    final long expire;

    public void init(Context context, Object ...extra) {
        this.current = new HashMap<Senz, Long>();
        this.previous = new HashMap<Senz, Long>();
        this.lastSwapped = SystemClock.uptimeMillis();
        if (extra.length >= 1 && extra[0] instanceof Number)
            this.expire = ((Number) extra[0]).longValue();
        else
            this.expire = TimeUnit.MINUTES.toMillis(30);
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

    public Set<Senz> filter(Set<Senz> senzes) {
        HashSet<Senz> result;
        Long lastVisited;
        long now = SystemClock.uptimeMillis();

        swap();
        for (Senz senz : senzs) {
            lastVisited = this.current.get(senz);
            if (lastVisited != null)
                continue;
            lastVisited = this.previous.get(senz);
            if (lastVisited != null)
                if (now - lastVisited <= expire)
                    continue;
                else
                    this.previous.remove(senz);
            this.current.put(senz, Long.valueOf(now));
            result.add(senz);
        }

        return result;
    }
}
