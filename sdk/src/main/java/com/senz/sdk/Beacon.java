package com.senz.sdk;

import android.bluetooth.BluetoothDevice;
import android.os.Parcelable;
import android.os.Parcel;
import java.util.UUID;
import com.senz.sdk.Utils;

public class Beacon implements Parcelable {

    private UUID mUUID;
    private String mMAC;
    private int mMajor;
    private int mMinor;
    private int mMPower;
    private int mRSSI;

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(mUUID.getMostSignificantBits());
        out.writeLong(mUUID.getLeastSignificantBits());
        out.writeString(mMAC);
        out.writeInt(mMajor);
        out.writeInt(mMinor);
        out.writeInt(mMPower);
        out.writeInt(mRSSI);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<Beacon> CREATOR
        = new Parcelable.Creator<Beacon>() {
            public Beacon createFromParcel(Parcel in) {
                return new Beacon(in);
            }

            public Beacon[] newArray(int size) {
                return new Beacon[size];
            }
        };

    public Beacon(Parcel in) {
        long hi, lo;
        hi = in.readLong();
        lo = in.readLong();
        this.mUUID = new UUID(hi, lo);
        this.mMAC = in.readString();
        this.mMajor = in.readInt();
        this.mMinor = in.readInt();
        this.mMPower = in.readInt();
        this.mRSSI = in.readInt();
    }

    public Beacon(UUID uuid, String mac, int major, int minor, int mpower, int rssi) {
        this.mUUID = uuid;
        this.mMAC = mac;
        this.mMajor = major;
        this.mMinor = minor;
        this.mMPower = mpower;
        this.mRSSI = rssi;
    }

    public static Beacon fromLeScan(BluetoothDevice device, int rssi, byte[] scanResult) {
        int len, i;

        for (i = 0; i < scanResult.length; i++) {
            len = Utils.unsignedByteToInt(scanResult[i]);
            if (len == 0 || (i + 1 >= scanResult.length))
                break;

            // check AD structure length and Manufacturer specific data AD type
            if (len != 0x1A || Utils.unsignedByteToInt(scanResult[i + 1]) != 0xFF)
                i += len;
            // check Company identifier code (0x004C == Apple)
            // and iBeacon advertisement indicator
            else if (Utils.unsignedByteToInt(scanResult[i + 2]) != 0x4C
                  || Utils.unsignedByteToInt(scanResult[i + 3]) != 0x00
                  || Utils.unsignedByteToInt(scanResult[i + 4]) != 0x02
                  || Utils.unsignedByteToInt(scanResult[i + 5]) != 0x15)
                i += len;
            // it is iBeacon
            else {
                UUID uuid = new UUID(Utils.longFrom8Bytes(scanResult[i + 6],
                                                          scanResult[i + 7],
                                                          scanResult[i + 8],
                                                          scanResult[i + 9],
                                                          scanResult[i + 10],
                                                          scanResult[i + 11],
                                                          scanResult[i + 12],
                                                          scanResult[i + 13]),
                                     Utils.longFrom8Bytes(scanResult[i + 14],
                                                          scanResult[i + 15],
                                                          scanResult[i + 16],
                                                          scanResult[i + 17],
                                                          scanResult[i + 18],
                                                          scanResult[i + 19],
                                                          scanResult[i + 20],
                                                          scanResult[i + 21]));
                int major = Utils.intFrom2Bytes(scanResult[i + 22], scanResult[i + 23]);
                int minor = Utils.intFrom2Bytes(scanResult[i + 24], scanResult[i + 25]);
                int mpower = scanResult[i + 26];

                return new Beacon(uuid, device.getAddress(), major, minor, mpower, rssi);
            }
        }
        return null;
    }

    public UUID getUUID() {
        return mUUID;
    }
    
    public String getMAC() {
        return mMAC;
    }

    public int getMajor() {
        return mMajor;
    }

    public int getMinor() {
        return mMinor;
    }

    public int getMPower() {
        return mMPower;
    }

    public int getRSSI() {
        return mRSSI;
    }
}
