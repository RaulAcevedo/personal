package com.jjkeller.kmb.adapters;


import android.content.Context;
import android.graphics.Typeface;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jjkeller.kmb.UnidentifiedELDEventsReview;
import com.jjkeller.kmbapi.controller.EmployeeLogEldMandateController;
import com.jjkeller.kmbapi.proxydata.EmployeeLogEldEvent;
import com.jjkeller.kmbui.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by T000683 on 4/24/2017.
 */

public class UnidentifiedELDEventsReviewListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<String> listDataHeader = new ArrayList<>();
    private HashMap<String, EmployeeLogEldEvent> listDataChild = new HashMap<String, EmployeeLogEldEvent>();
    List<EmployeeLogEldEvent> events;
    private HashMap<Long, DirtyEvent> dirtyEvents = new HashMap<Long, DirtyEvent>();

    public UnidentifiedELDEventsReviewListAdapter(Context context, List<EmployeeLogEldEvent> events) {
        this.context = context;
        this.events = events;
        // set the header List and child HashMap
        for (EmployeeLogEldEvent evt :events){
            String pKey = Long.toString(evt.getPrimaryKey());
            listDataHeader.add(pKey);
            listDataChild.put(pKey, evt);
            dirtyEvents.put(evt.getPrimaryKey(), new DirtyEvent(evt.getPrimaryKey()));
        }
    }

    @Override
    public int getGroupCount() {
        return this.listDataHeader.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 1;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.listDataHeader.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return this.listDataChild.get(this.listDataHeader.get(groupPosition));
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    public void removeGroup(int groupPosition) {
        String eventKey = listDataHeader.get(groupPosition);
        EmployeeLogEldEvent removedEvent = listDataChild.get(eventKey);
        events.remove(removedEvent);
        this.listDataHeader.remove(groupPosition);
    }

    public List<EmployeeLogEldEvent> getEvents(){
        return events;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String mapKey = (String) getGroup(groupPosition);
        EmployeeLogEldEvent event = this.listDataChild.get(mapKey);

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_unidentifiedeldevents_review_header, null);
        }

        TextView lblListHeader = (TextView) convertView.findViewById(R.id.lblListHeader);
        lblListHeader.setTypeface(null, Typeface.BOLD);
        lblListHeader.setText(event.getEventDateTime().toString()+"\n" + event.getCompositeEventCodeType(event.getEventType(),event.getEventCode()));

        return convertView;
    }

    Long selectedRecordPrimaryKey;

    @Override
    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        final EmployeeLogEldEvent childEvent = (EmployeeLogEldEvent) getChild(groupPosition, childPosition);
        selectedRecordPrimaryKey = childEvent.getPrimaryKey();
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_unidentifiedeldevents_review, null);
            holder = new ViewHolder();

            // read-only
            holder.txtReadOnly = (TextView) convertView.findViewById(R.id.lblEventsReadOnly);

            // required fields
            holder.llLocation = (LinearLayout) convertView.findViewById(R.id.llLocation);
            holder.edtLocation = (EditText) convertView.findViewById(R.id.editLocation);
            holder.reqLocation = (TextView) convertView.findViewById(R.id.reqLocation);
            holder.llUnitNumber = (LinearLayout) convertView.findViewById(R.id.llUnitNumber);
            holder.edtUnitNumber = (EditText) convertView.findViewById(R.id.editUnitNumber);
            holder.llTrailerInfo = (LinearLayout) convertView.findViewById(R.id.llTrailerInfo);
            holder.edtTrailerInfo = (EditText) convertView.findViewById(R.id.editTrailerInfo);
            holder.llShipmentInfo = (LinearLayout) convertView.findViewById(R.id.llShipmentInfo);
            holder.edtShipmentInfo = (EditText) convertView.findViewById(R.id.editShipmentInfo);

            // save button
            holder.saveBtn = (Button) convertView.findViewById(R.id.btnSave);
            holder.populateBtn = (Button) convertView.findViewById(R.id.btnPopulate);
            convertView.setTag(holder);
        }else {
            holder = (ViewHolder) convertView.getTag();
        }

        // READ-ONLY FIELDS
        StringBuilder sb = new StringBuilder();
        sb.append(childEvent.getEventDateTime().toString());
        sb.append("\n" + childEvent.getCompositeEventCodeType(childEvent.getEventType(),childEvent.getEventCode()));
        if(childEvent.getLocation() != null){
            if(childEvent.getLocation().getName() != null && childEvent.getLocation().getName().length() > 0){
                sb.append("\n" + childEvent.getLocation().getName());
            }
        }
        if(childEvent.getTractorNumber() != null)
            sb.append("\n" + childEvent.getTractorNumber());
        if(childEvent.getTrailerNumber() != null)
            sb.append("\n" + childEvent.getTrailerNumber());
        if(childEvent.getShipmentInfo() != null)
            sb.append("\n" + childEvent.getShipmentInfo());

        holder.txtReadOnly.setText(sb.toString());

        // REQUIRED FIELDS
        // location
        holder.edtLocation.setError(null);
        if(isDriversLocationDescriptionMissing(childEvent)){
            holder.llLocation.setVisibility(View.VISIBLE);
            holder.edtLocation.setVisibility(View.VISIBLE);
            holder.edtLocation.setHint(context.getString(R.string.unidentified_events_review_required_field, "Location"));
            holder.edtLocation.setText(dirtyEvents.get(childEvent.getPrimaryKey()).getLocation());
            holder.edtLocation.addTextChangedListener(new RequiredFieldTextWatcher(holder.edtLocation, childEvent.getPrimaryKey()));
            holder.reqLocation.setVisibility(View.VISIBLE);
        }else {
            // hide
            holder.llLocation.setVisibility(View.GONE);
        }
        // unit number
        holder.edtUnitNumber.setError(null);
        if(TextUtils.isEmpty(childEvent.getTractorNumber())){
            holder.llUnitNumber.setVisibility(View.VISIBLE);
            holder.edtUnitNumber.setVisibility(View.VISIBLE);
            holder.edtUnitNumber.setHint(context.getString(R.string.unitnumber_hint));
            holder.edtUnitNumber.setText(dirtyEvents.get(childEvent.getPrimaryKey()).getUnitNumber());
            holder.edtUnitNumber.addTextChangedListener(new RequiredFieldTextWatcher(holder.edtUnitNumber, childEvent.getPrimaryKey()));
        }else {
            // hide
            holder.llUnitNumber.setVisibility(View.GONE);
        }

        // trailer info
        holder.edtTrailerInfo.setError(null);
        if(TextUtils.isEmpty(childEvent.getTrailerNumber())){
            holder.llTrailerInfo.setVisibility(View.VISIBLE);
            holder.edtTrailerInfo.setVisibility(View.VISIBLE);
            holder.edtTrailerInfo.setHint(context.getString(R.string.trailerinfo_hint));
            holder.edtTrailerInfo.setText(dirtyEvents.get(childEvent.getPrimaryKey()).getTrailerInfo());
            holder.edtTrailerInfo.addTextChangedListener(new RequiredFieldTextWatcher(holder.edtTrailerInfo, childEvent.getPrimaryKey()));
        }else {
            // hide
            holder.llTrailerInfo.setVisibility(View.GONE);
        }

        // shipment info
        holder.edtShipmentInfo.setError(null);
        if(TextUtils.isEmpty(childEvent.getShipmentInfo())){
            holder.llShipmentInfo.setVisibility(View.VISIBLE);
            holder.edtShipmentInfo.setVisibility(View.VISIBLE);
            holder.edtShipmentInfo.setHint(context.getString(R.string.shipmentinfo_hint));
            holder.edtShipmentInfo.setText(dirtyEvents.get(childEvent.getPrimaryKey()).getShipmentInfo());
            holder.edtShipmentInfo.addTextChangedListener(new RequiredFieldTextWatcher(holder.edtShipmentInfo, childEvent.getPrimaryKey()));
        }else {
            // hide
            holder.llShipmentInfo.setVisibility(View.GONE);
        }

        // save button
        holder.saveBtn.setTag(groupPosition);
        holder.saveBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // run validation, save list item in Activity
                ((UnidentifiedELDEventsReview)context).saveEvent(groupPosition, childEvent);
            }
        });

        holder.populateBtn.setTag(groupPosition);
        holder.populateBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // run validation, save list item in Activity
                ((UnidentifiedELDEventsReview)context).populateEvent(groupPosition);
            }
        });

        return convertView;
    }

    protected boolean isDriversLocationDescriptionMissing(EmployeeLogEldEvent event) {
        if (event.getDistanceSinceLastCoordinates() == null) {
            return false;
        }

        boolean isGpsDistanceUncertain = event.getDistanceSinceLastCoordinates() > EmployeeLogEldMandateController.MAX_VALID_UNCERTAINTY_MILES;
        return isGpsDistanceUncertain && TextUtils.isEmpty(event.getDriversLocationDescription());
    }

    public class RequiredFieldTextWatcher implements TextWatcher{
        private EditText editText;
        private Long primaryKey;

        public RequiredFieldTextWatcher(EditText editText, Long primaryKey) {
            this.editText = editText;
            this.primaryKey = primaryKey;
        }
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}
        @Override
        public void afterTextChanged(Editable s) {
            // only update the selected child item, not all edittext views
            if(dirtyEvents.get(primaryKey) != null && primaryKey.longValue() == selectedRecordPrimaryKey.longValue()){
                if(editText.getId() == R.id.editLocation){
                    dirtyEvents.get(primaryKey).setLocation(s.toString());
                }else if(editText.getId() == R.id.editUnitNumber){
                    dirtyEvents.get(primaryKey).setUnitNumber(s.toString());
                }else if(editText.getId() == R.id.editTrailerInfo){
                    dirtyEvents.get(primaryKey).setTrailerInfo(s.toString());
                }else if(editText.getId() == R.id.editShipmentInfo){
                    dirtyEvents.get(primaryKey).setShipmentInfo(s.toString());
                }
            }
        }
    }

    static class ViewHolder {
        private TextView txtReadOnly;
        private LinearLayout llLocation;
        private EditText edtLocation;
        private TextView reqLocation;
        private LinearLayout llUnitNumber;
        private EditText edtUnitNumber;
        private LinearLayout llTrailerInfo;
        private EditText edtTrailerInfo;
        private LinearLayout llShipmentInfo;
        private EditText edtShipmentInfo;
        private Button saveBtn;
        private Button populateBtn;
    }

    class DirtyEvent {

        public DirtyEvent(Long primaryKey) {this.primaryKey = primaryKey;}

        private Long primaryKey;

        private String location;
        private String unitNumber;
        private String trailerInfo;
        private String shipmentInfo;

        public String getTrailerInfo() {
            return trailerInfo;
        }

        public void setTrailerInfo(String trailerInfo) {
            this.trailerInfo = trailerInfo;
        }

        public String getShipmentInfo() {
            return shipmentInfo;
        }

        public void setShipmentInfo(String shipmentInfo) {
            this.shipmentInfo = shipmentInfo;
        }

        public String getLocation() {
            return location;
        }
        public void setLocation(String location) {
            this.location = location;
        }

        public String getUnitNumber() {
            return unitNumber;
        }

        public void setUnitNumber(String unitNumber) {
            this.unitNumber = unitNumber;
        }
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

}