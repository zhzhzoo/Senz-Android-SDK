package com.senz.sdk;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.io.IOException;
import android.os.Parcelable;
import android.os.Parcel;
import android.util.JsonReader;
import android.util.JsonWriter;
import com.senz.sdk.utils.L;
import com.senz.sdk.Utils;

abstract public class Senz implements Parcelable {
    private String mId;

    public Senz(String id) {
        mId = id;
    }

    public String id() {
        return mId;
    }
    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.what());
        out.writeString(this.id());
        writeToParcelRemaining(out, flags);
    }

    public void writeToJson(JsonWriter writer) throws IOException {
        writer.name("id").value(this.id());
        writer.name("what").value(this.what());
        writeToJsonRemaining(writer);
    }

    abstract protected void writeToParcelRemaining(Parcel out, int flags);

    abstract protected void writeToJsonRemaining(JsonWriter writer) throws IOException;

    abstract public String what();

    abstract public boolean correct();

    private static <T> Senz newInstanceThroughConstructor(String what, String id, T arg) {
        Class<?> clazz;
        Constructor<?> ctor;

        try {
            clazz = Class.forName("com.senz.sdk.senz." + Utils.capitalize(what) + "Senz");
        }
        catch (Exception e) {
            L.wtf("Can't get subclass!", e);
            return null;
        }

        try {
            ctor = clazz.getConstructor(String.class, arg.getClass());
        }
        catch (Exception e) {
            L.wtf("Can't get constructor!", e);
            return null;
        }

        try {
            Senz res = (Senz) ctor.newInstance(id, arg);
            if (res.correct())
                return res;
            return null;
        }
        catch (Exception e) {
            L.wtf("Can't get an instance!", e);
            return null;
        }
    }

    public static Senz fromJson(JsonReader reader) throws IOException {
        Senz senz = null;
        String what = null, id = null;

        reader.beginObject();
        while (reader.hasNext()) {
            switch (reader.nextName()) {
                case "id":
                    id = reader.nextString();
                    break;
                case "what":
                    what = reader.nextString();
                    break;
                case "content":
                    senz = newInstanceThroughConstructor(what, id, reader);
                    break;
            }
        }
        reader.endObject();

        return senz;
    }

    public static Senz fromParcel(Parcel in) {
        // first element in `in' is `what'
        return in == null ? null : newInstanceThroughConstructor(in.readString(), in.readString(), in);
    }

    public static final Parcelable.Creator<Senz> CREATOR
            = new Parcelable.Creator<Senz> () {
        public Senz createFromParcel(Parcel in) {
            return fromParcel(in);
        }

        public Senz[] newArray(int size) {
            return new Senz[size];
        }
    };

    public static void writeSenzesIdArray(JsonWriter writer, List<Senz> senzes) throws IOException{
        writer.beginArray();
        for (Senz senz : senzes) {
            writer.value(senz.id());
        }
        writer.endArray();
    }

    public static ArrayList<Senz> senzesFromJsonIdArray(JsonReader reader, Map<String, Senz> senzesById) throws IOException {
        ArrayList<Senz> senzes = new ArrayList<Senz>();
        reader.beginArray();
        while (reader.hasNext()) {
            String id = reader.nextString();
            if (senzesById.containsKey(id))
                senzes.add(senzesById.get(id));
        }
        reader.endArray();
        return senzes;
    }
}
