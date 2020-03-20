package org.haobtc.wallet.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.set.VerificationSuccessActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class VerificationKEYActivity extends AppCompatActivity {

    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.img_jump)
    ImageView imgJump;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serviceonline);
        ButterKnife.bind(this);
        mWhiteinitState();

    }

    @SuppressLint("ObsoleteSdkInt")
    private void mWhiteinitState() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        }
    }


    @OnClick({R.id.img_back,R.id.img_jump})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.img_jump:
                Intent intent = new Intent(VerificationKEYActivity.this, VerificationSuccessActivity.class);
                startActivity(intent);
                break;
        }
    }

}
