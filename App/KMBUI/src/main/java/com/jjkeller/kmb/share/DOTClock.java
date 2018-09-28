package com.jjkeller.kmb.share;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.ArcShape;
import android.graphics.drawable.shapes.OvalShape;
import android.util.AttributeSet;
import android.view.View;

public class DOTClock extends View
{
	private ShapeDrawable _drawable;
	private ShapeDrawable _drawable_ResetBreak;	
	private ShapeDrawable _outline;
	//private ShapeDrawable _outline_ResetBreak;

	public DOTClock(Context context)
	{
		super(context);
		createClock();
	}

	public DOTClock(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		createClock();
	}

	private void createClock()
	{
		_drawable = new ShapeDrawable(new ArcShape(0f, 0f));
		_drawable_ResetBreak = new ShapeDrawable(new ArcShape(0f, 0f));
		//_outline_ResetBreak = new ShapeDrawable(new ArcShape(0f, 0f));
		
		
		_outline = new ShapeDrawable(new OvalShape());				
		_outline.getPaint().setColor(Color.BLACK);
		_outline.getPaint().setStrokeWidth(3.0f);		
		_outline.getPaint().setStyle(Paint.Style.STROKE);
		
	}
	
	public void changeOutlineColor(int color){
		_outline.getPaint().setColor(color);
	}

	/**
	 * Updates the percentage the clock should display. This must be called from
	 * a UI thread. The percentage should be between 0.0 and 1.0, e.g. 0.75 for 75%.
	 * 
	 * @param percentage The percentage of the clock to fill in fraction form, e.g. 0.75 for 75%
	 */
	public void setPercentage(double percentage, boolean eightHourClock, double eightHourPercentage) {
		if (percentage < 0.0)
			percentage = 0.0;

		if (eightHourPercentage < 0.0)
			eightHourPercentage = 0.0;

		// If zero, show a full red circle. Otherwise, draw based on percentage.
		float degrees = (percentage == 0.0) ? 360f : (float) (360f * percentage);
		_drawable.setShape(new ArcShape(270f, degrees));
		if (percentage == 0.0 && eightHourPercentage == 0.0 & eightHourClock) {
			// Either the Rest Break or Driving has 0 avail hours
			_drawable.getPaint().setColor(Color.RED);
		} else if (percentage <= 0.25 && eightHourPercentage <= 0.25 && eightHourClock) {
			// Rest Break Clock and Driving Clock are both under 25%
			_drawable.getPaint().setColor(0xff74AC23);
		} else {
			// Color the Driving Clock as normal
			_drawable.getPaint().setColor(getColorForPercentage(percentage, false));
		}

		// Handle the Rest Break Clock
		if (eightHourClock && eightHourPercentage > 0.0) {
			float degrees_ResetBreak = (eightHourPercentage == 0.0) ? 262f : (float) (262f * eightHourPercentage);
			_drawable_ResetBreak.setShape(new ArcShape(270f, degrees_ResetBreak));
			_drawable_ResetBreak.getPaint().setColor(getColorForPercentage(eightHourPercentage, true));

			/*_outline_ResetBreak.setShape(new ArcShape(270f, degrees_ResetBreak));
			_outline_ResetBreak.getPaint().setColor(Color.BLACK);
			_outline_ResetBreak.getPaint().setStrokeWidth(1.0f);
			_outline_ResetBreak.getPaint().setStyle(Paint.Style.STROKE);*/
		}

		invalidate();
	}
	
	public void setNotApplicable(int color){
		// 100% Circle of a solid color
		float degrees = 360f;
		_drawable.setShape(new ArcShape(270f, degrees));
		_drawable.getPaint().setColor(color);
		invalidate();
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		resizeClock();
		
		// Create the layering for the clocks
		Drawable []dr = new Drawable[3];
		dr[0] = _drawable;
		dr[1] = _drawable_ResetBreak;	
		//dr[2] = _outline_ResetBreak;
		dr[2] = _outline;
		LayerDrawable ld = new LayerDrawable(dr);
		ld.draw(canvas);
	}

	/**
	 * Resizes the clock to be a perfect circle in the center of the view
	 */
	private void resizeClock()
	{
		int size;
		int x;
		int y;
		int width = getWidth();
		int height = getHeight();
		if (width > height)
		{
			size = height;
			x = (width - height) / 2;
			y = 0;
		}
		else
		{
			size = width;
			x = 0;
			y = (height - width) / 2;
		}

		_drawable.setBounds(x, y, x + size, y + size);
		_drawable_ResetBreak.setBounds(x, y, x + size, y + size);
		//_outline_ResetBreak.setBounds(x, y, x + size, y + size);
		_outline.setBounds(x+1, y+1, x + size-1, y + size-1);
	}

	private int getColorForPercentage(double percentage, boolean eightHourClock) {
		// Handle the 8 Hour clock
		if (eightHourClock) {
			if (percentage > 0.25) // Greater than 25%
			{
				return android.graphics.Color.parseColor("#1e6697");
			}				
			else if (percentage > 0.0) // Between 0% and 25%, exclusively
				return Color.YELLOW;
			return Color.TRANSPARENT; // 0%
		} else {

			// Handle the normal 11 Hour Clock
			if (percentage > 0.25) // Greater than 25%
				return 0xff74AC23;
			else if (percentage > 0.0) // Between 0% and 25%, exclusively
				return Color.YELLOW;
			return Color.RED; // 0%
		}
	}
}
