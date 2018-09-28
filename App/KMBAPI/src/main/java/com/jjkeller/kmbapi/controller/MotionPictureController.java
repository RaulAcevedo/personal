package com.jjkeller.kmbapi.controller;

import android.content.Context;

import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.jjkeller.kmbapi.R;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.dataaccess.MotionPictureAuthorityFacade;
import com.jjkeller.kmbapi.controller.dataaccess.MotionPictureProductionFacade;
import com.jjkeller.kmbapi.controller.share.ControllerBase;
import com.jjkeller.kmbapi.controller.share.KmbApplicationException;
import com.jjkeller.kmbapi.controller.utility.RESTWebServiceHelper;
import com.jjkeller.kmbapi.proxydata.MotionPictureAuthority;
import com.jjkeller.kmbapi.proxydata.MotionPictureProduction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by djm5973 on 10/10/2016.
 */

public class MotionPictureController extends ControllerBase {

    public MotionPictureController(Context ctx) {
        super(ctx);
    }

    public void DownloadMotionPictureAuthorities() throws KmbApplicationException
    {
        ArrayList<MotionPictureAuthority> motionPictureAuthorities = new ArrayList<>();

        try
        {
            RESTWebServiceHelper rwsh = new RESTWebServiceHelper(getContext());
            motionPictureAuthorities = rwsh.DownloadMotionPictureAuthorities();
        }
        catch (JsonSyntaxException jse)
        {
            this.HandleExceptionAndThrow(jse, this.getContext().getString(R.string.downloadmotionpictureauthorities), this.getContext().getString(R.string.exception_webservicecommerror), this.getContext().getString(R.string.exception_serviceunavailable));
        }
        catch (JsonParseException e)
        {
            // when connected to a network, but unable to get to webservice "e" is null at times
            if (e == null)
                e = new JsonParseException(JsonParseException.class.getName());
            this.HandleExceptionAndThrow(e, this.getContext().getString(R.string.downloadmotionpictureauthorities), this.getContext().getString(R.string.exception_webservicecommerror), this.getContext().getString(R.string.exception_serviceunavailable));
        }
        catch (IOException ioe)
        {
            this.HandleExceptionAndThrow(ioe, this.getContext().getString(R.string.downloadmotionpictureauthorities), this.getContext().getString(R.string.exception_webservicecommerror), this.getContext().getString(R.string.exception_serviceunavailable));
        }

        this.SaveMotionPictureAuthorities(motionPictureAuthorities);
    }

    public void DownloadMotionPictureProductions() throws KmbApplicationException
    {
        ArrayList<MotionPictureProduction> motionPictureProductions = new ArrayList<>();

        try
        {
            RESTWebServiceHelper rwsh = new RESTWebServiceHelper(getContext());
            motionPictureProductions = rwsh.DownloadMotionPictureProductions();
        }
        catch (JsonSyntaxException jse)
        {
            this.HandleExceptionAndThrow(jse, this.getContext().getString(R.string.downloadmotionpictureproductions), this.getContext().getString(R.string.exception_webservicecommerror), this.getContext().getString(R.string.exception_serviceunavailable));
        }
        catch (JsonParseException e)
        {
            // when connected to a network, but unable to get to webservice "e" is null at times
            if (e == null)
                e = new JsonParseException(JsonParseException.class.getName());
            this.HandleExceptionAndThrow(e, this.getContext().getString(R.string.downloadmotionpictureproductions), this.getContext().getString(R.string.exception_webservicecommerror), this.getContext().getString(R.string.exception_serviceunavailable));
        }
        catch (IOException ioe)
        {
            this.HandleExceptionAndThrow(ioe, this.getContext().getString(R.string.downloadmotionpictureproductions), this.getContext().getString(R.string.exception_webservicecommerror), this.getContext().getString(R.string.exception_serviceunavailable));
        }

        this.SaveMotionPictureProductions(motionPictureProductions);
    }

    private void SaveMotionPictureAuthorities(ArrayList<MotionPictureAuthority> motionPictureAuthorities)
    {
        MotionPictureAuthorityFacade facade = new MotionPictureAuthorityFacade(this.getContext(),
                GlobalState.getInstance().getCurrentUser());
        facade.Save(motionPictureAuthorities);
    }

    private void SaveMotionPictureProductions(ArrayList<MotionPictureProduction> motionPictureProductions)
    {
        MotionPictureProductionFacade facade = new MotionPictureProductionFacade(this.getContext(), GlobalState.getInstance().getCurrentUser());
        facade.Save(motionPictureProductions);
    }

    public List<MotionPictureProduction> GetActiveMotionPictureProductions() {
        if (this.getCurrentUser() == null) return new ArrayList<>();

        MotionPictureProductionFacade facade = new MotionPictureProductionFacade(this.getContext(),
                GlobalState.getInstance().getCurrentUser());
        List<MotionPictureProduction> productionList = facade.GetActiveProductions();
        return productionList;
    }

    public List<MotionPictureAuthority> GetActiveMotionPictureAuthorities() {
        if (this.getCurrentUser() == null) return new ArrayList<>();

        MotionPictureAuthorityFacade facade = new MotionPictureAuthorityFacade(this.getContext(),
                GlobalState.getInstance().getCurrentUser());
        List<MotionPictureAuthority> authorityList = facade.GetActiveAuthorities();
        return authorityList;
    }

    public MotionPictureAuthority GetAuthorityByAuthorityId(String motionPictureAuthorityId){
        if (this.getCurrentUser().equals(null)) return null;

        MotionPictureAuthorityFacade facade = new MotionPictureAuthorityFacade(this.getContext(),
                GlobalState.getInstance().getCurrentUser());
        MotionPictureAuthority authority = facade.GetAuthorityByAuthorityId(motionPictureAuthorityId);
        return authority;
    }

    public MotionPictureProduction GetProductionByProductionId(String motionPictureProductionId) {
        if (this.getCurrentUser().equals(null)) return null;

        MotionPictureProductionFacade facade = new MotionPictureProductionFacade(this.getContext(),
                GlobalState.getInstance().getCurrentUser());
        MotionPictureProduction production = facade.GetProductionByProductionId(motionPictureProductionId);
        return production;
    }
}
