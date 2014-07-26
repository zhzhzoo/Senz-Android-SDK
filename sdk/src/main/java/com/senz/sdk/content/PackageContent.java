package com.senz.sdk.content;

import android.os.Parcelable;
import android.os.Parcel;
import android.util.JsonReader;
import android.util.JsonWriter;
import java.io.IOException;
import com.senz.sdk.content.Content;

final public class PackageContent extends Content {
    String mPackageName;

    @Override
    public String toString() {
        return String.format("package senz %s", mPackageName);
    }

    public PackageContent(Parcel in) {
        this.mPackageName = in.readString();
    }

    public PackageContent(JsonReader reader) throws IOException {
        reader.beginObject();
        switch(reader.nextName()) {
            case "name":
                this.mPackageName = reader.nextString();
                break;
            default:
                reader.skipValue();
        }
        reader.endObject();
    }

    public void writeToJson(JsonWriter writer) throws IOException {
        writer.beginObject();
        writer.name("name").value(mPackageName);
        writer.endObject();
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(mPackageName);
    }

    @Override
    public String what() {
        return "package";
    }

    public String getPackageName() {
        return mPackageName;
    }
}
