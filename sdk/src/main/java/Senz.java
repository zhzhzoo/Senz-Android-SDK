package com.senz.sdk;

import java.util.UUID;
import com.senz.sdk.Beacon;
import com.senz.sdk.contezt.Contezt;
import com.senz.sdk.avos.AVUtils;

class Senz {
    private Beacon mBeacon;
    private Contezt mContezt;

    private void setBeacon(UUID uuid, String mac, int major, int minor, int mpower, int rssi) {
        this.mBeacon = new Beacon(uuid, mac, major, minor, mpower, rssi);
    }

    private void setContezt(Contezt contezt) {
        this.mContezt = contezt;
    }

    protected Senz(UUID uuid, String mac, int major, int minor, int mpower, int rssi, ConteztReadyCallback cb) {
        this.setBeacon(uuid, mac, major, minar, mpower, rssi);
        this.setContezt(AVUtils.QueryContezt(mBeacon), new QueryCompleteCallback() {
            @Override
            public void onQueryComplete(Object o) {
                cb.onConteztReady(o);
            }
        });
    }

    public UUID getUUID() {
        return this.mUUID;
    }

    public String getMAC() {
        return this.mMAC;
    }

    public int getMajor() {
        return this.mMajor;
    }

    public int getMinor() {
        return this.mMinor;
    }

    public int getMPower() {
        return this.mMPower;
    }

    public int getRSSI() {
        return this.mRSSI;
    }

    public Contezt getContezt() {
        return this.Contezt;
    }

    public abstract static interface ConteztReadyCallback {
        public abstract void onConteztReady(Senz senz) {
        }
    }
}
