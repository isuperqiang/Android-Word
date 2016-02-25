package com.silence.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.silence.adapter.WordPagerAdapter;
import com.silence.dao.UnitDao;
import com.silence.dao.WordDao;
import com.silence.pojo.Word;
import com.silence.utils.Const;
import com.silence.word.R;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class DetailActivity extends AppCompatActivity implements View.OnClickListener, ViewPager.OnPageChangeListener {
    private static final int MSG_REFRESH = 0x1;
    private int mLevel = 0;// 2, 1, 0, -1, -2分别代表极慢、稍慢、普通、稍快、极快。
    private List<Word> mWordList;
    private int mWordKey;
    private TextView tvPlay;
    private boolean mIsPlaying = false;
    private Timer mTimer;
    private PlayHandler mPlayHandler;
    private long mTime;
    private String mMetaKey;
    private int mUnitKey;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        Intent intent = getIntent();
        mMetaKey = intent.getStringExtra(Const.META_KEY);
        mUnitKey = intent.getIntExtra(Const.UNIT_KEY, 1);
        mWordKey = intent.getIntExtra(Const.WORD_KEY, 1);
        WordDao wordDao = new WordDao(this);
        mWordList = wordDao.getWords(mMetaKey, mUnitKey);
        mPlayHandler = new PlayHandler(this);
        initViews();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mTime = System.currentTimeMillis();
    }

    @Override
    protected void onStop() {
        super.onStop();
        long readTime = System.currentTimeMillis() - mTime;
        UnitDao unitDao = new UnitDao(this);
        unitDao.updateTime(mMetaKey, mUnitKey, readTime);
        if (mIsPlaying) {
            mTimer.cancel();
        }
    }

    private void initViews() {
        tvPlay = (TextView) findViewById(R.id.tv_play);
        tvPlay.setOnClickListener(this);
        findViewById(R.id.btn_prev).setOnClickListener(this);
        findViewById(R.id.btn_next).setOnClickListener(this);
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mViewPager.setAdapter(new WordPagerAdapter(getSupportFragmentManager(), mWordList));
        mViewPager.setCurrentItem(mWordKey);
        mViewPager.addOnPageChangeListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_prev:
                if (mIsPlaying) {
                    pause();
                }
                if (mWordKey - 1 < 0) {
                    Toast.makeText(this, R.string.first_page, Toast.LENGTH_SHORT).show();
                    break;
                } else {
                    mWordKey--;
                }
                mViewPager.setCurrentItem(mWordKey);
                break;
            case R.id.tv_play:
                if (mIsPlaying) {
                    pause();
                } else {
                    play();
                }
                break;
            case R.id.btn_next:
                if (mIsPlaying) {
                    pause();
                }
                if (mWordKey + 1 >= mWordList.size()) {
                    Toast.makeText(this, R.string.last_page, Toast.LENGTH_SHORT).show();
                    break;
                } else {
                    mWordKey++;
                }
                mViewPager.setCurrentItem(mWordKey);
                break;
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        mWordKey = position;
        setTitle((mWordKey + 1) + "/" + mWordList.size());
    }

    @Override
    public void onPageSelected(int position) {

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private static class PlayHandler extends Handler {
        private WeakReference<Context> mWeakReference;

        public PlayHandler(DetailActivity detailActivity) {
            mWeakReference = new WeakReference<Context>(detailActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            DetailActivity detailActivity = (DetailActivity) mWeakReference.get();
            if (detailActivity != null) {
                if (msg.what == MSG_REFRESH) {
                    if (detailActivity.mWordKey + 1 > detailActivity.mWordList.size()) {
                        detailActivity.pause();
                        Toast.makeText(detailActivity, R.string.last_page, Toast.LENGTH_SHORT).show();
                    } else {
                        detailActivity.mViewPager.setCurrentItem(detailActivity.mWordKey);
                    }
                }
            }
        }
    }

    private void pause() {
        Drawable drawable = getResources().getDrawable(R.drawable.btn_play_selector);
        if (drawable != null) {
            drawable.setBounds(0, 0, 51, 51);
        }
        tvPlay.setCompoundDrawables(null, drawable, null, null);
        tvPlay.setText(R.string.tv_play);
        mIsPlaying = false;
        mTimer.cancel();
    }

    private void play() {
        Drawable drawable = getResources().getDrawable(R.drawable.btn_pause_selector);
        if (drawable != null) {
            drawable.setBounds(0, 0, 51, 51);
        }
        tvPlay.setCompoundDrawables(null, drawable, null, null);
        tvPlay.setText(R.string.tv_pause);
        mIsPlaying = true;
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                mWordKey++;
                mPlayHandler.sendEmptyMessage(MSG_REFRESH);
            }
        };
        long period;
        switch (mLevel) {
            case -2:
                period = 500;
                break;
            case -1:
                period = 1500;
                break;
            case 0:
                period = 2500;
                break;
            case 1:
                period = 3500;
                break;
            case 2:
                period = 4500;
                break;
            default:
                period = 2500;
                break;
        }
        mTimer = new Timer();
        mTimer.schedule(timerTask, 0, period);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPlayHandler.removeCallbacksAndMessages(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_setting, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mIsPlaying) {
            pause();
        }
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else if (item.getItemId() == R.id.menu_item_setting) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.select_speed).setSingleChoiceItems(getResources().getStringArray
                    (R.array.speed_type), mLevel + 2, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    mLevel = which - 2;
                    dialog.dismiss();
                }
            }).show();
        }
        return true;
    }
}