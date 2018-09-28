package com.jjkeller.kmb.interfaces;

import com.jjkeller.kmbapi.controller.interfaces.IAPIController;

import java.util.Date;

public interface INavButtonsFrag {
    public interface NavButtonsControllerMethods{

        public String getNavButtonsTitle();
        public void setNavButtonsTitle(String value);

        public int getCurrentItemIndex();
        public int getTotalItemCount();

        public void handleBtnPrevious();
        public void handleBtnNext();
    }
}
