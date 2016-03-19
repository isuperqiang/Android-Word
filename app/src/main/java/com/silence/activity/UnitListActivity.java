package com.silence.activity;

import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.SearchView;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.silence.dao.WordDao;
import com.silence.fragment.DetailFgt;
import com.silence.fragment.DetailFgt.onSpeechListener;
import com.silence.fragment.SearchFgt;
import com.silence.fragment.UnitListFgt;
import com.silence.pojo.Unit;
import com.silence.pojo.Word;
import com.silence.utils.Const;
import com.silence.utils.WavWriter;
import com.silence.word.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class UnitListActivity extends AppCompatActivity implements SearchView.OnQueryTextListener,
        SearchFgt.onSearchClickListener, UnitListFgt.onUnitClickListener, onSpeechListener {
    private UnitListFgt mUnitListFgt;
    private SearchFgt mSearchFgt;
    private DetailFgt mDetailFgt;
    private SearchView mSearchView;
    private String mMetaKey;
    private WordDao mWordDao;
    private ActionBar mActionBar;
    private FragmentManager mFragmentManager;
    private SpeechSynthesizer mSynthesizer;
    private MediaPlayer mMediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
        }
        mFragmentManager = getSupportFragmentManager();
        mMetaKey = getIntent().getStringExtra(Const.META_KEY);
        setActionTitle();
        if (savedInstanceState == null) {
            mUnitListFgt = UnitListFgt.getInstance(mMetaKey);
            mFragmentManager.beginTransaction().add(R.id.unit_content, mUnitListFgt).commit();
        }
        initSpeech();
        initPlayer();
    }

    private void setActionTitle() {
        int resId = 0;
        switch (mMetaKey) {
            case Const.WORDS_NMET:
                resId = R.string.nmet;
                break;
            case Const.WORDS_CET6:
                resId = R.string.cet6;
                break;
            case Const.WORDS_CET4:
                resId = R.string.cet4;
                break;
            case Const.WORDS_GRE:
                resId = R.string.gre;
                break;
            case Const.WORDS_IETSL:
                resId = R.string.ietsl;
                break;
        }
        setTitle(resId == 0 ? null : getResources().getString(resId));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);
        MenuItem item = menu.findItem(R.id.menu_item_search);
        mSearchView = (SearchView) item.getActionView();
        if (mSearchView != null) {
            mSearchView.setInputType(InputType.TYPE_CLASS_TEXT);
            mSearchView.setOnSearchClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mActionBar.setDisplayHomeAsUpEnabled(true);
                    mWordDao = new WordDao(UnitListActivity.this);
                    FragmentTransaction transaction = mFragmentManager.beginTransaction();
                    transaction.hide(mUnitListFgt);
                    if (mSearchFgt == null) {
                        mSearchFgt = SearchFgt.newInstance(mMetaKey);
                        transaction.add(R.id.unit_content, mSearchFgt);
                    } else {
                        transaction.show(mSearchFgt);
                    }
                    transaction.commit();
                }
            });
            mSearchView.setQueryHint(getString(R.string.search_hint));
            mSearchView.setOnQueryTextListener(this);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            FragmentTransaction transaction = mFragmentManager.beginTransaction();
            if (mSearchFgt != null && mSearchFgt.isVisible()) {
                hideSearchFgt(transaction);
            } else if (mDetailFgt != null && mDetailFgt.isVisible()) {
                hideDetailFgt(transaction);
            } else {
                finish();
            }
            transaction.commit();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void hideDetailFgt(FragmentTransaction transaction) {
        setTitle(null);
        mSearchView.setIconified(false);
        mSearchView.setVisibility(View.VISIBLE);
        transaction.remove(mDetailFgt);
        transaction.show(mSearchFgt);
    }

    private void hideSearchFgt(FragmentTransaction transaction) {
        mSearchView.clearFocus();
        mSearchView.setQuery(null, false);
        mSearchView.setIconified(true);
        setActionTitle();
        transaction.hide(mSearchFgt);
        transaction.show(mUnitListFgt);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        ArrayList<Word> words = null;
        if (!TextUtils.isEmpty(newText)) {
            words = mWordDao.queryWords(mMetaKey, newText);
        }
        mSearchFgt.refresh(words);
        return true;
    }

    @Override
    public void getWord(Word word) {
        mSearchView.setVisibility(View.INVISIBLE);
        mSearchView.setQuery(null, false);
        mSearchView.setIconified(true);
        setTitle("查询结果");
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.hide(mSearchFgt);
        mDetailFgt = DetailFgt.newInstance(word);
        transaction.setCustomAnimations(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        transaction.add(R.id.unit_content, mDetailFgt);
        transaction.commit();
    }

    @Override
    public void getUnit(Unit unit) {
        Intent intent = new Intent(this, WordListActivity.class);
        intent.putExtra(Const.UNIT_KEY, unit);
        startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        if (mSearchFgt != null && mSearchFgt.isVisible()) {
            hideSearchFgt(transaction);
        } else if (mDetailFgt != null && mDetailFgt.isVisible()) {
            hideDetailFgt(transaction);
        } else {
            super.onBackPressed();
        }
        transaction.commit();
    }

    private void initSpeech() {
        mSynthesizer = SpeechSynthesizer.createSynthesizer(this, null);
        mSynthesizer.setParameter(SpeechConstant.VOICE_NAME, "catherine"); //设置发音人
        mSynthesizer.setParameter(SpeechConstant.SPEED, "50");//设置语速
        mSynthesizer.setParameter(SpeechConstant.VOLUME, "80");//设置音量，范围 0~100
        mSynthesizer.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD); //设置云端
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
                mDetailFgt.setSpeakImg(R.mipmap.icon_speaker_off);
            }
        });
    }

    @Override
    public void speech(Word word) {
        mDetailFgt.setSpeakImg(R.mipmap.icon_speaker_on);
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
                        mDetailFgt.setSpeakImg(R.mipmap.icon_speaker_off);
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
    }
}
