package com.jjkeller.kmb.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.jjkeller.kmb.adapters.DOTAuthorityAdapter;
import com.jjkeller.kmb.share.BaseFragment;
import com.jjkeller.kmbui.R;

import java.util.List;

/**
 * Created by T000695 on 5/22/2017.
 */

public class DOTAuthorityListFrag extends BaseFragment
{
    private List<DOTAuthorityAdapter.DOTItem> _listDOTAuthority;
    private ListView _list;

    public static DOTAuthorityListFrag newInstance(){
        DOTAuthorityListFrag fragment = new DOTAuthorityListFrag();

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.f_dotauthoritylist, container, false);

        findControls(v);

        if (_listDOTAuthority != null) {
            setDataSource(_listDOTAuthority);
        }

        return v;
    }

    public void setDataSource(List<DOTAuthorityAdapter.DOTItem> listDOTAuthority) {
        if (isAdded() && _list != null) {
            _list.setAdapter(new DOTAuthorityAdapter(listDOTAuthority, getActivity().getApplicationContext()));
        }else {
            _listDOTAuthority = listDOTAuthority;
        }
    }

    protected void findControls(View v) {
        _list = (ListView) v.findViewById(R.id.listDot);
    }

}
