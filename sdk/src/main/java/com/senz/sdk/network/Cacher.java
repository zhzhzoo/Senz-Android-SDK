package com.senz.sdk.network;

import java.util.Collection;
import android.util.LruCache;
import com.senz.sdk.Beacon;
import com.senz.sdk.BeaconWithSenzes;

public class Cacher {
    private LruCache<Beacon, BeaconWithSenzes> mBeaconCache;

    public Cacher(int cacheSize) {
        mBeaconCache = new LruCache<Beacon, BeaconWithSenzes>(cacheSize);
    }

    public BeaconWithSenzes lookupBeacon(Beacon beacon) {
        return mBeaconCache.get(beacon);
    }

    public void addBeaconWithSenzes(BeaconWithSenzes bws) {
        mBeaconCache.put(bws, bws);
    }
}
