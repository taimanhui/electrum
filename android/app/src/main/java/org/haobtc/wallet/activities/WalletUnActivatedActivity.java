package org.haobtc.wallet.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.LayoutRes;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.activities.personalwallet.SingleSigWalletCreator;
import org.haobtc.wallet.activities.service.CommunicationModeSelector;
import org.haobtc.wallet.activities.settings.recovery_set.RecoveryActivity;
import org.haobtc.wallet.aop.SingleClick;
import org.haobtc.wallet.event.InitEvent;
import org.haobtc.wallet.event.ResultEvent;
import org.haobtc.wallet.event.SendXpubToSigwallet;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static org.haobtc.wallet.activities.service.CommunicationModeSelector.isNFC;

public class WalletUnActivatedActivity extends BaseActivity {
    public static final String TAG = "org.wallet.activities.WalletUnActivatedActivity";
    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.button_activate)
    Button buttonActivate;
    @BindView(R.id.button_recover)
    Button buttonRecover;
    @BindView(R.id.img_use_se)
    ImageView useSe;
    @BindView(R.id.linear_use_se)
    LinearLayout linearUseSe;
    private String tagXpub = "";
    private boolean buseSe = false;

    @Override
    public int getLayoutId() {
        return R.layout.activate;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        Intent intent = getIntent();
        tagXpub = intent.getStringExtra("tag_Xpub");
    }

    @Override
    public void initData() {
        EventBus.getDefault().register(this);
    }


    @SingleClick(value = 2000)
    @OnClick({R.id.img_back, R.id.button_recover, R.id.linear_use_se, R.id.button_activate})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.button_recover:
                startActivity(new Intent(this, RecoveryActivity.class));
                finish();
                break;
            case R.id.linear_use_se:
                if (buseSe) {
                    buseSe = false;
                    useSe.setImageDrawable(getDrawable(R.drawable.circle_empty));
                } else {
                    buseSe = true;
                    useSe.setImageDrawable(getDrawable(R.drawable.chenggong));
                }
                break;
            case R.id.button_activate:
                //Select backup method
                if (isNFC) {
                    Intent intent = new Intent(this, CommunicationModeSelector.class);
                    if (SingleSigWalletCreator.TAG.equals(tagXpub)) {
                        intent.putExtra("tag", SingleSigWalletCreator.TAG);
                    }
                    intent.putExtra("use_se", buseSe).setAction("init");
                    startActivity(intent);
                } else {
                    EventBus.getDefault().post(new InitEvent("Activate", buseSe));
                }
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + view.getId());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
    @Subscribe(threadMode = ThreadMode.MAIN_ORDERED)
    public void showProcessing(ResultEvent resultEvent) {
        EventBus.getDefault().removeStickyEvent(ResultEvent.class);
        switch (resultEvent.getResult()) {
            case "1":
                if (SingleSigWalletCreator.TAG.equals(tagXpub)) {
                    EventBus.getDefault().post(new SendXpubToSigwallet("get_xpub_and_send"));
                }
                startActivity(new Intent(this, ActivatePromptPIN.class));
                finish();
                break;
            case "0":
                Log.d(TAG, "设备激活失败");
                startActivity(new Intent(this, ActiveFailedActivity.class));
                finish();
                break;
            default:

                throw new IllegalStateException("Unexpected value: " + resultEvent.getResult());
        }
    }
}
