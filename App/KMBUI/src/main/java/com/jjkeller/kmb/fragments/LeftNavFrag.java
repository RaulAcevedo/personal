package com.jjkeller.kmb.fragments;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.jjkeller.kmbui.R;

public class LeftNavFrag extends ListFragment {
	
	private final int itemLayoutResourceId;
	
	protected OnNavItemSelectedListener _navItemSelectedListener;
	protected ActivityMenuItemsListener _menuItemsListener;
	private int _selectedItem = -1;
	private boolean _allowChange = false;
	
	public LeftNavFrag()
	{
		this.itemLayoutResourceId = R.layout.leftnav_item;
	}
	
	public LeftNavFrag(int itemLayoutResourceId)
	{
		this.itemLayoutResourceId = itemLayoutResourceId;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);       
        BuildMenu();   
        getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        
        // Loop through each item and highlight the selected item in the leftnav
		for (int i = 0; i < this.getListAdapter().getCount(); i++) {
			String name = (String) this.getListAdapter().getItem(i);
			// Do not highlight the "Done" item
			if (!name.equals("Done")) {
				if (i == _selectedItem && _allowChange)
					getListView().setItemChecked(i, true);
			}
		}
    }
    
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
        	_navItemSelectedListener = (OnNavItemSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnNavItemSelectedListener");
        }
        try {
        	_menuItemsListener = (ActivityMenuItemsListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement ActivityMenuItemsListener");
        }
    }

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {	
		// When items are selected, make sure that they are allowed to be highlighted
		if (_allowChange) {
			setSelectedItem(position);
			getListView().setItemChecked(position, true);
		} else {
			getListView().setItemChecked(position, false);
		}
		
		// Send the event to the host activity
		_navItemSelectedListener.onNavItemSelected(position);
	}
	

    // Container Activity must implement this interface
    public interface OnNavItemSelectedListener {
        public void onNavItemSelected(int itemPosition);
    }
    public interface ActivityMenuItemsListener{
    	public String getActivityMenuItemList();
    	//public String getActivityMenuIconList();
    }

    public void BuildMenu()
    {
    	// 8/14/12 JHM - Check for null listener.  Was causing team driver login to be shown 
    	// with OffDuty log.  Only seen on Acer100 7" tablet.
    	if(_menuItemsListener != null)
    	{
    		AddItems(_menuItemsListener.getActivityMenuItemList());
    	}
    }

	private void AddItems(String items)
    {
        Activity activity = getActivity();
        if (activity != null) {
            // Create an instance of the custom adapter for the GridView. A static array of location data
            // is stored in the Application sub-class for this app. This data would normally come
            // from a database or a web service.

        	ListView listView = getListView();
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD)
				listView.setOverscrollFooter(null);
			listView.setDrawSelectorOnTop(true);
			listView.setDivider(new ColorDrawable(getResources().getColor(R.color.menudivider)));
			listView.setDividerHeight(1);
    		
        	ArrayAdapter<String> list;
        	if(items != null)
        	{        		
        		String[] menuItems = items.split(",");
        		list = new ArrayAdapter<String>(getActivity(), this.itemLayoutResourceId, menuItems);
        	}
        	else
        		list = new ArrayAdapter<String>(getActivity(), this.itemLayoutResourceId);
        	
            setListAdapter(list);
        }    	
    }

    /**
     * Returns the text of an item in the LeftNav.
     * Primary usage is for activities where the items in the list are conditional and 
     * the name is needed instead of just the position.
     * @param itemPosition Position in the list provided by onListItemClick
     * @return Text of the list item
     */
    public String GetNavItemText(int itemPosition)
    {
    	return (String)this.getListAdapter().getItem(itemPosition);
    }

    /**
     * Sets the index for highlighting the selected item in the LeftNav.
     * @param position - Position in the list provided by onListItemClick and the BaseActivity
     */
	public void setSelectedItem(int position) {
		if (position >= 0)
			_selectedItem = position;
	}

	/**
	 * Sets the index for remove the highlighting of a selected item in the LeftNav.
	 * @param position Position in the list provided by onListItemClick and the BaseActivity
	 */
	public void highlightSelectedItem(int position) {
		if (getListView() != null)
			getListView().setItemChecked(position, true);
	}

	public void unHighlightSelectedItem(int position) {
		if (getListView() != null)
			getListView().setItemChecked(position, false);
	}

    /**
     * Sets whether or not highlighting the selected item in the LeftNav is allowed.
     * @param allow - Boolean provided by activities that should allow highlighting the selected item.
     */
	public void setAllowChange(boolean allow) {
		_allowChange = allow;
	}
}
