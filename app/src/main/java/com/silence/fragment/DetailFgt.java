package com.silence.fragment;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.silence.pojo.Word;
import com.silence.utils.Const;
import com.silence.word.R;

/**
 * Created by Silence on 2016/2/9 0009.
 */
public class DetailFgt extends Fragment {

    public static DetailFgt newInstance(Word word) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(Const.WORD_KEY, word);
        DetailFgt detailFgt = new DetailFgt();
        detailFgt.setArguments(bundle);
        return detailFgt;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail, container, false);
        TextView tvExample = (TextView) view.findViewById(R.id.tv_exam);
        TextView tvKey = (TextView) view.findViewById(R.id.tv_key);
        TextView tvPhono = (TextView) view.findViewById(R.id.tv_phono);
        TextView tvTrans = (TextView) view.findViewById(R.id.tv_trans);
        Word word = getArguments().getParcelable(Const.WORD_KEY);
        if (word != null) {
            tvExample.setText(word.getExample());
            tvKey.setText(word.getKey());
            tvPhono.setText("[" + word.getPhono() + "]");
            tvTrans.setText(word.getTrans());
        }
        return view;
    }
}
