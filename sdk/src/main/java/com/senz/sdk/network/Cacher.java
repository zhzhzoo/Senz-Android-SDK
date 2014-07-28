package com.senz.sdk.network;

import java.util.Collection;
import android.util.LruCache;
import com.senz.sdk.Beacon;
import com.senz.sdk.BeaconWithSenz;

public class Cacher {
    private LruCache<Beacon, BeaconWithSenz> mBeaconCache;

    public Cacher(int cacheSize) {
        mBeaconCache = new LruCache<Beacon, BeaconWithSenz>(cacheSize);
    }

    public BeaconWithSenz lookupBeacon(Beacon beacon) {
        return mBeaconCache.get(beacon);
    }

    public void addBeaconWithSenz(BeaconWithSenz bws) {
        mBeaconCache.put(bws, bws);
    }
}
