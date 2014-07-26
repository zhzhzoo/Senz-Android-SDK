package com.senz.sdk;

import android.util.JsonReader;
import android.util.JsonWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import com.senz.sdk.Beacon;
import com.senz.sdk.utils.Jsonable;

public class BeaconWithSenzes extends Beacon {
    private ArrayList<Senz> mSenzes;

    public BeaconWithSenzes(Beacon beacon, ArrayList<Senz> senzes) {
        super(beacon);
        this.mSenzes = senzes;
    }

    public void setSenzes(List<Senz> senzes) {
        if (this.mSenzes == null)
            this.mSenzes = new ArrayList(senzes);
        else {
            this.mSenzes.clear();
            this.mSenzes.addAll(senzes);
        }
    }

    public List<Senz> getSenzes() {
        return this.mSenzes;
    }

    @Override
    public void writeToJson(JsonWriter writer) throws IOException {
        writer.beginObject();
        this.writeToJsonNoBeginEnd(writer);
        writer.endObject();
    }

    public void writeToJsonNoBeginEnd(JsonWriter writer) throws IOException {
        super.writeToJsonNoBeginEnd(writer);
        writer.name("senzes");
        Senz.writeSenzesIdArray(writer, mSenzes);
    }

    public static BeaconWithSenzes fromJsonAndSenzesById(JsonReader reader, final Map<String, Senz> senzesById) throws IOException {
        reader.beginObject();
        BeaconWithSenzes bws = new BeaconWithSenzes(reader, senzesById);
        Utils.skipProperties(reader);
        reader.endObject();
        return bws;
    }

    // doesn't read beginning and ending of object
    public BeaconWithSenzes(JsonReader reader, final Map<String, Senz> senzesById) throws IOException {
        super(reader);
        int remaining = 1;
        String name;

        while (remaining != 0 && reader.hasNext()) {
            name = reader.nextName();
            remaining--;
            switch (name) {
                case "senzes":
                    this.mSenzes = Senz.senzesFromJsonIdArray(reader, senzesById);
                    break;
                default:
                    remaining++;
                    reader.skipValue();
            }
        }
    }
}
