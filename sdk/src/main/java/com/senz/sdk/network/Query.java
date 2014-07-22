package com.senz.sdk.network;

import android.location.Location;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Collection;
import com.senz.sdk.Senz;
import com.senz.sdk.Beacon;
import com.senz.sdk.BeaconWithSenzes;
import com.senz.sdk.network.Cache;
import com.senz.sdk.network.Network;
import com.senz.sdk.utils.Asyncfied;

public class Query {
    static Filter filter;

    static Filter getFilter() {
        if (Query.filter == null)
            // don't report beacons that has been reported in 30 minutes
            Query.filter = new Filter(TimeUnit.MINUTES.toMillis(30));
        return Query.filter;
    }

    static private <T> void addAllToHashSet(Collection<T> c, HashSet<T> hs) {
        for (T t : c)
            hs.add(t);
    }

    static public ArrayList<Senz> senzesFromBeacons(Collection<Beacon> beacons, Location lastBeen) {
        HashSet<Senz> result = new HashSet<Senz>();
        ArrayList<Beacon> toQueryServer = new ArrayList<Beacon>();

        beacons = getFilter().filterTime(beacons);
        for (Beacon beacon : beacons) {
            BeaconWithSenzes bws = Cache.lookupBeacon(beacon);
            if (bws == null)
                toQueryServer.add(beacon);
            else
                addAllToHashSet(bws.getSenzes(), result);
        }

        Collection<BeaconWithSenzes> bwss = Network.queryBeacons(toQueryServer, lastBeen);
        Cache.addBeaconsWithSenzes(bwss);
        for (BeaconWithSenzes bws : bwss)
            addAllToHashSet(bws.getSenzes(), result);
        return new ArrayList<Senz>(result);
    }

    static public ArrayList<Senz> senzesFromLocation(Location location) {
        HashSet<Senz> result = new HashSet<Senz>();
        ArrayList<BeaconWithSenzes> bwss = Network.queryLocation(location);
        Cache.addBeaconsWithSenzes(bwss);
        for (BeaconWithSenzes bws : bwss)
            addAllToHashSet(bws.getSenzes(), result);
        return new ArrayList<Senz>(result);
    }

    static public void senzesFromBeaconsAsync(final Collection<Beacon> beacons, final Location location, final SenzesReadyCallback cb) {
        Asyncfied.runAsyncfiable(new Asyncfied.Asyncfiable<ArrayList<Senz>>() {
            @Override
            public ArrayList<Senz> runAndReturn() {
                return senzesFromBeacons(beacons, location);
            }

            @Override
            public void onReturn(ArrayList<Senz> result) {
                cb.onSenzesReady(result);
            }
        });
    }

    static public void senzesFromLocationAsync(final Location location, final SenzesReadyCallback cb) {
        Asyncfied.runAsyncfiable(new Asyncfied.Asyncfiable<ArrayList<Senz>>() {
            @Override
            public ArrayList<Senz> runAndReturn() {
                return senzesFromLocation(location);
            }

            @Override
            public void onReturn(ArrayList<Senz> result) {
                cb.onSenzesReady(result);
            }
        });
    }

    public interface SenzesReadyCallback {
        public void onSenzesReady(ArrayList<Senz> senzes);
    }
}
