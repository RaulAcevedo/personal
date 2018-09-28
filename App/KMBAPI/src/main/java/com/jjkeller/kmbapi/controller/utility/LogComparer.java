package com.jjkeller.kmbapi.controller.utility;

import com.jjkeller.kmbapi.proxydata.EmployeeLog;

import java.util.Comparator;
import java.util.Date;


public class LogComparer implements Comparator<EmployeeLog> 
{
	private String _sortDirection = "Ascending";

	public LogComparer(String direction) {
		_sortDirection = direction;
	}
	
    /// <summary>
    /// Compare two logs, by using the logDate of each as the comparison.
    /// When sorting ascending compare the first log to the second log in
    /// the normal way.    When comparing descending, the answer is reversed.
    /// </summary>
    /// <param name="a"></param>
    /// <param name="b"></param>
    /// <returns>
    /// 0  : both logs have the same log date 
    /// -1 : when ascending: if a before b...when descending b is before a
    /// 1  : when ascending: if a after b...when descending b is after a
    /// </returns>
	public int compare(EmployeeLog a, EmployeeLog b) 
	{
		int answer = 0;
		
		Date firstLogDate = a.getLogDate();
		Date secondLogDate = b.getLogDate();
		answer = firstLogDate.compareTo(secondLogDate);		

		if(_sortDirection.compareTo("Descending") == 0)
		{
			// reverse the answer when sorting descending
			answer = -1 * answer;
		}
		return answer;
	}
	
}
