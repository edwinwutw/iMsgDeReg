package com.edwin.imsgdereg.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.TextView;

import com.edwin.imsgdereg.countryCode.CountryCode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by edwinwu on 2018/3/12.
 */

public class CountryCodeAdapter extends BaseAdapter {
    private final LayoutInflater mInflater;
    private final List<CountryCode> mData;
    private final int mViewId;
    private final int mDropdownViewId;
    private int mSelected;

    public CountryCodeAdapter(Context context, int viewId, int dropdownViewId) {
        this(context, new ArrayList<CountryCode>(), viewId, dropdownViewId);
    }

    public CountryCodeAdapter(Context context, List<CountryCode> data, int viewId, int dropdownViewId) {
        mInflater = LayoutInflater.from(context);
        mData = data;
        mViewId = viewId;
        mDropdownViewId = dropdownViewId;

        Iterator iter = CountryCode.COUNTRYCODE_MAP.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            Object key = entry.getKey();
            Object val = entry.getValue();
            mData.add(new CountryCode((int)val, (String)key));
        }
        sort(new Comparator<CountryCode>() {
            public int compare(CountryCode lhs, CountryCode rhs) {
                return lhs.countryName.compareTo(rhs.countryName);
            }
        });
    }

    public void sort(Comparator<? super CountryCode> comparator) {
        Collections.sort(mData, comparator);
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        CountryCode e = mData.get(position);
        return (e != null) ? e.countryCode : -1;
    }

    public void setSelected(int position) {
        mSelected = position;
    }

    public int getPositionForId(CountryCode cc) {
        return cc != null ? mData.indexOf(cc) : -1;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        CheckedTextView textView;
        View view;
        if (convertView == null) {
            view = mInflater.inflate(mDropdownViewId, parent, false);
            textView = (CheckedTextView) view.findViewById(android.R.id.text1);
            view.setTag(textView);
        }
        else {
            view = convertView;
            textView = (CheckedTextView) view.getTag();
        }

        CountryCode e = mData.get(position);

        StringBuilder text = new StringBuilder(5)
                .append(e.countryName)
                .append(" (+")
                .append(e.countryCode)
                .append(')');

        textView.setText(text);
        textView.setChecked((mSelected == position));

        return view;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        TextView textView;
        View view;
        if (convertView == null) {
            view = mInflater.inflate(mViewId, parent, false);
            textView = (TextView) view.findViewById(android.R.id.text1);
            view.setTag(textView);
        }
        else {
            view = convertView;
            textView = (TextView) view.getTag();
        }

        CountryCode e = mData.get(position);

        StringBuilder text = new StringBuilder(3)
                .append(e.countryName)
                .append(" (+")
                .append(e.countryCode)
                .append(')');


        textView.setText(text);

        return view;
    }

}