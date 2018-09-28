package com.jjkeller.kmb.share;

import android.content.Context;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.BulletSpan;
import android.widget.TextView;

public class BulletedListTextFormatter {
	
	// Create a bulleted list passing in each individual bullet point
	// indenting is included
	public static void createBulletedList(Context c, TextView textViewController, int strResource) {
		CharSequence t = c.getString(strResource);
		SpannableString s = new SpannableString(t);
		s.setSpan(new BulletSpan(15), 0, t.length(), 0);
		textViewController.setText(TextUtils.concat(s));
	}
}
