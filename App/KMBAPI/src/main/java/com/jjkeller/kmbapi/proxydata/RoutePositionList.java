package com.jjkeller.kmbapi.proxydata;

import java.util.List;



/**
 * Created by aaz3239 on 4/12/17.
 */

public class RoutePositionList extends ProxyBase {

    private String eobrSerialNumber = null;
    private RoutePosition[] routePositions;

    public RoutePositionList(){

    }

    public RoutePositionList(List<RoutePosition> list){
        this.setRoutePositions((RoutePosition[])list.toArray());
    }

    public String getEobrSerialNumber() {
        return eobrSerialNumber;
    }
    public void setEobrSerialNumber(String eobrSerialNumber) {
        this.eobrSerialNumber = eobrSerialNumber;
    }

    public RoutePosition[] getRoutePositions(){
        return this.routePositions;
    }
    public void setRoutePositions(RoutePosition[] routePositions){
        this.routePositions = routePositions;
    }
}
