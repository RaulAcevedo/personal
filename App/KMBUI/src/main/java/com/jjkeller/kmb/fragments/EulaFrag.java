package com.jjkeller.kmb.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ScrollView;
import android.widget.TextView;

import com.jjkeller.kmb.Eula;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbapi.controller.utility.FileUtility;
import com.jjkeller.kmbui.R;

/**
 * Created by RDelgado on 5/19/2017.
 * Eula fragment class which shows scrollView with the EULA text.
 */

public class EulaFrag extends BaseFragment {

	private String eula;
	private TextView _tvEula;
	private TextView _tvEulaTittle;
	private ScrollView _svEula;
	Boolean lockScrollUpdate = false;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.f_eula, container, false);
		findControls(v);
		if (savedInstanceState != null && savedInstanceState.containsKey("eula")){
			eula = savedInstanceState.getString("eula");
		}else{
			//Get the Eula raw resource from eula.txt to be displayed
			eula = FileUtility.InputStreamToString(this.getResources().openRawResource(R.raw.eula));
		}
		return v;
	}

	protected void findControls(View v)
	{
		_svEula = (ScrollView) v.findViewById(R.id.svEula);
		_tvEulaTittle = (TextView)v.findViewById((R.id.tvEulaTittle));
		_tvEula = (TextView)v.findViewById((R.id.tvEula));
	}

	protected void loadControls()
	{
		_tvEula.setText(eula);
		_svEula.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
			@Override
			public void onScrollChanged() {
				//Update the LeftNavFragment highlight only if the LockScrollUpdate is not
				// locked meaning that this is an scroll movement by user input
				if(!lockScrollUpdate){
					updateHighlightSelectedItem(_svEula.getScrollY());
				}
			}
		});


		_svEula.setOnTouchListener(new View.OnTouchListener() {
			@Override
			public boolean onTouch(View view, MotionEvent motionEvent) {
				//if the user touched the scrollview to scroll unlock the LockScrollUpdate
				if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
					setLockScrollUpdate(false);
				}
				return false;
			}
		});
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		loadControls();
	}

	/**
	 * Move automatically the eula scrollview to an EulaSection position
	 * @param sectionStartPosition Position of an EulaSection in the scrollview
	 */
	public void moveScrollViewTo(int sectionStartPosition){
		//lock the LockScrollUpdate this is an automatic movement by touch on the LeftNavFragment
		// item
		setLockScrollUpdate(true);
		//get the number line that contains the sectionStartPosition and multiply it by the line
		//height to calculate the y scroll position to move
		int y = getLineForOffsetEula(sectionStartPosition)  * _tvEula.getLineHeight();
		//move scroll to y adding the Height of the eulaTittle to compensate for the textbox at the
		//beginning of the scrollview.
		_svEula.smoothScrollTo (0, y + _tvEulaTittle.getMeasuredHeight());
	}

	/**
	 * Update the LeftNavFragment item highlight
	 * @param scrollY Position in y of the eula scrollView
	 */
	public void updateHighlightSelectedItem(int scrollY){
		//Stop the propagation of the infinite auto scroll at the beginning of the scrollview
		if(scrollY!=0){
			//get the character position of the section in the scrollview
			int yPosition = scrollY / _tvEula.getLineHeight();
			((Eula)getActivity()).updateHighlightSelectedItem(yPosition);
		}
	}

	/**
	 * Obtain the number line of a EulaSection position
	 * @param sectionStartPosition First character position of EulaSection
	 * @return line number in Eula scrollview
	 */
	public int getLineForOffsetEula(int sectionStartPosition){
		return _tvEula.getLayout().getLineForOffset(sectionStartPosition);
	}

	/**
	 * Lock the updateHighlightSelectedItem depending on if this is an automatic automatic movement
	 * by touch on the LeftNavFragment or an user input movement to prevent collision
	 * @param lockScrollUpdate Lock flag
	 */
	public void setLockScrollUpdate(Boolean lockScrollUpdate){
		this.lockScrollUpdate = lockScrollUpdate;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString("eula", eula);
		super.onSaveInstanceState(outState);
	}
}


