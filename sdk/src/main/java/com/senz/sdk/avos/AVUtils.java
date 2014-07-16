package com.senz.sdk.avos;

import java.util.List;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.AVException;
import com.avos.avoscloud.GetCallback;
import com.avos.avoscloud.FindCallback;
import com.senz.sdk.Beacon;
import com.senz.sdk.contezt.Contezt;
import com.senz.sdk.utils.L;

public class AVUtils {
    public static void initAVOS() {
    }

    public static void queryContezt(final Beacon beacon, final QueryConteztCompleteCallback cb) {
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
                    AVObject avoBeacon = results.get(0);
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

    public interface QueryConteztCompleteCallback {
        public void onComplete(Contezt o);
    }
}
