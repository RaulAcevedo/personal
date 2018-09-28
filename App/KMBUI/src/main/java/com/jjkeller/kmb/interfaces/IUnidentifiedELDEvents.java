package com.jjkeller.kmb.interfaces;

import android.view.View;
import android.widget.AdapterView;

public interface IUnidentifiedELDEvents
{
	
	public interface UnidentifiedELDEventsFragActions
	{
		public void handleClaimButtonClicked(View v);
		public void handleSelectAllClicked(View view, boolean checked);
	}
}
