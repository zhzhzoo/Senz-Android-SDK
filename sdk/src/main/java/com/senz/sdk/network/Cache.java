package com.senz.sdk.network;

import java.util.Collection;
import com.senz.sdk.Senz;
import com.senz.sdk.Beacon;
import com.senz.sdk.BeaconWithSenz;
import com.senz.sdk.network.Cacher;

public class Cache {
    static private Cacher cacher;

    static private void checkCacher() {
        if (cacher == null)
            // Cache 300 most visited Beacons by default
            cacher = new Cacher(300);
    }

    static public BeaconWithSenz lookupBeacon(Beacon beacon) {
        Cache.checkCacher();
        return Cache.cacher.lookupBeacon(beacon);
    }

    static public void addBeaconsWithSenz(Collection<BeaconWithSenz> bwss) {
        Cache.checkCacher();
        for (BeaconWithSenz bws : bwss)
            Cache.cacher.addBeaconWithSenz(bws);
    }
}
