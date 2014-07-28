package com.senz.sdk.network;

import android.location.Location;
import java.util.concurrent.TimeUnit;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Collection;
import java.io.IOException;
import com.senz.sdk.Senz;
import com.senz.sdk.Beacon;
import com.senz.sdk.BeaconWithSenz;
import com.senz.sdk.network.Cache;
import com.senz.sdk.network.Network;
import com.senz.sdk.utils.Asyncfied;

public class Query {
    static private <T> void addAllToHashSet(Collection<T> c, HashSet<T> hs) {
        for (T t : c)
            hs.add(t);
    }

    static public ArrayList<Senz> senzesFromBeacons(Collection<Beacon> beacons, Location lastBeen) throws IOException {
        HashSet<Senz> result = new HashSet<Senz>();
        ArrayList<Beacon> toQueryServer = new ArrayList<Beacon>();

        for (Beacon beacon : beacons) {
            BeaconWithSenz bws = Cache.lookupBeacon(beacon);
            if (bws == null)
                toQueryServer.add(beacon);
            else
                result.add(bws.getSenz());
        }

        Collection<BeaconWithSenz> bwss = Network.queryBeacons(toQueryServer, lastBeen);
        Cache.addBeaconsWithSenz(bwss);
        for (BeaconWithSenz bws : bwss)
            result.add(bws.getSenz());
        return new ArrayList<Senz>(result);
    }

    static public ArrayList<Senz> senzesFromLocation(Location location) throws IOException {
        HashSet<Senz> result = new HashSet<Senz>();
        ArrayList<BeaconWithSenz> bwss = Network.queryLocation(location);
        Cache.addBeaconsWithSenz(bwss);
        for (BeaconWithSenz bws : bwss)
            result.add(bws.getSenz());
        return new ArrayList<Senz>(result);
    }

    static public void senzesFromBeaconsAsync(final Collection<Beacon> beacons, final Location location, final SenzReadyCallback cb, final ErrorHandler eh) {
        Asyncfied.runAsyncfiable(new Asyncfied.Asyncfiable<ArrayList<Senz>>() {
            @Override
            public ArrayList<Senz> runAndReturn() throws IOException {
                return senzesFromBeacons(beacons, location);
            }

            @Override
            public void onReturn(ArrayList<Senz> result) {
                cb.onSenzReady(result);
            }

            @Override
            public void onError(Exception e) {
                eh.onError(e);
            }
        });
    }

    static public void senzesFromLocationAsync(final Location location, final SenzReadyCallback cb, final ErrorHandler eh) {
        Asyncfied.runAsyncfiable(new Asyncfied.Asyncfiable<ArrayList<Senz>>() {
            @Override
            public ArrayList<Senz> runAndReturn() throws IOException {
                return senzesFromLocation(location);
            }

            @Override
            public void onReturn(ArrayList<Senz> result) {
                cb.onSenzReady(result);
            }

            @Override
            public void onError(Exception e) {
                eh.onError(e);
            }
        });
    }

    public interface SenzReadyCallback {
        public void onSenzReady(ArrayList<Senz> senzes);
    }
    
    public interface ErrorHandler {
        public void onError(Exception e);
    }
}
