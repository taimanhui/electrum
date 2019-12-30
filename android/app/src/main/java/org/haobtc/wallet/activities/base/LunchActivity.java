package org.haobtc.wallet.activities.base;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
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

        boolean jumpOr = preferences.getBoolean("JumpOr", true);
        if (preferences.getBoolean(FIRST_RUN, false)) {
            Intent intent = new Intent(this, MainActivity.class);
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

    private void initGuide() {
        Intent intent = new Intent(this, GuideActivity.class);
        startActivity(intent);
        finish();
    }

    private void initCreatWallet() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(LunchActivity.this, CreateWalletActivity.class);
                startActivity(intent);
                finish();
            }
        },1500);

    }

    @Override
    public void initData() {

    }
}
