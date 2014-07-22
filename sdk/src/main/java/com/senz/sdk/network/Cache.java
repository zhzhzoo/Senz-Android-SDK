package com.senz.sdk.network;

import java.util.Collection;
import com.senz.sdk.Senz;
import com.senz.sdk.Beacon;
import com.senz.sdk.BeaconWithSenzes;
import com.senz.sdk.network.Cacher;

public class Cache {
    static private Cacher cacher;

    static private void checkCacher() {
        if (cacher == null)
            // Cache 300 most visited Beacons by default
            cacher = new Cacher(300);
    }

    static public BeaconWithSenzes lookupBeacon(Beacon beacon) {
        Cache.checkCacher();
        return Cache.cacher.lookupBeacon(beacon);
    }

    static public void addBeaconsWithSenzes(Collection<BeaconWithSenzes> bwss) {
        Cache.checkCacher();
        for (BeaconWithSenzes bws : bwss)
            Cache.cacher.addBeaconWithSenzes(bws);
    }
}
