package com.senz.sdk.network;

import android.location.Location;
import android.util.JsonReader;
import android.util.JsonWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.InputStreamReader;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import com.senz.sdk.Senz;
import com.senz.sdk.Utils;
import com.senz.sdk.Beacon;
import com.senz.sdk.BeaconWithSenzes;

public class Network {
    private static String queryUrl = "https://cn.avoscloud.com/1/functions/";
    private static int timeout = (int) TimeUnit.SECONDS.toMillis(10);
    private static String AVOS_ID = "vigxpgtjk8w6ruxcfaw4kju3ssyttgcqz38y6y6uablqivjd";
    private static String AVOS_KEY = "dxbawm2hh0338hb37wap59gticgr92dpajd80tzekrgv1ptw";

    private static void writeLocation(JsonWriter writer, Location location) throws IOException {
        writer.beginObject();
        writer.name("latitude").value(location.getLatitude());
        writer.name("longitude").value(location.getLongitude());
        writer.name("accuracy").value(location.getAccuracy());
        writer.name("time").value(location.getTime());
        writer.endObject();
    }

    private static void writeBeaconsQueryPost(JsonWriter writer, Collection<Beacon> toQuery, Location lastBeen) throws IOException {
        writer.beginObject();

        writer.name("meta");
        writer.beginObject();
        if (lastBeen != null) {
            writer.name("location");
            writeLocation(writer, lastBeen);
        }
        writer.endObject();
        
        writer.name("beacons");
        Utils.writeToJsonArray(writer, toQuery);
        
        writer.endObject();
        writer.close();
    }

    private static void writeLocationQueryPost(JsonWriter writer, Location location) throws IOException {
        writer.beginObject();

        writer.name("location");
        writeLocation(writer, location);

        writer.endObject();
        writer.close();
    }

    private static HashMap<String, Senz> readSenzHashMapFromJsonArray(JsonReader reader) throws IOException {
        HashMap<String, Senz> msenz = new HashMap<String, Senz>();
        reader.beginArray();
        while (reader.hasNext()) {
            Senz next = Senz.fromJson(reader);
            msenz.put(next.id(), next);
        }
        reader.endArray();
        return msenz;
    }

    private static ArrayList<BeaconWithSenzes> readBeaconWithSenzesArrayListFromJsonArrayAndSenzesById(JsonReader reader, Map<String, Senz> senzesById) throws IOException {
        ArrayList<BeaconWithSenzes> bwss = new ArrayList<BeaconWithSenzes>();
        reader.beginArray();
        while (reader.hasNext()) {
            BeaconWithSenzes next = BeaconWithSenzes.fromJsonAndSenzesById(reader, senzesById);
            bwss.add(next);
        }
        reader.endArray();
        return bwss;
    }

    private static ArrayList<BeaconWithSenzes> readResult(JsonReader reader) throws IOException {
        String name;
        HashMap<String, Senz> senzesById = null;
        ArrayList<BeaconWithSenzes> bwss = null;

        reader.beginObject();
        while (reader.hasNext()) {
            name = reader.nextName();
            switch (name) {
                case "senzes":
                    senzesById = readSenzHashMapFromJsonArray(reader);
                    break;
                case "beacons":
                    bwss = readBeaconWithSenzesArrayListFromJsonArrayAndSenzesById(reader, senzesById);
                    break;
            }
        }
        reader.endObject();
        reader.close();

        return bwss;
    }

    private interface QueryWriter {
        public void write(OutputStream os) throws IOException;
    }

    private interface ResultReader<T> {
        public T read(InputStream is) throws IOException;
    }

    public static <T> T doQuery(URL url, QueryWriter w, ResultReader<T> r) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        urlConnection.setConnectTimeout(timeout);
        urlConnection.setRequestProperty("Content-Type", "application/json");
        urlConnection.setRequestProperty("charset", "utf-8");
        urlConnection.setRequestProperty("X-AVOSCloud-Application-Id", AVOS_ID);
        urlConnection.setRequestProperty("X-AVOSCloud-Application-Key", AVOS_KEY);
        urlConnection.setRequestProperty("X-AVOSCloud-Application-Production", "0");
        T t = null;

        try {
            if (w != null) {
                urlConnection.setDoOutput(true);
                urlConnection.setChunkedStreamingMode(0);
            }

            w.write(urlConnection.getOutputStream());

            t = r.read(urlConnection.getInputStream());
        }
        finally {
            urlConnection.disconnect();
        }

        return t;
    }


    public static ArrayList<BeaconWithSenzes> queryBeacons(final Collection<Beacon> toQuery, final Location lastBeen) throws IOException {
        return doQuery(
                new URL(queryUrl + "queryWithBeacon"),
                new QueryWriter() {
                    @Override
                    public void write(OutputStream os) throws IOException {
                        writeBeaconsQueryPost(new JsonWriter(new OutputStreamWriter(os)), toQuery, lastBeen);
                    }
                },
                new ResultReader<ArrayList<BeaconWithSenzes>>() {
                    @Override
                    public ArrayList<BeaconWithSenzes> read(InputStream is) throws IOException {
                        return readResult(new JsonReader(new InputStreamReader(is)));
                    }
                });
    }

    public static ArrayList<BeaconWithSenzes> queryLocation(final Location location) throws IOException {
        return doQuery(
                new URL(queryUrl + "queryWithLocation"),
                new QueryWriter() {
                    @Override
                    public void write(OutputStream os) throws IOException {
                        writeLocationQueryPost(new JsonWriter(new OutputStreamWriter(os)), location);
                    }
                },
                new ResultReader<ArrayList<BeaconWithSenzes>>() {
                    @Override
                    public ArrayList<BeaconWithSenzes> read(InputStream is) throws IOException {
                        return readResult(new JsonReader(new InputStreamReader(is)));
                    }
                });
    }
}
