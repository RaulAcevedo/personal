package com.jjkeller.kmb.interfaces;

import android.os.Bundle;


public interface IBaseFragment {
    public interface BaseFragmentActions {
        public abstract void setFragments();
        public abstract void handleActivityStart(Class<?> c, Bundle extras);
    }
}
