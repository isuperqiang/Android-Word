package com.silence.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.silence.fragment.DetailFgt;
import com.silence.pojo.Word;

import java.util.List;

/**
 * Created by Silence on 2016/2/9 0009.
 */
public class WordPagerAdapter extends FragmentStatePagerAdapter {

    private List<Word> mWordList;

    public WordPagerAdapter(FragmentManager fm, List<Word> wordList) {
        super(fm);
        mWordList = wordList;
    }

    @Override
    public Fragment getItem(int position) {
        return DetailFgt.newInstance(mWordList.get(position));
    }

    @Override
    public int getCount() {
        return mWordList.size();
    }
}
