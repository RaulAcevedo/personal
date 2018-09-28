package com.jjkeller.kmbapi.controller.dataaccess;

import android.content.Context;

import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.dataaccess.db.FeatureTogglePersist;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.proxydata.FeatureToggle;

import java.util.List;

public class FeatureToggleFacade extends FacadeBase {

    public FeatureToggleFacade(Context ctx, User user){ super(ctx, user);}

    public List<FeatureToggle> Fetch(){
        FeatureTogglePersist<FeatureToggle> persist = new FeatureTogglePersist<FeatureToggle>(
                FeatureToggle.class, this.getContext());

        return persist.FetchList();
    }

    public void DeleteAll(){
        List<FeatureToggle> allRecords = Fetch();

        FeatureTogglePersist<FeatureToggle> persist = new FeatureTogglePersist<FeatureToggle>(
                FeatureToggle.class, this.getContext());

        for(FeatureToggle toggle : allRecords){
            persist.Delete(toggle);
        }
    }

    public void Save(List<FeatureToggle> featureToggleList){
        FeatureTogglePersist<FeatureToggle> persist = new FeatureTogglePersist<>(FeatureToggle.class, this.getContext());

        for (FeatureToggle ft : featureToggleList)
            persist.Persist(ft);
    }
}
