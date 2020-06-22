package org.haobtc.wallet.activities;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.activities.personalwallet.SingleSigWalletCreator;
import org.haobtc.wallet.activities.service.CommunicationModeSelector;
import org.haobtc.wallet.activities.settings.recovery_set.RecoveryActivity;
import org.haobtc.wallet.aop.SingleClick;
import org.haobtc.wallet.event.FinishEvent;
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
    private String tag_xpub = "";
    private boolean use_se = false;

    @Override
    public int getLayoutId() {
        return R.layout.activate;
    }

    public void initView() {
        ButterKnife.bind(this);
        Intent intent = getIntent();
        tag_xpub = intent.getStringExtra("tag_Xpub");
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
                if (use_se) {
                    use_se = false;
                    useSe.setImageDrawable(getDrawable(R.drawable.circle_empty));
                } else {
                    use_se = true;
                    useSe.setImageDrawable(getDrawable(R.drawable.chenggong));
                }
                break;
            case R.id.button_activate:
//                Intent intent = new Intent(this, ActivatedProcessing.class);
//                intent.putExtra("tag_xpub", tag_xpub);
//                intent.putExtra("use_se", use_se);
//                startActivity(intent);
//                finish();
                if (isNFC) {
                   Intent intent = new Intent(this, CommunicationModeSelector.class);
                    if (SingleSigWalletCreator.TAG.equals(tag_xpub)) {
                        intent.putExtra("tag", SingleSigWalletCreator.TAG);
                    }
                   intent.putExtra("use_se", use_se).setAction("init");
                   startActivity(intent);
                } else {
                    EventBus.getDefault().post(new InitEvent("Activate", use_se));
                }
                break;
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
//                startActivity(new Intent(this, ActivateSuccessActivity.class));
                if (SingleSigWalletCreator.TAG.equals(tag_xpub)) {
                    EventBus.getDefault().post(new SendXpubToSigwallet("get_xpub_and_send"));
                }
                startActivity(new Intent(this, ActivatePromptPIN.class));
                finish();
                break;
            case "0":
                Log.d(TAG, "设备激活失败");
                startActivity(new Intent(this, ActiveFailedActivity.class));
                finish();
        }
    }
}
