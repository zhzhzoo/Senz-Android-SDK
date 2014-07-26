package com.senz.sdk.content;

import android.os.Parcelable;
import android.util.JsonWriter;
import java.io.IOException;

abstract public class Content implements Parcelable {

    abstract public String what();

    @Override
    public int describeContents() {
        return 0;
    }

    abstract public void writeToJson(JsonWriter writer) throws IOException;
}
