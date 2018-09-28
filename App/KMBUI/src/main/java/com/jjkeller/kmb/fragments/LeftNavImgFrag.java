package com.jjkeller.kmb.fragments;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbui.R;

public class LeftNavImgFrag extends LeftNavFrag {

	private final int itemLayoutResourceId;
	
	protected ActivityMenuIconItems _menuItemsIcons;
	
	
	public LeftNavImgFrag()
	{
		this.itemLayoutResourceId = R.layout.leftnav_item_imageandtext;
	}
	
	public LeftNavImgFrag(int itemLayoutResourceId)
	{
		this.itemLayoutResourceId = itemLayoutResourceId;
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
        
        try {
        	_menuItemsIcons = (ActivityMenuIconItems) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement ActivityMenuIconItems");
        }
        
    }
    

	@Override
    public void BuildMenu()
    {
    	// 8/14/12 JHM - Check for null listener.  Was causing team driver login to be shown 
    	// with OffDuty log.  Only seen on Acer100 7" tablet.
    	if(_menuItemsListener != null)
    	{
    		AddItems(_menuItemsListener.getActivityMenuItemList(), _menuItemsIcons.getActivityMenuIconList()); //  _menuItemsListener.getActivityMenuIconList() 
    	}
    }
    
	private void AddItems(String items, String icons)
    {
        Activity activity = getActivity();
        if (activity != null) {
            // Create an instance of the custom adapter for the GridView. 
        	// A static array of menu items and image names 
            // are stored in the strings.xml file

        	ListView listView = getListView();
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD)
				listView.setOverscrollFooter(null);
			listView.setDrawSelectorOnTop(true);
			listView.setDivider(new ColorDrawable(getResources().getColor(R.color.menudivider)));
			listView.setDividerHeight(0);
    		
        	if(items != null)
        	{ 
        		// Convert icon name list into array and obtain identifiers
        		String[] menuIcons = icons.split(",");
        		
        		Integer[] imageId;
        		
        		imageId = new Integer[menuIcons.length];
        		
        		for(int intCtr = 0; intCtr < menuIcons.length; intCtr ++)
        		{
					//imageId[intCtr] = getResources().getIdentifier(menuIcons[intCtr], "drawable", "com.jjkeller.kmb");
					imageId[intCtr] = getResources().getIdentifier(menuIcons[intCtr], "drawable", GlobalState.getInstance().getPackageName());

        		}
        		
        		
          		String[] menuItems = items.split(",");
          		
      			LeftNavImageAndText adapter = new
      					LeftNavImageAndText(getActivity(), menuItems, imageId);

    			setListAdapter(adapter);
        	}
        	else
        	{
        		ArrayAdapter<String> list;
        		list = new ArrayAdapter<String>(getActivity(), this.itemLayoutResourceId);
        		setListAdapter(list);
        	}
        }    	
    }
	
    public interface ActivityMenuIconItems{
    	public String getActivityMenuIconList();
    }

}
    


