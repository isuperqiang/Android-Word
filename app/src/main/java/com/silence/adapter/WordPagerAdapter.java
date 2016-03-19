package com.silence.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.silence.fragment.DetailFgt;
import com.silence.pojo.Word;

import java.util.List;

/**
 * Created by Silence on 2016/2/9 0009.
 */
public class WordPagerAdapter extends FragmentStatePagerAdapter {

    private List<Word> mWordList;
    private SparseArray<Fragment> mFragments;

    public WordPagerAdapter(FragmentManager fm, List<Word> wordList) {
        super(fm);
        mWordList = wordList;
        mFragments=new SparseArray<>(mWordList.size());
    }

    @Override
    public Fragment getItem(int position) {
        DetailFgt detailFgt = DetailFgt.newInstance(mWordList.get(position));
        mFragments.put(position, detailFgt);
        return detailFgt;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        super.destroyItem(container, position, object);
        mFragments.remove(position);
    }

    @Override
    public int getCount() {
        return mWordList.size();
    }

    public Fragment getFragment(int position) {
        return mFragments.get(position);
    }
}
