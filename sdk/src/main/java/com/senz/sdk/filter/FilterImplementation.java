package com.senz.sdk.filter;

import java.util.Set;

abstract class FilterImplementation {
    public void init(Context context, Object ...extra);
    public Set<Senz> filter(Set<Senz> senz);
}
