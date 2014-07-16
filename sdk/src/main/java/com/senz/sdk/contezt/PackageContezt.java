package com.senz.sdk.contezt;

import android.os.Parcelable;
import android.os.Parcel;
import com.avos.avoscloud.AVObject;
import com.senz.sdk.contezt.Contezt;

final class PackageContezt extends Contezt {
    String mPackageName;

    protected PackageContezt(AVObject avo) {
        this.mPackageName = avo.getString("packageName");
    }

    protected PackageContezt(Parcel in) {
        this.mPackageName = in.readString();
    }

    protected AVObject toAVObject() {
        AVObject avo = new AVObject("contezt");
        avo.put("what", this.what());
        avo.put("packageName", mPackageName);
        return avo;
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
}
