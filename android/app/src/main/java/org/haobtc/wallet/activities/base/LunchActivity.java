package org.haobtc.wallet.activities.base;

import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import org.haobtc.wallet.MainActivity;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.CreateWalletActivity;
import org.haobtc.wallet.activities.GuideActivity;

public class LunchActivity extends BaseActivity {
    private final String FIRST_RUN = "is_first_run";

    @Override
    public int getLayoutId() {
        return R.layout.activity_lunch;
    }

    @Override
    public void initView() {
        init();

    }

    private void init() {
        SharedPreferences preferences = getSharedPreferences("preferences", MODE_PRIVATE);
        String language = preferences.getString("language", "");
        judgeLanguage(language);

        boolean jumpOr = preferences.getBoolean("JumpOr", true);
        if (preferences.getBoolean(FIRST_RUN, false)) {
//            Handler handler = new Handler();
//            handler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
            Intent intent = new Intent(LunchActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
//                }
//            }, 1500);

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
            if (language.equals("Chinese")) {
                mTextChinese();
            } else if (language.equals("English")) {
                mTextEnglish();
            }
        }

    }

    private void initGuide() {
//        Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
        Intent intent = new Intent(LunchActivity.this, GuideActivity.class);
        startActivity(intent);
        finish();
//            }
//        }, 1500);

    }

    private void initCreatWallet() {
//        Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
        Intent intent = new Intent(LunchActivity.this, CreateWalletActivity.class);
        startActivity(intent);
        finish();

//            }
//        }, 1500);

    }

    @Override
    public void initData() {

    }
}
