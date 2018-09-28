package com.jjkeller.kmb;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.actionbarsherlock.view.Menu;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmbui.R;

public class OffDutyConfirmation extends BaseActivity {

	String _missingLogDates;
	int _missingLogCount;
	TextView _odclblmessage;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.offdutyconfirmation);

		_missingLogDates = this.getIntent().getExtras().getString(this.getString(R.string.MissingLogDates));
		_missingLogCount = this.getIntent().getExtras().getInt(this.getString(R.string.MissingLogCount));
		
		this.findActivityControls();
		this.loadControls();

	}
	
	@Override
	protected void findActivityControls()
	{
		_odclblmessage = (TextView)findViewById(R.id.odclblmessage);
		
		findViewById(R.id.mlbtnyes).setOnClickListener(
	            new OnClickListener() {
	            	public void onClick(View v) {
	            		handleButtonClick(v);
	            	}
	            });

			findViewById(R.id.mlbtnno).setOnClickListener(
	            new OnClickListener() {
	            	public void onClick(View v) {
	            		handleButtonClick(v);
	            	}
	            });
	}
	
	protected void loadControls()
	{

		_odclblmessage.setText(String.format(this.getResources().getString(R.string.msgmissinglogscreateoffduty), _missingLogCount, _missingLogDates ));
	}
	
	private void handleButtonClick(View v)
	{
		Button btn = (Button)v;
		int resultCode = 1;
		
		if (btn.getText().toString().equalsIgnoreCase(this.getString(R.string.No)))
			resultCode = 0;
		
		Intent intent = new Intent();
		setResult(resultCode, intent);
		
		this.finish();
	}
	
	@Override
	protected void InitController() {

	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		this.CreateOptionsMenu(menu, false);
		return true;
	}
	
}
