package com.jjkeller.kmbapi.kmbeobr;

import android.os.Bundle;

/**
 * Created by ief5781 on 9/9/16.
 */
public class BundleBuilder {

    private Bundle bundle;

    private BundleBuilder() { this(new Bundle()); }
    private BundleBuilder(Bundle bundle) { this.bundle = bundle; }

    public static BundleBuilder empty() {
        return new BundleBuilder();
    }

    public static BundleBuilder withBundle(Bundle bundle) {
        return new BundleBuilder(bundle);
    }

    public static BundleBuilder withReturnCode(int returnCode) {
        BundleBuilder builder = new BundleBuilder();
        builder.withValue(Constants.RETURNCODE, returnCode);

        return builder;
    }

    public BundleBuilder withReturnValue(int value) {
        this.withValue(Constants.RETURNVALUE, value);

        return this;
    }

    public BundleBuilder withReturnValue(float value) {
        this.withValue(Constants.RETURNVALUE, value);

        return this;
    }

    public BundleBuilder withReturnValue(String value) {
        this.withValue(Constants.RETURNVALUE, value);

        return this;
    }

    public BundleBuilder withReturnValue(long value) {
        this.withValue(Constants.RETURNVALUE, value);

        return this;
    }

    public BundleBuilder withReturnValue(boolean value) {
        this.withValue(Constants.RETURNVALUE, value);

        return this;
    }

    public BundleBuilder withValue(String key, int value) {
        bundle.putInt(key, value);

        return this;
    }

    public BundleBuilder withValue(String key, float value) {
        bundle.putFloat(key, value);

        return this;
    }

    public BundleBuilder withValue(String key, String value) {
        bundle.putString(key, value);

        return this;
    }

    public BundleBuilder withValue(String key, long value) {
        bundle.putLong(key, value);

        return this;
    }

    public BundleBuilder withValue(String key, boolean value) {
        bundle.putBoolean(key, value);

        return this;
    }

    public Bundle build() {
        return bundle;
    }
}
