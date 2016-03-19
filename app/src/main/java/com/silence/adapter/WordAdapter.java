package com.silence.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.andraskindler.quickscroll.Scrollable;
import com.silence.pojo.Word;
import com.silence.word.R;

import java.util.List;

/**
 * Created by Silence on 2016/3/18 0018.
 */
public class WordAdapter extends BaseAdapter implements Scrollable {
    private Context mContext;
    private List<Word> mWordList;

    public WordAdapter(Context context, List<Word> wordList) {
        mContext = context;
        mWordList = wordList;
    }

    @Override
    public int getCount() {
        return mWordList != null ? mWordList.size() : 0;
    }

    @Override
    public Object getItem(int position) {
        return mWordList != null ? mWordList.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_word, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.tvWord = (TextView) convertView.findViewById(R.id.tv_item_word);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.tvWord.setText(mWordList.get(position).getKey());
        return convertView;
    }

    @Override
    public String getIndicatorForPosition(int childposition, int groupposition) {
        return String.valueOf(mWordList.get(childposition).getKey().charAt(0));
    }

    @Override
    public int getScrollPosition(int childposition, int groupposition) {
        return childposition;
    }

    static class ViewHolder {
        TextView tvWord;
    }
}
