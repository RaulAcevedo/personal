package com.jjkeller.kmbapi.controller.utility;

import com.jjkeller.kmbapi.proxydata.TeamDriver;

import java.util.Comparator;
import java.util.Date;

public class TeamDriverComparer implements Comparator<TeamDriver>
	{

	private String _sortDirection = "Ascending";

	public TeamDriverComparer(String direction) {
		_sortDirection = direction;
	}
	
    /// <summary>
    /// Compare two team drivers, by using the start time of each as the comparison
    /// </summary>
    /// <param name="a"></param>
    /// <param name="b"></param>
    /// <returns>
    /// 0  : both have the same date 
    /// -1 : if a before b 
    /// 1  : if a after b
    /// </returns>
	public int compare(TeamDriver a, TeamDriver b) 
	{
		int answer = 0;
		
		Date firstDate = a.getStartTime();
		Date secondDate = b.getStartTime();
		answer = firstDate.compareTo(secondDate);		

		if(_sortDirection.compareTo("Descending") == 0)
		{
			// reverse the answer when sorting descending
			answer = -1 * answer;
		}
		return answer;
	}
	
}
