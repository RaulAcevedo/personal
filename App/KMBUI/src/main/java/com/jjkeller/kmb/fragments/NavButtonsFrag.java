package com.jjkeller.kmb.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.jjkeller.kmb.interfaces.INavButtonsFrag;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbui.R;

public class NavButtonsFrag extends BaseFragment  {
    private TextView _txtTitle;
    private TextView _txtEventXofX;
    private Button _btnPrevious;
    private Button _btnNext;

    private INavButtonsFrag.NavButtonsControllerMethods _navBtnsCtrlr;
    private boolean _titleOnly=false;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.f_navbuttons, container, false);
        findControls(v);
        return v;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadControls();
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        _navBtnsCtrlr = (INavButtonsFrag.NavButtonsControllerMethods) activity;
    }

    protected void findControls(View v) {
        _txtTitle = (TextView) v.findViewById(R.id.lblNavButtonsTitle);
        _txtEventXofX = (TextView) v.findViewById(R.id.lblEventXofX);
        _btnPrevious = (Button) v.findViewById(R.id.btnPrevious);
        _btnNext = (Button) v.findViewById(R.id.btnNext);
        if (_titleOnly){
            _btnNext.setVisibility(View.GONE);
            _btnPrevious.setVisibility(View.GONE);
        }

        _btnPrevious.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {_navBtnsCtrlr.handleBtnPrevious();
            }
        });
        _btnNext.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                _navBtnsCtrlr.handleBtnNext();
            }
        });
    }

    protected void loadControls() {
        if (_navBtnsCtrlr!=null && _navBtnsCtrlr.getNavButtonsTitle()!=null)
            _txtTitle.setText(Html.fromHtml(_navBtnsCtrlr.getNavButtonsTitle()));
        if (!_titleOnly) {
            updateCurrentItemDisplay();
        }
    }

    public void updateCurrentItemDisplay() {
        if (_navBtnsCtrlr != null) {
            String str = getString(R.string.lbleventxofx, String.valueOf(_navBtnsCtrlr.getCurrentItemIndex() + 1), String.valueOf(_navBtnsCtrlr.getTotalItemCount()));
            _txtEventXofX.setText(Html.fromHtml(str));
            _btnPrevious.setEnabled(_navBtnsCtrlr.getCurrentItemIndex() > 0);
            _btnNext.setEnabled(_navBtnsCtrlr.getCurrentItemIndex() < _navBtnsCtrlr.getTotalItemCount() - 1);
        }
    }
    public void setTitleOnly(boolean value){
        _titleOnly=value;
    }
}
