package com.senz.sdk.network;

import android.location.Location;
import java.util.ArrayList;
import java.util.Collection;
import com.senz.sdk.Senz;
import com.senz.sdk.Beacon;
import com.senz.sdk.BeaconWithSenzes;

public class Network {
    public static ArrayList<BeaconWithSenzes> queryBeacons(Collection<Beacon> toQuery, Location lastBeen) {
        //TODO : implement server query
        return null;
    }

    public static ArrayList<BeaconWithSenzes> queryLocation(Location location) {
        //TODO : implement server query
        return null;
    }
}
