package com.jjkeller.kmbapi.configuration;

import com.jjkeller.kmbapi.proxydata.LocationCode;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class LocationCodeDictionary {

    private Hashtable<String, LocationCode> _dict = new Hashtable<String, LocationCode>();

    public LocationCodeDictionary(List<LocationCode> locationCodeList)
    {
        if (locationCodeList != null)
        {
        	for(LocationCode locCode:locationCodeList)
            {
                String key = locCode.getCode().toUpperCase();
                if (!_dict.containsKey(key))
                {
                    _dict.put(key, locCode);
                }
            }
        }
    }
    
    /// <summary>
    /// Answer the full location string, given the code for the location.
    /// If not found, then return null.
    /// </summary>
    /// <param name="codeKey">code for the location</param>
    /// <returns>location if found, otherwise null</returns>
    public String LocationFromCode(String codeKey)
    {
    	String answer = null;
        String key = codeKey.toUpperCase();
        if (_dict.containsKey(key))
        {
            answer = _dict.get(key).getLocation();
        }
        return answer;
    }

    /// <summary>
    /// Answer a list of every location code in the dictionary
    /// </summary>
    /// <returns></returns>
    public ArrayList<LocationCode> AllCodes()
    {
    	return new ArrayList<LocationCode>(_dict.values());
    }

    /// <summary>
    /// Answer if the dictionary is empty.
    /// </summary>
    /// <returns></returns>
    public boolean IsEmpty()
    {
        boolean isEmpty = true;
        if (_dict != null && _dict.size() > 0)
        {
            isEmpty = false;
        }
        return isEmpty;
    }

    public int getCount()
    {
        if (_dict != null && _dict.size() > 0)
        {
            return _dict.size();
        }
        else
            return 0;
    }
}
