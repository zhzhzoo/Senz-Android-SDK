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
import com.senz.sdk.utils.Jsonable;
import com.senz.sdk.Utils;
import com.senz.sdk.content.Content;

public class Senz implements Parcelable, Jsonable {
    private String mId;
    private Content mContent;

    public Senz(String id) {
        mId = id;
    }

    @Override
    public int hashCode() {
        return this.id().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;

        if (obj instanceof Senz)
            return this.id().equals(((Senz) obj).id());

        return false;
    }

    public String id() {
        return mId;
    }

    public String what() {
        return this.mContent.what();
    }

    public Content getContent() {
        return this.mContent;
    }

    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.what());
        out.writeString(this.id());
        this.mContent.writeToParcel(out, flags);
    }

    @Override
    public void writeToJson(JsonWriter writer) throws IOException {
        writer.name("id").value(this.id());
        writer.name("what").value(this.what());
        writer.name("content");
        this.mContent.writeToJson(writer);
    }

    public Senz(Parcel in) {
        String what;

        this.mId = in.readString();
        what = in.readString();
        this.mContent = newContent(what, in);
    }

    public Senz(JsonReader reader) throws IOException {
        String what = null;

        reader.beginObject();
        while (reader.hasNext()) {
            switch (reader.nextName()) {
                case "id":
                    this.mId = reader.nextString();
                    break;
                case "what":
                    what = reader.nextString();
                    break;
                case "content":
                    this.mContent = newContent(what, reader);
                    break;
            }
        }
        reader.endObject();
    }

    public static Senz fromJson(JsonReader reader) throws IOException {
        return new Senz(reader);
    }

    private static <T> Content newContent(String what, T arg) {
        Class<?> clazz;
        Constructor<?> ctor;

        try {
            clazz = Class.forName("com.senz.sdk.content." + Utils.capitalize(what) + "Content");
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
            Content res = (Content) ctor.newInstance(arg);
            return res;
        }
        catch (Exception e) {
            L.wtf("Can't get an instance!", e);
            return null;
        }
    }

    public static final Jsonable.Creator<Senz> JsonCREATOR
        = new Jsonable.Creator<Senz>() {
            @Override
            public Senz createFromJson(JsonReader in) throws IOException {
                return Senz.fromJson(in);
            }
        };

    public static final Parcelable.Creator<Senz> CREATOR
        = new Parcelable.Creator<Senz> () {
            public Senz createFromParcel(Parcel in) {
                return new Senz(in);
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
