

package com.jjkeller.kmbapi.controller.dataaccess;

import android.content.Context;

import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.dataaccess.db.MotionPictureAuthorityPersist;
import com.jjkeller.kmbapi.controller.share.User;
import com.jjkeller.kmbapi.proxydata.MotionPictureAuthority;
import java.util.List;

/**
 * Created by tgrayeb on 9/28/2016.
 */
public class MotionPictureAuthorityFacade extends FacadeBase {

    public MotionPictureAuthorityFacade(Context ctx, User user) {
        super(ctx, user);
    }

    public List<MotionPictureAuthority> Fetch()
    {
        MotionPictureAuthorityPersist<MotionPictureAuthority> persist = new MotionPictureAuthorityPersist<>(MotionPictureAuthority.class,
                this.getContext(), GlobalState.getInstance().getCurrentUser());
        return persist.FetchList();
    }

    public void Save(List<MotionPictureAuthority> list)
    {
        MotionPictureAuthorityPersist<MotionPictureAuthority> persist = new MotionPictureAuthorityPersist<>(MotionPictureAuthority.class,
                this.getContext(), GlobalState.getInstance().getCurrentUser());

        for(MotionPictureAuthority auth : list)
            persist.Persist(auth);
    }

    public void Save(MotionPictureAuthority motionPictureAuthorityData)
    {
        MotionPictureAuthorityPersist<MotionPictureAuthority> persist = new MotionPictureAuthorityPersist<>(MotionPictureAuthority.class,
                this.getContext(), GlobalState.getInstance().getCurrentUser());
        persist.Persist(motionPictureAuthorityData);
    }

    public List<MotionPictureAuthority> GetActiveAuthorities()
    {
        MotionPictureAuthorityPersist<MotionPictureAuthority> persist = new MotionPictureAuthorityPersist<>(MotionPictureAuthority.class,
                this.getContext(), GlobalState.getInstance().getCurrentUser());
        return persist.FetchActiveAuthorities();
    }

    public MotionPictureAuthority GetAuthorityByAuthorityId(String motionPictureAuthorityId) {
        MotionPictureAuthorityPersist<MotionPictureAuthority> persist = new MotionPictureAuthorityPersist<>(MotionPictureAuthority.class,
                this.getContext(), GlobalState.getInstance().getCurrentUser());
        return persist.FetchAuthorityByAuthorityId(motionPictureAuthorityId);
    }
}
