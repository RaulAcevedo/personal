package com.jjkeller.kmbapi.controller;

/**
 * Created by eth6134 on 6/8/16.
 */


import com.jjkeller.kmbapi.proxydata.ProxyBase;

public class KMBEncompassUser extends ProxyBase{

    private String UserId;
    private String LastName;
    private String FirstName;
    private String UserName;


    public String getUserId(){
        return this.UserId;
    }
    public void setUserId(String UserId){
        this.UserId = UserId;
    }

    public String getLastName(){
        return this.LastName;
    }
    public void setLastName(String LastName){
        this.LastName = LastName;
    }

    public String getFirstName(){
        return this.FirstName;
    }
    public void setFirstName(String FirstName){
        this.FirstName = FirstName;
    }

    public String getUserName(){
        return this.UserName;
    }
    public void setUserName(String UserName){
        this.UserName = UserName;
    }
}
