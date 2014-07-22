package com.senz.sdk.contezt;

import java.lang.reflect.Constructor;
import android.os.Parcelable;
import android.os.Parcel;
import com.avos.avoscloud.AVObject;
import com.senz.sdk.utils.L;
import com.senz.sdk.Utils;

abstract public class Contezt implements Parcelable {
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.what());
        writeToParcelRemaining(out, flags);
    }

    abstract protected void writeToParcelRemaining(Parcel out, int flags);

    abstract public String what();

    private static <T> Contezt newInstanceThroughConstructor(String what, T arg) {
        Class<?> clazz;
        Constructor<?> ctor;

        try {
            clazz = Class.forName("com.senz.sdk.contezt." + Utils.capitalize(what) + "Contezt");
        }
        catch (Exception e) {
            L.wtf("Can't get subclass!", e);
            return null;
        }

        try {
            ctor = clazz.getConstructor(arg.getClass());
        }
        catch (Exception e) {
            L.wtf("Can't get constructor!", e);
            return null;
        }

        try {
            return (Contezt) ctor.newInstance(arg);
        }
        catch (Exception e) {
            L.wtf("Can't get an instance!", e);
            return null;
        }
    }

    public static Contezt fromAVObject(AVObject avo) {
        return avo == null ? null : newInstanceThroughConstructor(avo.getString("what"), avo);
    }

    public static Contezt fromParcel(Parcel in) {
        // first element in `in' is `what'
        return in == null ? null : newInstanceThroughConstructor(in.readString(), in);
    }

    public static final Parcelable.Creator<Contezt> CREATOR
            = new Parcelable.Creator<Contezt> () {
        public Contezt createFromParcel(Parcel in) {
            return fromParcel(in);
        }

        public Contezt[] newArray(int size) {
            return new Contezt[size];
        }
    };
}
