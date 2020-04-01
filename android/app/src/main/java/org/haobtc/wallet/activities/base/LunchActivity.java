package org.haobtc.wallet.activities.base;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.chaquo.python.Python;

import org.haobtc.wallet.BuildConfig;
import org.haobtc.wallet.MainActivity;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.CreateWalletActivity;
import org.haobtc.wallet.activities.GuideActivity;
import org.haobtc.wallet.utils.Daemon;
import org.haobtc.wallet.utils.Global;

import java.lang.ref.WeakReference;

public class LunchActivity extends BaseActivity {

    @Override
    public int getLayoutId() {
        return R.layout.activity_lunch;
    }

    @Override
    public void initView() {
    }

    @SuppressLint("HandlerLeak")
    private Handler nHandler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            Global.py = Python.getInstance();
            if (BuildConfig.net_type.equals(getString(R.string.TestNet))) {
                Global.py.getModule("electrum.constants").callAttr("set_testnet");
            } else if (BuildConfig.net_type.equals(getString(R.string.RegTest))) {
                Global.py.getModule("electrum.constants").callAttr("set_regtest");
            }
            Global.guiDaemon = Global.py.getModule("electrum_gui.android.daemon");
            Global.guiConsole = Global.py.getModule("electrum_gui.android.console");
            Daemon.daemonWeakReference = new WeakReference<>(new Daemon());
            Daemon.commands.callAttr("set_callback_fun", Daemon.daemonWeakReference.get());
            init();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        nHandler.sendEmptyMessageDelayed(0, 100);
    }

    private void init() {
        SharedPreferences preferences = getSharedPreferences("Preferences", MODE_PRIVATE);
        String language = preferences.getString("language", "");
        judgeLanguage(language);

        boolean jumpOr = preferences.getBoolean("JumpOr", false);
        String FIRST_RUN = "is_first_run";
        if (preferences.getBoolean(FIRST_RUN, false)) {
            Intent intent = new Intent(LunchActivity.this, MainActivity.class);
            startActivity(intent);
            finish();

        } else {
            if (jumpOr) {
                //CreatWallet
                initCreatWallet();
            } else {
                //splash
                initGuide();
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
