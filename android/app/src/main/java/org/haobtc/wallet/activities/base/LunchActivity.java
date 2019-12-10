package org.haobtc.wallet.activities.base;

import android.content.Intent;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.LinearLayout;

import org.haobtc.wallet.MainActivity;
import org.haobtc.wallet.R;

public class LunchActivity extends BaseActivity {

    @Override
    public int getLayoutId() {
        return R.layout.activity_lunch;
    }

    @Override
    public void initView() {
        LinearLayout layoutSplash = (LinearLayout) findViewById(R.id.lin_Splash);
        AlphaAnimation alphaAnimation = new AlphaAnimation(0.1f, 1.0f);
        alphaAnimation.setDuration(1000);
        layoutSplash.startAnimation(alphaAnimation);
        alphaAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                Intent intent = new Intent(LunchActivity.this, MainActivity.class);
                startActivity(intent);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
    }

    @Override
    public void initData() {

    }
}
