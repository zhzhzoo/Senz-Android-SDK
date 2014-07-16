package com.senz.sdk.contezt;

import android.os.Parcelable;
import com.avos.avoscloud.AVObject;
import com.senz.sdk.contezt.Contezt;

final class PackageContezt extends Contezt implements Parcelable {
    String mPackageName;

    protected PackageContezt(AVObject avo) {
        this.mPackageName = avo.getString("packageName");
    }

    protected PackageContezt(Parcel in) {
        this.mPackageName = in.readString();
    }

    protected AVObject toAVObject() {
        AVObject avo = new AVObject("contezt");
        avo.putString("what", this.what());
        avo.putString("packageName", mPackageName);
        return avo;
    }

    protected void writeToParcelRemaining(Parcel out, int flags) {
        out.writeString(mPackageName);
    }

    public static String what() {
        return "package";
    }

    public String getPackageName() {
        return mPackageName;
    }
}
