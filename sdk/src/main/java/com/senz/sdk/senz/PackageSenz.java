package com.senz.sdk.contezt;

import android.os.Parcelable;
import android.os.Parcel;
import android.util.JsonReader;
import android.util.JsonWriter;
import java.io.IOException;
import com.senz.sdk.Senz;

final class PackageSenz extends Senz {
    String mPackageName;

    @Override
    public String toString() {
        return String.format("package contezt %s", mPackageName);
    }

    protected PackageSenz(String id, Parcel in) {
        super(id);
        this.mPackageName = in.readString();
    }

    protected PackageSenz(String id, JsonReader reader) throws IOException {
        super(id);
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

    protected void writeToJsonRemaining(JsonWriter writer) throws IOException {
        writer.beginObject();
        writer.name("name").value(mPackageName);
        writer.endObject();
    }

    protected void writeToParcelRemaining(Parcel out, int flags) {
        out.writeString(mPackageName);
    }

    public String what() {
        return "package";
    }

    public String getPackageName() {
        return mPackageName;
    }

    public boolean correct() {
        return mPackageName != null;
    }
}
