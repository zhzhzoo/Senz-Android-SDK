package com.senz.sdk.avos;

import com.avoscloud.AVObject;
import com.avoscloud.AVQuery;
import com.senz.sdk.Beacon;
import com.senz.sdk.Contezt;

public class AVUtils {
    public static void initAVOS() {
    }

    public static void queryContezt(Beacon beacon, QueryCompleteCallback cb) {
        queryBeacon(beacon, new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> results, AVException e) {
                if (e != null) {
                    L.wtf("avos query error!", e);
                    cb.onComplete(null);
                }
                else if (results.size() == 0)
                    cb.onComplete(null);
                else {
                    AVObject avoBeacon = results[0];
                    avoBeacon.getAVObject("contezt").fetchIfNeededInBackground(
                        new GetCallback<AVObject>() {
                            @Override
                            public void done(AVObject avo, AVException e) {
                                if (e != null) {
                                    L.wtf("avos query error!", e);
                                    cb.onComplete(null);
                                }
                                else
                                    cb.onComplete(Contezt.fromAVObject(avo));
                            }
                        }
                    );
                }
            }
        });
    }

    public static void queryBeacon(Beacon beacon, FindCallback<AVObject> cb) {
        AVQuery<AVObject> query = new AVQuery<AVObject>("Beacon");
        query.whereEqualTo("UUID", beacon.getUUID().toString());
        query.whereEqualTo("MAC", beacon.getMAC());
        query.whereEqualTo("Major", beacon.getMajor());
        query.whereEqualTo("Minor", beacon.getMinor());
        query.findInBackground(cb);
    }

    public interface QueryCompleteCallback() {
        public void onComplete(AVObject avo);
    }
}
