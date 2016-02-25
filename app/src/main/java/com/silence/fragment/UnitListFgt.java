package com.silence.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.silence.adapter.CommonAdapter;
import com.silence.dao.UnitDao;
import com.silence.pojo.Unit;
import com.silence.utils.Const;
import com.silence.utils.DateUtils;
import com.silence.word.R;

import java.util.List;

/**
 * Created by Silence on 2016/2/8 0008.
 */
public class UnitListFgt extends ListFragment {

    private int mFlag = 1;
    private int mCurUnit = 0;
    private View mClickedView;
    private UnitDao mUnitDao;
    private onUnitClickListener mOnUnitClickListener;

    public static UnitListFgt getInstance(String metaKey) {
        Bundle bundle = new Bundle();
        bundle.putString(Const.META_KEY, metaKey);
        UnitListFgt unitListFgt = new UnitListFgt();
        unitListFgt.setArguments(bundle);
        return unitListFgt;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof onUnitClickListener) {
            mOnUnitClickListener = (onUnitClickListener) context;
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mCurUnit != 0 && mClickedView != null) {
            long curTime = mUnitDao.getTime(getArguments().getString(Const.META_KEY),
                    mCurUnit);
            TextView textView = (TextView) mClickedView.findViewById(R.id.tv_time);
            String text = getString(R.string.sum_time) + DateUtils.formatTime(curTime);
            textView.setText(text);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getActivity() instanceof onUnitClickListener) {
            mOnUnitClickListener = (onUnitClickListener) getActivity();
        }
        mUnitDao = new UnitDao(getActivity());
        List<Unit> units = mUnitDao.getUnits(getArguments().getString(Const.META_KEY));
        if (units != null) {
            CommonAdapter commonAdapter = new CommonAdapter<Unit>(units, R.layout.item_category) {
                @Override
                public void bindView(ViewHolder holder, Unit obj) {
                    holder.setText(R.id.tv_unit, String.valueOf(obj.getKey()));
                    holder.setText(R.id.tv_time, getString(R.string.sum_time) + DateUtils.formatTime(obj.getTime()));
                    if (mFlag % 3 == 1) {
                        holder.setImageResource(R.id.item_unit, R.drawable.unit_yellow_bg);
                    } else if (mFlag % 3 == 2) {
                        holder.setImageResource(R.id.item_unit, R.drawable.unit_red_bg);
                    } else {
                        holder.setImageResource(R.id.item_unit, R.drawable.unit_blue_bg);
                    }
                    mFlag++;
                }
            };
            setListAdapter(commonAdapter);
        } else {
            Toast.makeText(getActivity(), R.string.no_data, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getListView().setDivider(getResources().getDrawable(R.mipmap.word_line));
        getListView().setDividerHeight(2);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mOnUnitClickListener = null;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        mCurUnit = position + 1;
        mClickedView = l.getChildAt(position);
        if (mOnUnitClickListener != null) {
            mOnUnitClickListener.getUnit((Unit) l.getItemAtPosition(position));
        }
    }

    public interface onUnitClickListener {
        void getUnit(Unit unit);
    }
}
