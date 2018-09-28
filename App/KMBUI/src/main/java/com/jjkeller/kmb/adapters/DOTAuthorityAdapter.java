package com.jjkeller.kmb.adapters;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jjkeller.kmbui.R;

import java.util.List;

/**
 * Created by developer on 5/19/17.
 */

public class DOTAuthorityAdapter extends BaseAdapter {
    private List<DOTItem> listRecords;
    private Context ctx;

    public DOTAuthorityAdapter(List<DOTItem> listRecords, Context ctx) {
        this.listRecords = listRecords;
        this.ctx = ctx;
    }

    @Override
    public int getCount() {
        return listRecords.size();
    }

    @Override
    public DOTItem getItem(int position) {
        return listRecords.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DOTAuthorityRowView dotAuthorityRowView;
        if(convertView==null){
            dotAuthorityRowView = new DOTAuthorityRowView(ctx);
        }else{
            dotAuthorityRowView = (DOTAuthorityRowView)convertView;
        }
        dotAuthorityRowView.bind(getItem(position));
        return dotAuthorityRowView;
    }

    public class DOTAuthorityRowView extends LinearLayout {
        TextView tvBeginningTime, tvEndingTime, tvNumber, tvName, tvAddress;

        public DOTAuthorityRowView(Context context) {
            super(context);
            View vLayout = inflate(context, R.layout.dot_authority_row, this);

            tvBeginningTime = (TextView)vLayout.findViewById(R.id.tvBeginningTime);
            tvEndingTime = (TextView)vLayout.findViewById(R.id.tvEndingTime);
            tvNumber = (TextView)vLayout.findViewById(R.id.tvNumber);
            tvName = (TextView)vLayout.findViewById(R.id.tvAuthorityName);
            tvAddress = (TextView)vLayout.findViewById(R.id.tvAuthorityAddress);
        }
        public void bind(DOTItem record){
            tvBeginningTime.setText(record.getStartTime());
            tvEndingTime.setText(record.getEndTime() == null ? "" : record.getEndTime());
            tvNumber.setText(record.getNumber());
            tvName.setText(record.getName());
            tvAddress.setText(record.getAddress());
        }
    }

    public static class DOTItem implements Parcelable{
        private String startTime;
        private String endTime;
        private String number;
        private String name;
        private String address;

        public DOTItem(String startTime, String endTime, String number, String name, String address) {
            this.startTime = startTime;
            this.endTime = endTime;
            this.number = number;
            this.name = name;
            this.address = address;
        }

        public String getStartTime() {
            return startTime;
        }

        public String getEndTime() {
            return endTime;
        }

        public String getNumber() {
            return number;
        }

        public String getName() {
            return name;
        }

        public String getAddress() {
            return address;
        }

        protected DOTItem(Parcel in) {
            startTime = in.readString();
            endTime = in.readString();
            number = in.readString();
            name = in.readString();
            address = in.readString();
        }

        public void setEndTime(String endTime) {
            this.endTime = endTime;
        }

        //region Parcelable implementation

        public static final Creator<DOTItem> CREATOR = new Creator<DOTItem>() {
            @Override
            public DOTItem createFromParcel(Parcel in) {
                return new DOTItem(in);
            }

            @Override
            public DOTItem[] newArray(int size) {
                return new DOTItem[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(startTime);
            parcel.writeString(endTime);
            parcel.writeString(number);
            parcel.writeString(name);
            parcel.writeString(address);
        }

        //endregion
    }

}