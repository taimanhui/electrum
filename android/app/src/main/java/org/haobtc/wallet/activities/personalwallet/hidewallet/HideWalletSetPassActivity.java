package org.haobtc.wallet.activities.personalwallet.hidewallet;

import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.activities.service.NfcNotifyHelper;
import org.haobtc.wallet.aop.SingleClick;
import org.haobtc.wallet.event.CheckReceiveAddress;
import org.haobtc.wallet.event.FinishEvent;
import org.haobtc.wallet.event.HideInputPassFinishEvent;
import org.haobtc.wallet.event.OperationTimeoutEvent;
import org.haobtc.wallet.event.PinEvent;
import org.haobtc.wallet.utils.Global;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static org.haobtc.wallet.activities.service.CommunicationModeSelector.isNFC;

public class HideWalletSetPassActivity extends BaseActivity {

    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.editNewPass)
    EditText editNewPass;
    @BindView(R.id.bn_next)
    Button bnNext;
    public static final String TAG = "org.haobtc.wallet.activities.personalwallet.hidewallet.HideWalletSetPassActivity";

    @Override
    public int getLayoutId() {
        return R.layout.activity_hide_wallet_set_pass;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);

    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void timeout(OperationTimeoutEvent event) {
        Toast.makeText(this, "passphrase 输入超时", Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public void initData() {

    }

    @SingleClick
    @OnClick({R.id.img_back, R.id.bn_next})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                Global.py.getModule("trezorlib.customer_ui").get("CustomerUI").put("user_cancel", 1);
                finish();
                break;
            case R.id.bn_next:
                String strNewpass = editNewPass.getText().toString();
                if (TextUtils.isEmpty(strNewpass)) {
                    mToast(getString(R.string.please_input_pass));
                    return;
                }
                if (!isNFC) {
                    EventBus.getDefault().post(new PinEvent("", strNewpass));
                    if ("hideWalletReceive".equals(getIntent().getStringExtra("hideWalletReceive"))){
                        EventBus.getDefault().post(new CheckReceiveAddress("checking"));
                        finish();
                    }
                } else {
                    Intent intent = new Intent(this, NfcNotifyHelper.class);
                    intent.putExtra("tag", "Passphrase");
                    intent.putExtra("passphrase", strNewpass);
                    startActivity(intent);
                }
                break;
            default:
                    throw new IllegalStateException("Unexpected value: " + view.getId());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFinish(FinishEvent event) {
        finish();
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void event(HideInputPassFinishEvent event) {
        finish();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
