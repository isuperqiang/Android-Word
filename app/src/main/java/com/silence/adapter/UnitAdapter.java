package com.silence.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.andraskindler.quickscroll.Scrollable;
import com.silence.pojo.Unit;
import com.silence.utils.DateUtils;
import com.silence.word.R;

import java.util.List;

/**
 * Created by Silence on 2016/3/18 0018.
 */
public class UnitAdapter extends BaseAdapter implements Scrollable {
    private Context mContext;
    private List<Unit> mUnitList;

    public UnitAdapter(Context context, List<Unit> units) {
        mContext = context;
        mUnitList = units;
    }

    @Override
    public int getCount() {
        return mUnitList != null ? mUnitList.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return mUnitList != null ? mUnitList.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_category, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.linearLayout = (LinearLayout) convertView.findViewById(R.id.item_unit);
            viewHolder.tvUnit = (TextView) convertView.findViewById(R.id.tv_unit);
            viewHolder.tvTime = (TextView) convertView.findViewById(R.id.tv_time);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        Unit unit = mUnitList.get(position);
        viewHolder.tvUnit.setText(String.valueOf(unit.getKey()));
        viewHolder.tvTime.setText(mContext.getString(R.string.sum_time) + DateUtils.formatTime(unit.getTime()));
        if (position % 3 == 1) {
            viewHolder.linearLayout.setBackgroundResource(R.drawable.unit_yellow_bg);
        } else if (position % 3 == 2) {
            viewHolder.linearLayout.setBackgroundResource(R.drawable.unit_red_bg);
        } else {
            viewHolder.linearLayout.setBackgroundResource(R.drawable.unit_blue_bg);
        }
        return convertView;
    }

    @Override
    public String getIndicatorForPosition(int childposition, int groupposition) {
        return String.valueOf(mUnitList.get(childposition).getKey());
    }

    @Override
    public int getScrollPosition(int childposition, int groupposition) {
        return childposition;
    }

    static class ViewHolder {
        TextView tvUnit;
        LinearLayout linearLayout;
        TextView tvTime;
    }
}
