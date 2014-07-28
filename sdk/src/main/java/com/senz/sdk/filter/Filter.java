package com.senz.sdk.filter;

import android.content.Context;
import com.senz.sdk.filter.ImplementationList;

class Filter {
    Context mContext;
    FilterImplementation mFilterImpls[];

    Filter(Context context) {
        this.mContext = context;
        mFilterImpls = new FilterImplementation[ImplementationList.classes.length];
        for (int i = 0; i < ImplementationList.classes.length; i++) {
            mFilterImpls[i] = ImplementationList.classes[i].newInstance();
            mFilterImpls[i].init(Context);
        }
    }

    public ArrayList<Senz> filter(ArrayList<Senz> senzes) {
        HashSet<Senz> orig = new HashSet<Senz>(senzes);
        HashSet<Senz> res = (HashSet<Senz>) orig.clone();

        for (FilterImplementation f : mFilterImpls)
            res.retainAll(f.filter(orig));

        return new ArrayList<Senz>(res);
    }
}
