package org.haobtc.wallet.activities.base;

import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.chaquo.python.Kwarg;

import org.haobtc.wallet.BuildConfig;
import org.haobtc.wallet.MainActivity;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.CreateWalletActivity;
import org.haobtc.wallet.activities.GuideActivity;
import org.haobtc.wallet.utils.Daemon;
import org.haobtc.wallet.utils.Global;


public class LunchActivity extends BaseActivity {

    private SharedPreferences preferences;

    @Override
    public int getLayoutId() {
        return R.layout.activity_lunch;
    }

    @Override
    public void initView() {
        preferences = getSharedPreferences("Preferences", MODE_PRIVATE);
        SharedPreferences.Editor edit = preferences.edit();
        edit.putBoolean("haveCreateNopass", false);//No password is required to create app wallet for the first time
        edit.apply();
   }

    private void init() {
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
        intent.putExtra("intentWhere","lunch");
        startActivity(intent);
        finish();

    }

    @Override
    public void initData() {
            if (BuildConfig.net_type.equals(getString(R.string.TestNet))) {
                Global.py.getModule("electrum.constants").callAttr("set_testnet");
            } else if (BuildConfig.net_type.equals(getString(R.string.RegTest))) {
                Global.py.getModule("electrum.constants").callAttr("set_regtest");
            }
            Global.guiDaemon = Global.py.getModule("electrum_gui.android.daemon");
            Global.guiConsole = Global.py.getModule("electrum_gui.android.console");
            try {
                Daemon.commands = Global.guiConsole.callAttr("AndroidCommands", new Kwarg("callback", Daemon.getInstance()));
            } catch (Exception ignored) {
                ignored.printStackTrace();
            }
            //Daemon.commands.callAttr("start", Daemon.getInstance());
            //Daemon.commands.callAttr("set_callback_fun", Daemon.getInstance());
            init();
    }
}
