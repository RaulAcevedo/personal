package com.jjkeller.kmbapi.controller.dataaccess;

import android.content.Context;

import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.dataaccess.db.MotionPictureProductionPersist;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.proxydata.MotionPictureProduction;

import java.util.List;

/**
 * Created by gav6058 on 9/29/2016.
 */
public class MotionPictureProductionFacade extends FacadeBase {

    public MotionPictureProductionFacade(Context ctx, User user) {
        super(ctx, user);
    }

    public List<MotionPictureProduction> Fetch()
    {
        MotionPictureProductionPersist<MotionPictureProduction> persist = new MotionPictureProductionPersist<>(MotionPictureProduction.class,
                this.getContext(), this.getCurrentUser());
        return persist.FetchList();
    }

    public void Save(List<MotionPictureProduction> list) {
        MotionPictureProductionPersist<MotionPictureProduction> persist = new MotionPictureProductionPersist<MotionPictureProduction>(MotionPictureProduction.class, this.getContext(), this.getCurrentUser());

        for (MotionPictureProduction prod : list)
            persist.Persist(prod);
    }

    public void Save(MotionPictureProduction productionData)
    {
        MotionPictureProductionPersist<MotionPictureProduction> persist = new MotionPictureProductionPersist<MotionPictureProduction>(MotionPictureProduction.class, this.getContext(), this.getCurrentUser());
        persist.Persist(productionData);
    }

    public List<MotionPictureProduction> GetActiveProductions()
    {
        MotionPictureProductionPersist<MotionPictureProduction> persist = new MotionPictureProductionPersist<>(MotionPictureProduction.class,
                this.getContext(), GlobalState.getInstance().getCurrentUser());
        return persist.FetchActiveProductions();
    }

    public MotionPictureProduction GetProductionByProductionId(String motionPictureProductionId) {
        MotionPictureProductionPersist<MotionPictureProduction> persist = new MotionPictureProductionPersist<>(MotionPictureProduction.class,
                this.getContext(), GlobalState.getInstance().getCurrentUser());
        return persist.FetchProductionByProductionId(motionPictureProductionId);
    }
}
