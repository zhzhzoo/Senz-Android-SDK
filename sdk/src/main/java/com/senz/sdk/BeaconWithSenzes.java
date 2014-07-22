package com.senz.sdk;

import android.util.JsonReader;
import android.util.JsonWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.io.IOException;
import com.senz.sdk.Beacon;
import com.senz.sdk.utils.Wrapper;

public class BeaconWithSenzes extends Beacon{
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

    public void writeToJson(JsonWriter writer) throws IOException {
        super.writeToJson(writer, new JsonAppender() {
            @Override
            public void doAppend(JsonWriter writer) throws IOException {
                writer.name("senzes");
                Senz.writeSenzesIdArray(writer, mSenzes);
            }
        });
    }

    public static BeaconWithSenzes fromJsonAndSenzesById(JsonReader reader, final Map<String, Senz> senzesById) throws IOException {
        Beacon beacon;
        final Wrapper<ArrayList<Senz>> pSenzes = new Wrapper();

        beacon = Beacon.fromJson(reader, new JsonPropertyHook() {
            @Override
            public void readProperty(String name, JsonReader reader) throws IOException {
                if (name == "senzes")
                    pSenzes.deref = Senz.senzesFromJsonIdArray(reader, senzesById);
            }
        });

        return new BeaconWithSenzes(beacon, pSenzes.deref);
    }
}
