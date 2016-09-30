package com.javon.cdmk.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.javon.cdmk.R;


/**
 * @author Javon Davis
 */
public class SimpleCharacterAdapter extends BaseAdapter {

    private Context mContext;
    private char[] mAnswer;
    private boolean visible;

    public SimpleCharacterAdapter(Context context,char[] answer, boolean flag) {
        mContext = context;
        mAnswer = answer;
        setVisible(flag);
    }

    @Override
    public int getCount() {
        return mAnswer.length;
    }

    @Override
    public Object getItem(int position) {
        return mAnswer[position];
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;

        LayoutInflater inflater = (LayoutInflater) mContext
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            view = inflater.inflate(R.layout.options_item,null);
            if(isVisible()) {
                TextView option = (TextView) view.findViewById(R.id.letterView);
                option.setText(Character.toString(mAnswer[position]));
            }

        } else {
            view = (View) convertView;
        }
        return view;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
