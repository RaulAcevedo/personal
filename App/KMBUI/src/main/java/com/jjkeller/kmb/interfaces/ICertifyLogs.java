package com.jjkeller.kmb.interfaces;

import java.util.Date;
import java.util.List;

public interface ICertifyLogs
{
    public interface CertifyLogsFragActions {
        public void handleSubmitButtonClick();
        public void handleCancelButtonClick();
    }
	public interface CertifyLogsControllerMethods
	{
		/**
		 * Gets all log dates that either have no ELD events or the last ELD event is not a certification event.
		 * If none exist, an empty list is returned.
		 * @return A list of log dates that have not been certified. If none exist, an empty list is returned.
		 */
		public List<Date> getUncertifiedLogDates();
		public List<Date> getCertifiedUnsubmittedLogDates();
	}
}
