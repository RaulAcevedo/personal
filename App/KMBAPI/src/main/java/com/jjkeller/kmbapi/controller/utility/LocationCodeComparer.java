package com.jjkeller.kmbapi.controller.utility;

import com.jjkeller.kmbapi.proxydata.LocationCode;

import java.util.Comparator;

public class LocationCodeComparer implements Comparator<LocationCode> {

	private String _sortDirection = "Ascending";
	
	public LocationCodeComparer(String direction) {
		_sortDirection = direction;
	}
	
    /// <summary>
    /// Compare two Location Codes, by using the code of each as the comparison.
    /// When sorting ascending compare the first code to the second code in
    /// the normal way.    When comparing descending, the answer is reversed.
    /// </summary>
    /// <param name="a"></param>
    /// <param name="b"></param>
    /// <returns>
    /// 0  : both logs have the same log date 
    /// -1 : when ascending: if a before b...when descending b is before a
    /// 1  : when ascending: if a after b...when descending b is after a
    /// </returns>
	public int compare(LocationCode a, LocationCode b) {
		int answer = 0;
		
		String firstCode = a.getCode();
		String secondCode = b.getCode();
		answer = firstCode.compareTo(secondCode);		

		if(_sortDirection.compareTo("Descending") == 0)
		{
			// reverse the answer when sorting descending
			answer = -1 * answer;
		}
		return answer;
	}
	
}
