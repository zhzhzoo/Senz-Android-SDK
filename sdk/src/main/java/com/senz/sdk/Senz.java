package com.senz.sdk;

import java.util.UUID;
import com.senz.sdk.Beacon;
import com.senz.sdk.contezt.Contezt;
import com.senz.sdk.avos.AVUtils;

class Senz {
    private Beacon mBeacon;
    private Contezt mContezt;

    private void setBeacon(Beacon beacon) {
        this.mBeacon = beacon;
    }

    private void setBeaconParams(UUID uuid, String mac, int major, int minor, int mpower, int rssi) {
        this.setBeacon(new Beacon(uuid, mac, major, minor, mpower, rssi));
    }

    private void setContezt(Contezt contezt) {
        this.mContezt = contezt;
    }

    private Senz(Beacon beacon, Contezt contezt) {
        this.setBeacon(beacon);
        this.setContezt(contezt);
    }

    private Senz(UUID uuid, String mac, int major, int minor, int mpower, int rssi, Contezt contezt) {
        this.setBeaconParams(uuid, mac, major, minor, mpower, rssi);
        this.setContezt(contezt);
    }

    public static void fromBeacon(final Beacon beacon, final SenzReadyCallback cb) {
        AVUtils.queryContezt(beacon, new AVUtils.QueryConteztCompleteCallback() {
            @Override
            public void onComplete(final Contezt contezt) {
                cb.onSenzReady(new Senz(beacon, contezt));
            }
        });
    }

    public UUID getUUID() {
        return this.mBeacon.getUUID();
    }

    public String getMAC() {
        return this.mBeacon.getMAC();
    }

    public int getMajor() {
        return this.mBeacon.getMajor();
    }

    public int getMinor() {
        return this.mBeacon.getMinor();
    }

    public int getMPower() {
        return this.mBeacon.getMPower();
    }

    public int getRSSI() {
        return this.mBeacon.getRSSI();
    }

    public Contezt getContezt() {
        return this.mContezt;
    }

    public interface SenzReadyCallback {
        public void onSenzReady(Senz senz);
    }
}
