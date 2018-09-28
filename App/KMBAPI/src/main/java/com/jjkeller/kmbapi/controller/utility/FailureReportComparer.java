package com.jjkeller.kmbapi.controller.utility;

import com.jjkeller.kmbapi.proxydata.FailureReport;

import org.joda.time.DateTime;

import java.util.Comparator;
import java.util.Date;

public class FailureReportComparer implements Comparator<FailureReport>
{
	private String _sortDirection = "Ascending";

	public FailureReportComparer(String direction) 
	{
		_sortDirection = direction;
	}
	
    /// <summary>
    /// Compare two failure events, by using the start time of each as the comparison
    /// </summary>
    /// <param name="a"></param>
    /// <param name="b"></param>
    /// <returns>
    /// 0  : both events have the same date 
    /// -1 : if a before b 
    /// 1  : if a after b
    /// </returns>
    public int compare(FailureReport a, FailureReport b)
    {
    	int answer = 0;
    	
        DateTime firstDate = a.getStartTime();
        DateTime secondDate = b.getStartTime();
        answer = firstDate.compareTo(secondDate);

		if(_sortDirection.compareTo("Descending") == 0)
		{
			// reverse the answer when sorting descending
			answer = -1 * answer;
		}
		return answer;
		
    }
}
