package com.silence.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.silence.adapter.CommonAdapter;
import com.silence.dao.WordDao;
import com.silence.pojo.Word;
import com.silence.utils.Const;
import com.silence.word.R;

import java.util.List;

/**
 * Created by Silence on 2016/2/8 0008.
 */
public class WordListFgt extends ListFragment {

    private onWordClickListener mOnWordClickListener;

    public static WordListFgt newInstance(String metaKey, int unitKey) {
        Bundle bundle = new Bundle();
        bundle.putInt(Const.UNIT_KEY, unitKey);
        bundle.putString(Const.META_KEY, metaKey);
        WordListFgt wordListFgt = new WordListFgt();
        wordListFgt.setArguments(bundle);
        return wordListFgt;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof onWordClickListener) {
            mOnWordClickListener = (onWordClickListener) context;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        WordDao wordDao = new WordDao(getActivity());
        Bundle bundle = getArguments();
        List<Word> wordList = wordDao.getWords(bundle.getString(Const.META_KEY), bundle.getInt(Const.UNIT_KEY));
        if (wordList != null) {
            CommonAdapter commonAdapter = new CommonAdapter<Word>(wordList, R.layout.item_word) {
                @Override
                public void bindView(ViewHolder holder, Word obj) {
                    holder.setText(R.id.tv_item_word, obj.getKey());
                }
            };
            setListAdapter(commonAdapter);
        } else {
            Toast.makeText(getActivity(), R.string.no_data, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mOnWordClickListener = null;
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        if (mOnWordClickListener != null) {
            mOnWordClickListener.getWordList(getArguments().getString(Const.META_KEY),
                    getArguments().getInt(Const.UNIT_KEY), position);
        }
    }

    public interface onWordClickListener {
        void getWordList(String metaKey, int unitKey, int wordKey);
    }
}
