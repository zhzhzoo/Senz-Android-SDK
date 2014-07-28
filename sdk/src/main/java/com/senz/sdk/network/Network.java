package com.senz.sdk.network;

import android.location.Location;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.util.Pair;
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
import java.io.StringReader;
import java.io.StringWriter;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import com.senz.sdk.utils.L;
import com.senz.sdk.Senz;
import com.senz.sdk.Utils;
import com.senz.sdk.Beacon;
import com.senz.sdk.BeaconWithSenz;

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

    private static HashMap<String, Senz> readSenzHashMapFromJsonObject(JsonReader reader) throws IOException {
        HashMap<String, Senz> msenz = new HashMap<String, Senz>();
        reader.beginObject();
        while (reader.hasNext())
            msenz.put(reader.nextName(), Senz.fromJson(reader));
        reader.endObject();
        return msenz;
    }

    private static ArrayList<BeaconWithSenz> readBeaconWithSenzArrayListFromJsonArrayAndSenzById(JsonReader reader, Map<String, Senz> senzesById) throws IOException {
        ArrayList<BeaconWithSenz> bwss = new ArrayList<BeaconWithSenz>();
        reader.beginArray();
        while (reader.hasNext())
            bwss.add(BeaconWithSenz.fromJsonAndSenzById(reader, senzesById));
        reader.endArray();
        return bwss;
    }

    private static ArrayList<Pair<Beacon, String>> readBeaconSenzIdPairArrayListFromJsonArray(JsonReader reader) throws IOException {
        ArrayList<Pair<Beacon, String>> tmp = new ArrayList<Pair<Beacon, String>>();
        reader.beginArray();
        while (reader.hasNext()) {
            tmp.add(BeaconWithSenz.beaconSenzIdPairFromJson(reader));
        }
        reader.endArray();
        return tmp;
    }

    private static ArrayList<BeaconWithSenz> readResult(JsonReader reader) throws IOException {
        String name, result = null;
        HashMap<String, Senz> senzesById = null;
        ArrayList<BeaconWithSenz> bwss = null;
        ArrayList<Pair<Beacon, String>> tmp = null;


        reader.beginObject();
        while (reader.hasNext()) {
            name = reader.nextName();
            switch (name) {
                case "result":
                    result = reader.nextString();
                    break;
                default:
                    reader.skipValue();
            }
        }
        reader.endObject();
        reader.close();

        if (result == null)
            throw new ResultNotPresentException();

        reader = new JsonReader(new StringReader(result));
        reader.beginObject();
        while (reader.hasNext()) {
            name = reader.nextName();
            switch (name) {
                case "senzes":
                    senzesById = readSenzHashMapFromJsonObject(reader);
                    break;
                case "beacons":
                    if (senzesById != null)
                        bwss = readBeaconWithSenzArrayListFromJsonArrayAndSenzById(reader, senzesById);
                    else
                        tmp = readBeaconSenzIdPairArrayListFromJsonArray(reader);
                    break;
                default:
                    reader.skipValue();
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


    public static ArrayList<BeaconWithSenz> queryBeacons(final Collection<Beacon> toQuery, final Location lastBeen) throws IOException {
        return doQuery(
                new URL(queryUrl + "beacons"),
                new QueryWriter() {
                    @Override
                    public void write(OutputStream os) throws IOException {
                        StringWriter sw = new StringWriter(100);
                        writeBeaconsQueryPost(new JsonWriter(sw), toQuery, lastBeen);
                        L.i(sw.toString());
                        writeBeaconsQueryPost(new JsonWriter(new OutputStreamWriter(os)), toQuery, lastBeen);
                    }
                },
                new ResultReader<ArrayList<BeaconWithSenz>>() {
                    @Override
                    public ArrayList<BeaconWithSenz> read(InputStream is) throws IOException {
                        return readResult(new JsonReader(new InputStreamReader(is)));
                    }
                });
    }

    public static ArrayList<BeaconWithSenz> queryLocation(final Location location) throws IOException {
        return doQuery(
                new URL(queryUrl + "beacons"),
                new QueryWriter() {
                    @Override
                    public void write(OutputStream os) throws IOException {
                        writeLocationQueryPost(new JsonWriter(new OutputStreamWriter(os)), location);
                    }
                },
                new ResultReader<ArrayList<BeaconWithSenz>>() {
                    @Override
                    public ArrayList<BeaconWithSenz> read(InputStream is) throws IOException {
                        return readResult(new JsonReader(new InputStreamReader(is)));
                    }
                });
    }

    public static class ResultNotPresentException extends IOException {
    }
}
