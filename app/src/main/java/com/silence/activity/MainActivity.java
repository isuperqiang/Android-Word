package com.silence.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.silence.utils.Const;
import com.silence.word.R;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private long exitTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.iv_cet4).setOnClickListener(this);
        findViewById(R.id.iv_cet6).setOnClickListener(this);
        findViewById(R.id.iv_gre).setOnClickListener(this);
        findViewById(R.id.iv_ietsl).setOnClickListener(this);
        findViewById(R.id.iv_nmet).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        Intent intent = new Intent();
        switch (v.getId()) {
            case R.id.iv_nmet:
                intent.setClass(this, UnitListActivity.class);
                intent.putExtra(Const.META_KEY, Const.WORDS_NMET);
                break;
            case R.id.iv_cet4:
                intent.setClass(this, UnitListActivity.class);
                intent.putExtra(Const.META_KEY, Const.WORDS_CET4);
                break;
            case R.id.iv_cet6:
                intent.setClass(this, UnitListActivity.class);
                intent.putExtra(Const.META_KEY, Const.WORDS_CET6);
                break;
            case R.id.iv_ietsl:
                intent.setClass(this, UnitListActivity.class);
                intent.putExtra(Const.META_KEY, Const.WORDS_IETSL);
                break;
            case R.id.iv_gre:
                intent.setClass(this, UnitListActivity.class);
                intent.putExtra(Const.META_KEY, Const.WORDS_GRE);
                break;
        }
        startActivity(intent);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(this, R.string.exit_hint, Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}