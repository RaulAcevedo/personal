package com.jjkeller.kmb.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.alk.cpik.Copilot;

public class AlkCopilotFrag extends Fragment
{
	private boolean isAlreadyRemovedFromParent = false;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
		super.onCreateView(inflater, container, savedInstanceState);
		
		// Get and return the view
		View v = Copilot.getView();
		v.requestFocus();
		return v;
	}
	
	public void removeViewFromParent()
	{
		if (!isAlreadyRemovedFromParent)
		{
			isAlreadyRemovedFromParent = true;
			
			View v = Copilot.getView();

			// remove from parent in order to use again
			ViewGroup parent = (ViewGroup) v.getParent();
			if (parent != null)
			{
				parent.removeView(v);
			}
		}
	}

	@Override
	public void onDestroyView()
	{
		super.onDestroyView();
		removeViewFromParent();
	}
}
