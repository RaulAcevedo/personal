package com.jjkeller.kmb.fragments;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jjkeller.kmb.EditLogRequest;
import com.jjkeller.kmb.interfaces.IReviewEditRequests.ReviewEditRequestsFragActions;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbapi.configuration.GlobalState;
import com.jjkeller.kmbapi.controller.utility.DateUtility;
import com.jjkeller.kmbapi.proxydata.EmployeeLog;
import com.jjkeller.kmbui.R;

import java.util.List;

/***
 * Fragment to display the list of Employee Logs that have ELD Events with Change Requests.
 */
public class ReviewEditRequestsFrag extends BaseFragment {
	private GridView _gridLogsThatNeedToBeReviewed;
	private TextView _textNoRecords;
	private Button _btnDownloadEditRequests;
	ReviewEditRequestsFragActions actionsListener;

	private LinearLayout _linearHUD;
	private TextView _txtError;
	private TextView _txtSuccess;
	private String _hudSuccessMessage="";
	private String _hudErrorMessage="";


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.f_revieweditrequests, container, false);
		findControls(v);
		return v;
	}


	@Override
	public void onAttach(Activity activity){
		super.onAttach(activity);
		actionsListener = (ReviewEditRequestsFragActions) activity;
	}


	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	}

	protected void findControls(View v) {
		_gridLogsThatNeedToBeReviewed = (GridView)v.findViewById(R.id.gridReviewEditRequests);
		_textNoRecords = (TextView)v.findViewById(R.id.textNoRecords);
		_btnDownloadEditRequests = (Button) v.findViewById(R.id.btnDownloadEditRequests);
		_linearHUD = (LinearLayout)v.findViewById(R.id.linearHUD);
		_txtError = (TextView)v.findViewById(R.id.txtError);
		_txtSuccess = (TextView)v.findViewById(R.id.txtSuccess);
		_btnDownloadEditRequests.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				actionsListener.handleDownloadButtonClick();
			}
		});
	}

	public Button getDownloadButton(){
		return _btnDownloadEditRequests;
	}


	// Shows messages in the Heads-Up Display.
	public void showHUDMessageSuccess(String message) {
		if (message.length() > 0) {
			_txtError.setVisibility(View.GONE);
			_txtSuccess.setText(Html.fromHtml(message));
			_txtSuccess.setVisibility(View.VISIBLE);
			_linearHUD.setVisibility(View.VISIBLE);

			_hudSuccessMessage = message;
		}
	}

	public void showHUDMessageError(String message) {
		if (message.length() > 0) {
			_txtSuccess.setVisibility(View.GONE);
			_txtError.setText(Html.fromHtml(message));
			_txtError.setVisibility(View.VISIBLE);
			_linearHUD.setVisibility(View.VISIBLE);

			_hudErrorMessage = message;
		}
	}

	public void hideHUD() {
		_linearHUD.setVisibility(View.GONE);
		_txtError.setVisibility(View.GONE);
		_txtSuccess.setVisibility(View.GONE);
	}



	public void setDataSource(List<EmployeeLog> logsThatNeedToBeReviewed)
	{
		if (logsThatNeedToBeReviewed == null || logsThatNeedToBeReviewed.isEmpty()) {
			_gridLogsThatNeedToBeReviewed.setVisibility(View.GONE);
			_textNoRecords.setVisibility(View.VISIBLE);
		}
		else {
			_textNoRecords.setVisibility(View.GONE);

			_gridLogsThatNeedToBeReviewed.setVisibility(View.VISIBLE);
			_gridLogsThatNeedToBeReviewed.setAdapter(new ReviewEditRequestsAdapter(getActivity(), R.layout.grdrevieweditrequests, logsThatNeedToBeReviewed));
		}
	}

	private class ReviewEditRequestsAdapter extends ArrayAdapter<EmployeeLog> {

		private List<EmployeeLog> items;
		public ReviewEditRequestsAdapter (Context context, int textViewResourceId, List<EmployeeLog> items ) {
			super(context, textViewResourceId, items);
			this.items = items;
		}

		public class ViewHolder {
			public TextView txtLogDate;
			public Button btnReviewLog;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder view;

			if(convertView == null) {
				view = new ViewHolder();

				LayoutInflater inflator = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = inflator.inflate(R.layout.grdrevieweditrequests, null);

				view.txtLogDate = (TextView) convertView.findViewById(R.id.tvLogDate);
				view.btnReviewLog = (Button) convertView.findViewById(R.id.btnReviewLog);

				convertView.setTag(view);
			}
			else
			{
				view = (ViewHolder) convertView.getTag();
			}

			view.txtLogDate.setText(DateUtility.getHomeTerminalDateFormat().format(items.get(position).getLogDate()));

			// onClickListener needs to be defined each time or the recycled view will reference it's original position
			final int logPosition = position;
			view.btnReviewLog.setOnClickListener(
					new View.OnClickListener() {
						public void onClick(View v) {
							EmployeeLog log = items.get(logPosition);
							hideHUD();
							Bundle bundle = new Bundle();
                            //setting these on global state to be able to return after navigating on view original log
                            GlobalState.getInstance().setReviewEldEventDate(log.getLogDate());
                            GlobalState.getInstance().setReviewEldEventLogKey((int)log.getPrimaryKey());
							bundle.putInt(EditLogRequest.EXTRA_EMPLOYEELOGKEY, (int)log.getPrimaryKey());
							bundle.putString(EditLogRequest.EXTRA_EMPLOYEELOGDATE, DateUtility.getHomeTerminalDateFormat().format(log.getLogDate()));
							startActivity(EditLogRequest.class, bundle);
						}
					});

			return convertView;
		}
	}
}
