package com.senz.sdk.contezt

import java.lang.reflect.Constructor;
import com.android.os.Parcelable;
import com.avos.avoscloud.AVObject;
import com.senz.sdk.utils.L;
import com.senz.sdk.Utils.*;

abstract class Contezt implements Parcelable {
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.what());
        writeToParcelRemaining(out, flags);
    }

    abstract protected void writeToParcelRemaining(Parcel out, int flags) {
    }

    abstract protected toAVObject() {
    }

    abstract static String what() {
    }

    private static Contezt NewInstanceThroughConstructor(String what, Object arg) {
        Class<?> clazz;
        Constructor<?> ctor;

        try {
            clazz = Class.forName("com.senz.sdk.contezt." + capitalize(what()) + "Contezt");
        }
        catch (Exception e) {
            L.wtf("Can't get subclass!", e);
            return null;
        }

        try {
            ctor = class.getConstructor(arg.getClass());
        }
        catch (Exception e) {
            L.wtf("Can't get constructor!", e);
            return null;
        }

        try {
            return ctor.newInstance(arg);
        }
        catch (Exception e) {
            L.wtf("Can't get an instance!", e);
            return null;
        }
    }

    protected static Contezt FromAVObject(AVObject avo) {
        return NewInstanceThroughConstructor(avo.getString("what"));
    }

    protected static Contezt FromParcel(Parcel in) {
        // first element in `in' is `what'
        return NewInstanceThroughConstructor(in.readString(), in);
    }

    public static final Parcelable.Creator<Contezt> CREATOR
            = new Parcelable.Creator<Contezt> () {
        public Contezt createFromParcel(Parcel in) {
            return FromParcel(in);
        }

        public Contezt[] newArray(int size) {
            return new Contezt[size];
        }
    };
}
