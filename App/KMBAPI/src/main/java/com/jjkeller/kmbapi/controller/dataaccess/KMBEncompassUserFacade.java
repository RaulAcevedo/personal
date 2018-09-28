package com.jjkeller.kmbapi.controller.dataaccess;

/**
 * Created by eth6134 on 6/8/16.
 */

import android.content.Context;

import com.jjkeller.kmbapi.controller.KMBEncompassUser;
import com.jjkeller.kmbapi.controller.dataaccess.db.KMBEncompassUserPersist;

public class KMBEncompassUserFacade extends FacadeBase {
    public KMBEncompassUserFacade(Context ctx)
    {
        super(ctx);
    }

    public KMBEncompassUser Fetch()
    {
        KMBEncompassUserPersist<KMBEncompassUser> persist = new KMBEncompassUserPersist(KMBEncompassUser.class, this.getContext());
        return persist.Fetch();
    }

    public void Save(KMBEncompassUser kmbEncompassUser)
    {
        KMBEncompassUserPersist<KMBEncompassUser> persist = new KMBEncompassUserPersist<KMBEncompassUser>(KMBEncompassUser.class, this.getContext());
        persist.Persist(kmbEncompassUser);
    }
}
