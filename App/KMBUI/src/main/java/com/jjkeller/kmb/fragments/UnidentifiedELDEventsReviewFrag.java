package com.jjkeller.kmb.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.jjkeller.kmb.UnidentifiedELDEventsReview;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbui.R;

public class UnidentifiedELDEventsReviewFrag extends BaseFragment
{
	private static final String TAG = "UnidentifiedELDEventsReviewFrag Activity";

	private ExpandableListView expListEventsToReview;



	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
	{
        // Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.f_unidentifiedeldevents_review, container, false);
		findControls(v);
		return v;
	}

	protected void findControls(View v)
	{
		// find the expandablelistview
		expListEventsToReview = (ExpandableListView) v.findViewById(R.id.listEventsToReview);

		// set text elements
        TextView txtHeader = (TextView) v.findViewById(R.id.txtHeader);
		txtHeader.setText(getString(R.string.unidentified_events_review_header, getActivity().getIntent().getStringExtra(UnidentifiedELDEventsReview.BUNDLE_REVIEW_HEADER)));
        TextView txtInstructions = (TextView) v.findViewById(R.id.txtInstructions);
		txtInstructions.setText(getString(R.string.unidentified_events_review_instructions, getActivity().getIntent().getStringExtra(UnidentifiedELDEventsReview.BUNDLE_REVIEW_INSTRUCTIONS)));
	}

	public ExpandableListView getEventsListView()
	{
		return expListEventsToReview;
	}

}