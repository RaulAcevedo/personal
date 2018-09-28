package com.jjkeller.kmbapi.controller.interfaces;

import android.content.Context;

import com.jjkeller.kmbapi.controller.share.KmbApplicationException;

public interface IController {
    Context getContext();
    void HandleException(Exception ex);
    void HandleException(Exception ex, String tag);
    void HandleExceptionAndThrow(Exception ex, String tag, String caption, String displayMessage) throws KmbApplicationException;
}
