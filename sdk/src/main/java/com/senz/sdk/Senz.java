package com.senz.sdk;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.io.IOException;
import android.os.Parcelable;
import android.os.Parcel;
import android.util.JsonReader;
import android.util.JsonWriter;
import com.senz.sdk.utils.L;
import com.senz.sdk.utils.Jsonable;
import com.senz.sdk.Utils;

public class Senz implements Parcelable, Jsonable {
    private String mId;
    private String mType;
    private String mSubType;
    private HashMap<String, String> mEntities;

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

    public String type() {
        return mType;
    }

    public String subType() {
        return mSubType;
    }

    public Map<String, String> entities() {
        return mEntities;
    }

    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(this.id());
        out.writeString(this.type());
        out.writeString(this.subType());
        Utils.writeParcelStringMap(out, this.entities());
    }

    @Override
    public void writeToJson(JsonWriter writer) throws IOException {
        writer.name("id").value(this.id());
        writer.name("type").value(this.type());
        writer.name("subType").value(this.subType());
        writer.name("content");
        Utils.writeStringMapAsJsonObject(writer, this.entities());
    }

    public Senz(Parcel in) {
        String what;

        this.mId = in.readString();
        this.mType = in.readString();
        this.mSubType = in.readString();
        this.mEntities = new HashMap<String, String>();
        Utils.readParcelStringMap(this.mEntities, in);
    }

    public Senz(JsonReader reader) throws IOException {
        String what = null;

        reader.beginObject();
        while (reader.hasNext()) {
            switch (reader.nextName()) {
                case "objectId":
                    this.mId = reader.nextString();
                    break;
                case "type":
                    this.mType = reader.nextString();
                    break;
                case "subType":
                    this.mSubType = reader.nextString();
                    break;
                case "entities":
                    this.mEntities = new HashMap<String, String>();
                    Utils.readJsonStringMap(reader, this.mEntities);
                    break;
                default:
                    reader.skipValue();
            }
        }
        reader.endObject();
    }

    public static Senz fromJson(JsonReader reader) throws IOException {
        return new Senz(reader);
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

    public static void writeSenzIdArray(JsonWriter writer, List<Senz> senzes) throws IOException{
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
