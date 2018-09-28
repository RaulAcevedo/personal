package com.jjkeller.kmb;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.jjkeller.kmb.fragments.EulaFrag;
import com.jjkeller.kmb.fragments.LeftNavFrag;
import com.jjkeller.kmb.share.BaseActivity;
import com.jjkeller.kmbapi.controller.utility.FileUtility;
import com.jjkeller.kmbapi.models.EulaSection;
import com.jjkeller.kmbui.R;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by RDelgado on 5/19/2017.
 * Eula BaseActivity used to present the EULA text, this class which contains the LeftNavFragment and EulaFragment
 */

public class Eula extends BaseActivity implements LeftNavFrag.OnNavItemSelectedListener, LeftNavFrag.ActivityMenuItemsListener{

	private EulaFrag _contentFrag;
	private LeftNavFrag _leftNavFrag;
	private int _lastSelectedItem;
	private ArrayList<EulaSection> sections;
	private Boolean startedFromLogin;

	@Override
	@SuppressWarnings("unchecked")
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.baselayout);

		//Get startedFromLogin from intent to know if this activity was started from Login activity
		// or from SystemMenu
		startedFromLogin = getIntent().getBooleanExtra("startedFromLogin" , false);

		if (savedInstanceState != null && savedInstanceState.containsKey("sections")){
			sections = (ArrayList<EulaSection>) savedInstanceState.getSerializable("sections");
			_lastSelectedItem = savedInstanceState.getInt("selectedItem");
		}else{
			//Get the Eula raw resource from eula.txt for parsing
			String eula = FileUtility.InputStreamToString(this.getResources().openRawResource(R.raw.eula));
			sections = parseEula(eula);
			_lastSelectedItem = -1;
		}

		loadContentFragment(new EulaFrag());
		setFragments();
	}

	protected void InitController() {}

	@Override
	public void setFragments()
	{
		super.setFragments();
		setLeftNavAllowChange(true);
		//Fix the size of the leftNavFrag
		updateLeftNavSize();
		//Load LeftNavFragment with a custom item layout leftnav_item_eula
		super.loadLeftNavFragment(R.layout.leftnav_item_eula);
		Fragment f = getSupportFragmentManager().findFragmentById(R.id.content_fragment);
		_contentFrag = (EulaFrag) f;
		_leftNavFrag = getLeftNavFragment();
	}

	public String getActivityMenuItemList()
	{
		//Add the "Done" item to the LeftNavFragment menu
		String menuItems = getString(R.string.btndone);
		//Add one item to the LeftNavFragment menu for each section use the section name for tag
		for (EulaSection section: sections) {
			menuItems = menuItems + ","  + section.getName();
		}
		return menuItems;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		//Override and do not call CreateOptionsMenu() if the Activity shouldn't have any menu.
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		handleMenuItemSelected(item.getItemId());
		return true;
	}

	public void onNavItemSelected(int menuItem)
	{
		handleMenuItemSelected(menuItem);
	}

	private void handleMenuItemSelected(int itemPosition)
	{
		switch (itemPosition){
			case android.R.id.home:
				//JJ Keller Mobile navigation icon on actionBar selected
				this.Return();
				break;
			case 0:
				//"Done" LeftNavFragment item selected
				this.Return();
				break;
			default:
				//For any other LeftNavFragment item selected get the sectionPosition,
				// move the scroll to section and Highlight the LeftNavFragment item
				EulaSection section = sections.get(itemPosition - 1);
				_contentFrag.moveScrollViewTo(section.getStartPosition());
				leftNavHighlightSelectedItem(itemPosition);
				_lastSelectedItem = itemPosition;
				break;
		}
	}

	/**
	 * Parse and create the EulaSections from the eula text:
	 * @param eula The string resource which will be used as eula message.
	 * @return Arraylist of EulaSections with number, position and name.
	 */
	private ArrayList<EulaSection> parseEula(String eula){
		ArrayList<EulaSection> sections = new ArrayList<>();

		//Identify if there is any match to the format (##. ) single or double digit + dot + white
		//space. This format marks the beginning od a new Eula section
		String re1="([1-9][0-9]?)";	// Any number from 1 to 99
		String re2="(\\.)";	// then a single dot
		String re3="(\\s+)";	// then a white space
		final Pattern pattern = Pattern.compile(re1+re2+re3,Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
		Matcher matcher = pattern.matcher(eula);

		while (matcher.find()) {
			//Identify the position of the next newline after the match
			int endOfLine = eula.indexOf("\n", matcher.end());
			//Check if we are not at the end of the document
			if(endOfLine > matcher.end()){
				//Remove the /t tab indentation from the match.
				String match = matcher.group().substring(0,matcher.group().length() - 2 );
				int sectionNumber = Integer.parseInt(match);
				//Get the text line as EulaSection name after the match
				String sectionName = eula.substring(matcher.end(), endOfLine);
				//Remove the ',' since the menuItems for the LeftNavFragment uses this character to
				// separate menu items.
				sectionName = sectionName.replace(",", "");
				//Create and add the new EulaSection
				EulaSection section = new EulaSection(sectionNumber, matcher.start(), sectionName);
				sections.add(section);
			}
		}
		return sections;
	}

	@Override
	protected void onStart() {
		super.onStart();
		UnlockScreenRotation();
		//Check if there was a highlight selected item from previous orientation change
		if (_lastSelectedItem >= 0){
			leftNavHighlightSelectedItem(_lastSelectedItem);
		}
	}

	/**
	 * Update the highlight of LeftNavFragment item accordingly to the EulaFrag scrollView
	 * @param scrollY Position in Y of the EulaFrag scrollView
	 */
	public void updateHighlightSelectedItem(int scrollY){
		if(scrollY > 0){
			for (EulaSection section: sections) {
				//Check if the scrollY matches any EulaSection startPosition line, then move to that
				//position and Highlight the leftNavFrag item
				int sectionLine = _contentFrag.getLineForOffsetEula(section.getStartPosition());
				if(sectionLine == scrollY){
					leftNavHighlightSelectedItem(section.getNumber());
					_leftNavFrag.getListView().smoothScrollToPosition(section.getNumber());
					_lastSelectedItem = section.getNumber();
				}
			}
		}else{
			//When reaching y = 0 start of the document unHighlight the _lastSelectedItem
			leftNavUnHighlightSelectedItem(_lastSelectedItem);
			_leftNavFrag.getListView().smoothScrollToPosition(0);
			_lastSelectedItem = -1;
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putSerializable("sections", sections);
		outState.putInt("selectedItem", _lastSelectedItem);
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void Return()
	{
		//If this activity was started from login, finish activity on return
		if(startedFromLogin)
		{
			this.finish();
		}else{
			//If this activity was started from SystemMenu ClearTop to RodsEntry Activity
			Bundle extras = new Bundle();
			this.startActivity(RodsEntry.class, Intent.FLAG_ACTIVITY_CLEAR_TOP, extras);
			this.finish();
		}
	}

	/**
	 * Increase the weight of the leftNavFrag only if we are in portrait orientation
	 */
	public void updateLeftNavSize(){
		Configuration config = this.getResources().getConfiguration();
		if(config.orientation == Configuration.ORIENTATION_PORTRAIT ){
			increaseLeftNavWeight(30);
		}
	}
}
