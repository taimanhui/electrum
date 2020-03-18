package org.haobtc.wallet.activities.onlywallet;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.chaquo.python.Kwarg;
import com.chaquo.python.PyObject;
import com.yzq.zxinglibrary.common.Constant;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.WalletUnActivatedActivity;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.activities.manywallet.CustomerDialogFragment;
import org.haobtc.wallet.fragment.ReadingPubKeyDialogFragment;
import org.haobtc.wallet.utils.Daemon;
import org.haobtc.wallet.utils.Global;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static org.haobtc.wallet.activities.manywallet.CustomerDialogFragment.REQUEST_ACTIVE;
import static org.haobtc.wallet.activities.manywallet.CustomerDialogFragment.xpub;

public class ImportHistoryWalletActivity extends BaseActivity {

    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.create_trans_one2one)
    Button createTransOne2one;
    private CustomerDialogFragment dialogFragment;
    public static boolean isNfc;
    private boolean executable = true;
    // new version code
    public String pin = "";
    private boolean isInit;
    private boolean isActive;

    @Override
    public int getLayoutId() {
        return R.layout.activity_import_history_wallet;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);

    }

    @Override
    public void initData() {

    }

    @OnClick({R.id.img_back, R.id.create_trans_one2one})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.create_trans_one2one:
                // new version code
                showPopupAddCosigner1();

                break;
        }
    }

    private void showPopupAddCosigner1() {
//        mIntent(ChooseHistryWalletActivity.class);
        dialogFragment = new CustomerDialogFragment("", null, "");
        dialogFragment.show(getSupportFragmentManager(), "");
    }

    private boolean isInitialized() throws Exception {
        boolean isInitialized = false;
        try {
            System.out.println("call is_initialized =====");
            isInitialized = Daemon.commands.callAttr("is_initialized").toBoolean();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        return isInitialized;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String action = intent.getAction(); // get the action of the coming intent
        if (Objects.equals(action, NfcAdapter.ACTION_NDEF_DISCOVERED) // NDEF type
                || Objects.equals(action, NfcAdapter.ACTION_TECH_DISCOVERED)
                || Objects.requireNonNull(action).equals(NfcAdapter.ACTION_TAG_DISCOVERED)) {
            isNfc = true;
            if (executable) {
                Tag tags = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                PyObject nfc = Global.py.getModule("trezorlib.transport.nfc");
                PyObject nfcHandler = nfc.get("NFCHandle");
                nfcHandler.put("device", tags);
                executable = false;
            }
            if (!TextUtils.isEmpty(pin) && isInit) {
                CustomerDialogFragment.customerUI.put("pin", pin);
                try {
                    ReadingPubKeyDialogFragment dialog = (ReadingPubKeyDialogFragment) dialogFragment.showReadingDialog();
                    xpub = CustomerDialogFragment.futureTask.get(40, TimeUnit.SECONDS).toString();
                    dialog.dismiss();
                    Intent intent1 = new Intent(ImportHistoryWalletActivity.this, ChooseHistryWalletActivity.class);
                    intent1.putExtra("histry_xpub", xpub);
                    startActivity(intent1);
                    finish();
                    return;
                } catch (ExecutionException | TimeoutException | InterruptedException e) {
                    dialogFragment.showReadingFailedDialog();
                    if ("com.chaquo.python.PyException: BaseException: (7, 'PIN invalid')".equals(e.getMessage())) {
                        mToast(getResources().getString(R.string.pin_wrong));
                    }
                }
            }
            try {
                isInit = isInitialized();
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(this, "communication error, get firmware info error", Toast.LENGTH_SHORT).show();
                return;
            }
            if (isInit) {
                boolean pinCached = false;
                try {
                    pinCached = Daemon.commands.callAttr("get_pin_status").toBoolean();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // todo: get xpub
                CustomerDialogFragment.futureTask = new FutureTask<>(() -> Daemon.commands.callAttr("get_xpub_from_hw", new Kwarg("_type", "p2wpkh")));
                new Thread(CustomerDialogFragment.futureTask).start();
                if (pinCached) {
                    try {
                        xpub = CustomerDialogFragment.futureTask.get().toString();
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                        return;
                    }
                    Intent intent1 = new Intent(ImportHistoryWalletActivity.this, ChooseHistryWalletActivity.class);
                    intent1.putExtra("histry_xpub", xpub);
                    startActivity(intent1);
                    finish();
                }

            } else {
                // todo: Initialized
                if (isActive) {
                    new Thread(() -> {
                        try {
                            Daemon.commands.callAttr("init");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    ).start();
                } else {
                    Intent intent1 = new Intent(this, WalletUnActivatedActivity.class);
                    startActivityForResult(intent1, REQUEST_ACTIVE);
                }
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CustomerDialogFragment.PIN_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                pin = data.getStringExtra("pin");
                CustomerDialogFragment.pin = pin;
                if (isActive) {
                    CustomerDialogFragment.handler.sendEmptyMessage(CustomerDialogFragment.SHOW_PROCESSING);
                    return;
                }
                if (!isNfc) {
                    CustomerDialogFragment.customerUI.put("pin", pin);
                    pin = "";
                }
                if (CustomerDialogFragment.isActive) {
                    CustomerDialogFragment.handler.sendEmptyMessage(CustomerDialogFragment.SHOW_PROCESSING);
                    return;
                }
                if (!isNfc) {
                    try {
                        xpub = CustomerDialogFragment.futureTask.get(40, TimeUnit.SECONDS).toString();
                        Intent intent1 = new Intent(ImportHistoryWalletActivity.this, ChooseHistryWalletActivity.class);
                        intent1.putExtra("histry_xpub", xpub);
                        startActivity(intent1);
                        finish();
                    } catch (ExecutionException | TimeoutException | InterruptedException e) {
                        dialogFragment.showReadingFailedDialog();
                        if ("com.chaquo.python.PyException: BaseException: (7, 'PIN invalid')".equals(e.getMessage())) {
                            Toast.makeText(this, "PIN码输入有误，请重新输入", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            }
        } else if (requestCode == 0 && resultCode == RESULT_OK) {
            if (data != null) {
                String content = data.getStringExtra(Constant.CODED_CONTENT);
                Log.i("CODED_CONTENT", "content=----: " + content);
//                edit_sweep.setText(content);
            }
        } else if (requestCode == REQUEST_ACTIVE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                isActive = data.getBooleanExtra("isActive", false);
            }
        }
    }

}
