package org.haobtc.wallet.activities.base;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;

import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import org.haobtc.wallet.BuildConfig;
import org.haobtc.wallet.MainActivity;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.CreateWalletActivity;
import org.haobtc.wallet.activities.GuideActivity;
import org.haobtc.wallet.utils.Daemon;
import org.haobtc.wallet.utils.Global;

public class LunchActivity extends BaseActivity {
    private final String FIRST_RUN = "is_first_run";

    @Override
    public int getLayoutId() {
        return R.layout.activity_lunch;
    }

    @Override
    public void initView() {
//
    }

    @SuppressLint("HandlerLeak")
    private Handler nHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            long t = System.currentTimeMillis();
            Global.app = MyApplication.getInstance();
            Python.start(new AndroidPlatform(Global.app));
            Global.py = Python.getInstance();
            if (BuildConfig.net_type.equals(getResources().getString(R.string.TestNet))) {
                Global.py.getModule("electrum.constants").callAttr("set_testnet");
            } else if (BuildConfig.net_type.equals(getResources().getString(R.string.RegTest))) {
                Global.py.getModule("electrum.constants").callAttr("set_regtest");
            }
            Log.i("JXM", "t4 = " + (System.currentTimeMillis() - t));
            Global.mHandler = new Handler(Looper.getMainLooper());
            Global.guiDaemon = Global.py.getModule("electrum_gui.android.daemon");
            Global.guiConsole = Global.py.getModule("electrum_gui.android.console");
            new Daemon();
            init();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        nHandler.sendEmptyMessageDelayed(0, 100);
    }

    private void init() {
        SharedPreferences preferences = getSharedPreferences("preferences", MODE_PRIVATE);
        String language = preferences.getString("language", "");
        judgeLanguage(language);

        boolean jumpOr = preferences.getBoolean("JumpOr", true);
        if (preferences.getBoolean(FIRST_RUN, false)) {
            Intent intent = new Intent(LunchActivity.this, MainActivity.class);
            startActivity(intent);
            finish();

        } else {
            if (jumpOr) {
                //splash
                initGuide();
            } else {
                //CreatWallet
                initCreatWallet();
            }

        }
    }

    //switch language
    private void judgeLanguage(String language) {
        if (!TextUtils.isEmpty(language)) {
            if (language.equals("English")) {
                mTextEnglish();
            } else {
                mTextChinese();
            }
        }

    }

    private void initGuide() {
        Intent intent = new Intent(LunchActivity.this, GuideActivity.class);
        startActivity(intent);
        finish();

    }

    private void initCreatWallet() {
        Intent intent = new Intent(LunchActivity.this, CreateWalletActivity.class);
        startActivity(intent);
        finish();

    }

    @Override
    public void initData() {

    }
}
