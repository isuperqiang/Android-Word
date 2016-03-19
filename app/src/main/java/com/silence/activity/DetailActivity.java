package com.silence.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.silence.adapter.WordPagerAdapter;
import com.silence.dao.UnitDao;
import com.silence.dao.WordDao;
import com.silence.fragment.DetailFgt;
import com.silence.pojo.Word;
import com.silence.utils.Const;
import com.silence.utils.WavWriter;
import com.silence.word.R;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class DetailActivity extends AppCompatActivity implements View.OnClickListener,
        ViewPager.OnPageChangeListener, DetailFgt.onSpeechListener {
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
    private SpeechSynthesizer mSynthesizer;
    private MediaPlayer mMediaPlayer;
    private WordPagerAdapter mWordPagerAdapter;
    private SharedPreferences mSharedPreferences;
    private boolean mIsAutoSpeak;

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
        initSpeech();
        initPlayer();
        setTitle((mWordKey + 1) + "/" + mWordList.size());
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mIsAutoSpeak = mSharedPreferences.getBoolean(Const.AUTO_SPEAK, true);
    }

    private void initPlayer() {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mMediaPlayer.start();
            }
        });
        mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                mMediaPlayer.reset();
                return true;
            }
        });
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                DetailFgt item = (DetailFgt) mWordPagerAdapter.getFragment(mWordKey);
                item.setSpeakImg(R.mipmap.icon_speaker_off);
            }
        });
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
        mWordPagerAdapter = new WordPagerAdapter(getSupportFragmentManager(), mWordList);
        mViewPager.setAdapter(mWordPagerAdapter);
        mViewPager.setCurrentItem(mWordKey);
        mViewPager.addOnPageChangeListener(this);
        mViewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
    }

    private void initSpeech() {
        mSynthesizer = SpeechSynthesizer.createSynthesizer(this, null);
        mSynthesizer.setParameter(SpeechConstant.VOICE_NAME, "catherine"); //设置发音人
        mSynthesizer.setParameter(SpeechConstant.SPEED, "50");//设置语速
        mSynthesizer.setParameter(SpeechConstant.VOLUME, "80");//设置音量，范围 0~100
        mSynthesizer.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD); //设置云端
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

    }

    @Override
    public void onPageSelected(int position) {
        mWordKey = position;
        setTitle((mWordKey + 1) + "/" + mWordList.size());
        if (mIsAutoSpeak) {
            if (mIsPlaying && mLevel < 0) {
                Toast.makeText(this, R.string.toast_too_fast, Toast.LENGTH_SHORT).show();
                pause();
            } else {
                speech(mWordList.get(position));
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void speech(Word word) {
        DetailFgt item = (DetailFgt) mWordPagerAdapter.getFragment(mWordKey);
        item.setSpeakImg(R.mipmap.icon_speaker_on);
        String content = word.getKey();
        String path = getExternalCacheDir() + File.separator + "word_" + content + ".pcm";
        final File file = new File(path);
        if (file.exists()) {
            try {
                if (mMediaPlayer.isPlaying()) {
                    mMediaPlayer.stop();
                }
                mMediaPlayer.reset();
                mMediaPlayer.setDataSource(this, Uri.fromFile(file));
                mMediaPlayer.prepareAsync();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            mSynthesizer.setParameter(SpeechConstant.TTS_AUDIO_PATH, path);
            SynthesizerListener synListener = new SynthesizerListener() {
                public void onCompleted(SpeechError error) {
                    if (error == null) {
                        try {
                            WavWriter wavWriter = new WavWriter(file, 16000);
                            wavWriter.writeHeader();
                            wavWriter.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        DetailFgt item = (DetailFgt) mWordPagerAdapter.getFragment(mWordKey);
                        item.setSpeakImg(R.mipmap.icon_speaker_off);
                    }
                }

                public void onBufferProgress(int percent, int beginPos, int endPos, String info) {
                }

                public void onSpeakBegin() {
                }

                public void onSpeakPaused() {
                }

                public void onSpeakProgress(int percent, int beginPos, int endPos) {
                }

                public void onSpeakResumed() {
                }

                public void onEvent(int arg0, int arg1, int arg2, Bundle arg3) {
                }
            };
            mSynthesizer.startSpeaking(content, synListener);
        }
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
        speech(mWordList.get(mWordKey));
        mWordKey -= 1;
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
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_setting, menu);
        menu.findItem(R.id.menu_item_speak).setChecked(mIsAutoSpeak);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mIsPlaying) {
            pause();
        }
        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();
            }
            break;
            case R.id.menu_item_speak: {
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                if (item.isChecked()) {
                    item.setChecked(false);
                    mIsAutoSpeak = false;
                    editor.putBoolean(Const.AUTO_SPEAK, false);
                } else {
                    item.setChecked(true);
                    mIsAutoSpeak = true;
                    editor.putBoolean(Const.AUTO_SPEAK, true);
                }
                editor.apply();
            }
            break;
            case R.id.menu_item_speed: {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.select_speed).setSingleChoiceItems(getResources().getStringArray
                                (R.array.speed_type), mSharedPreferences.getInt(Const.PLAY_SPEED, mLevel) + 2,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                mLevel = which - 2;
                                mSharedPreferences.edit().putInt(Const.PLAY_SPEED, mLevel).apply();
                                dialog.dismiss();
                            }
                        }).show();
            }
            break;
        }
        return true;
    }

}