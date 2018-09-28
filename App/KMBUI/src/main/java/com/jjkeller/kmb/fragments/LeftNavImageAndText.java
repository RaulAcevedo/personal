package com.jjkeller.kmb.fragments;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.jjkeller.kmbui.R;

public class LeftNavImageAndText extends ArrayAdapter<String>{
	private final Activity context;
	private final String[] web;
	private final Integer[] imageId;
	
	public LeftNavImageAndText(Activity context,

	String[] web, Integer[] imageId) {
		super(context, R.layout.leftnav_item_imageandtext, web);
		this.context = context;
		this.web = web;
		this.imageId = imageId;
	}

	
	@Override
	public View getView(int position, View view, ViewGroup parent) {
		LayoutInflater inflater = context.getLayoutInflater();
		View rowView= inflater.inflate(R.layout.leftnav_item_imageandtext, null, true);
		TextView txtTitle = (TextView) rowView.findViewById(R.id.text);
		ImageView imageView = (ImageView) rowView.findViewById(R.id.image); 
		txtTitle.setText(web[position]);
		imageView.setImageResource(imageId[position]);
		return rowView;
	}
	
}