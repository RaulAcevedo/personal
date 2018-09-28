package com.jjkeller.kmb.share;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class CustomProgressDialog extends ProgressDialog {

    private int progressPercentVisibility = View.VISIBLE;
    private int progressNumberVisibility = View.VISIBLE;

    public CustomProgressDialog(Context context, int progressPercentVisibility, int progressNumberVisibility) {
        super(context);

        this.progressPercentVisibility = progressPercentVisibility;
        this.progressNumberVisibility = progressNumberVisibility;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setFieldVisibility("mProgressPercent", progressPercentVisibility);
        setFieldVisibility("mProgressNumber", progressNumberVisibility);
    }

    private void setFieldVisibility(String fieldName, int visibility) {
        try {
            Method method = TextView.class.getMethod("setVisibility", Integer.TYPE);

            if(method != null)
            {
	            Field[] fields = this.getClass().getSuperclass()
	                    .getDeclaredFields();
	
	            for (Field field : fields) {
	                if (field.getName().equalsIgnoreCase(fieldName)) {
	                    field.setAccessible(true);
	                    TextView textView = (TextView) field.get(this);
	                    method.invoke(textView, visibility);
	                }
	            }
            }
        } catch (Exception e) {}
    }
}
