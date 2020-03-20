package org.haobtc.wallet.activities.set;

import android.app.Activity;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.Nullable;
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
import static org.haobtc.wallet.activities.manywallet.CustomerDialogFragment.executorService;
import static org.haobtc.wallet.activities.manywallet.CustomerDialogFragment.futureTask;
import static org.haobtc.wallet.activities.manywallet.CustomerDialogFragment.isNFC;
import static org.haobtc.wallet.activities.manywallet.CustomerDialogFragment.xpub;

public class VersionUpgradeActivity extends BaseActivity {

    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.btn_toUpgrade)
    Button btnToUpgrade;
    @BindView(R.id.tet_firmware)
    TextView tetFirmware;
    @BindView(R.id.checkBox_firmware)
    CheckBox checkBoxFirmware;
    @BindView(R.id.tet_bluetooth)
    TextView tetBluetooth;
    @BindView(R.id.checkBox_bluetooth)
    CheckBox checkBoxBluetooth;
    private boolean executable = true;
    // new version code
    public String pin = "";
    private boolean ready;
    private boolean isInit;
    private boolean isActive;
    public final static String TAG = VersionUpgradeActivity.class.getSimpleName();
    private int checkWitch = 1;
    private CustomerDialogFragment dialogFragment;

    @Override
    public int getLayoutId() {
        return R.layout.activity_version_upgrade;
    }

    @Override
    public void initView() {
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);

    }

    @Override
    public void initData() {
        checkBoxClick();
    }

    private void checkBoxClick() {
        checkBoxFirmware.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    checkBoxBluetooth.setChecked(false);
                    checkWitch = 1;
                } else {
                    checkWitch = 0;
                }
            }
        });
        checkBoxBluetooth.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    checkBoxFirmware.setChecked(false);
                    checkWitch = 2;
                } else {
                    checkWitch = 0;
                }
            }
        });
    }

    @OnClick({R.id.img_back, R.id.btn_toUpgrade})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.btn_toUpgrade:
                if (checkWitch == 0) {
                    mToast(getString(R.string.please_choose_firmware));
                    return;
                }else if (checkWitch == 1){
                    showPopupAddCosigner1("hardware");
                }
                else if (checkWitch == 2){
                    showPopupAddCosigner1("ble");
                }
                break;
        }
    }

    private void showPopupAddCosigner1(String type) {
        dialogFragment = new CustomerDialogFragment(TAG, null, type);
        dialogFragment.show(getSupportFragmentManager(), "");
    }

    private boolean isInitialized() throws Exception {
        boolean isInitialized = false;
        try {
            isInitialized = executorService.submit(() -> Daemon.commands.callAttr("is_initialized")).get().toBoolean();
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
            isNFC = true;
            if (executable) {
                Tag tags = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                PyObject nfc = Global.py.getModule("trezorlib.transport.nfc");
                PyObject nfcHandler = nfc.get("NFCHandle");
                nfcHandler.put("device", tags);
                executable = false;
            }
            if (ready) {
                CustomerDialogFragment.customerUI.put("pin", pin);
                getResult();
                ready = false;
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
                    pinCached = executorService.submit(() -> Daemon.commands.callAttr("get_pin_status")).get().toBoolean();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // todo: get xpub
                futureTask = new FutureTask<>(() -> Daemon.commands.callAttr("get_xpub_from_hw"));
                executorService.submit(futureTask);
                //new Thread(CustomerDialogFragment.futureTask).start();
                if (pinCached) {
                    try {
                        xpub = futureTask.get().toString();
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                        return;
                    }
                    Intent intent1 = new Intent(VersionUpgradeActivity.this, UpgradeBixinKEYActivity.class);
                    intent1.putExtra("way", "nfc");
                    if (checkWitch == 1) {
                        intent1.putExtra("tag", 1);
                    } else if (checkWitch == 2) {
                        intent1.putExtra("tag", 2);
                    }
                    startActivity(intent1);

                }

            } else {
                // todo: Initialized
                if (isActive) {
                    executorService.execute(() -> {
                        try {
                            Daemon.commands.callAttr("init");
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                } else {
                    Intent intent1 = new Intent(this, WalletUnActivatedActivity.class);
                    startActivityForResult(intent1, REQUEST_ACTIVE);
                }
            }
        }
    }

    private void getResult() {
        try {
            ReadingPubKeyDialogFragment dialog = dialogFragment.showReadingDialog();
            xpub = futureTask.get(40, TimeUnit.SECONDS).toString();
            dialog.dismiss();
            Intent intent = new Intent(VersionUpgradeActivity.this, UpgradeBixinKEYActivity.class);
            intent.putExtra("way", "nfc");
            if (checkWitch == 1) {
                intent.putExtra("tag", 1);
            } else if (checkWitch == 2) {
                intent.putExtra("tag", 2);
            }
            startActivity(intent);
        } catch (ExecutionException | TimeoutException | InterruptedException e) {
            if ("com.chaquo.python.PyException: BaseException: (7, 'PIN invalid')".equals(e.getMessage())) {
                mToast(getResources().getString(R.string.pin_wrong));
            } else {
                dialogFragment.showReadingFailedDialog();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CustomerDialogFragment.PIN_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                pin = data.getStringExtra("pin");
                int tag = data.getIntExtra("tag", 0);
                switch (tag) {
                    case CustomerDialogFragment.PIN_NEW_FIRST: // 激活
                        // ble 激活
                        if (CustomerDialogFragment.isActive) {
                            CustomerDialogFragment.customerUI.put("pin", pin);
                            CustomerDialogFragment.handler.sendEmptyMessage(CustomerDialogFragment.SHOW_PROCESSING);
                            CustomerDialogFragment.isActive = false;
                        } else if (isActive) {
                            // nfc 激活
                            CustomerDialogFragment.pin = pin;
                            CustomerDialogFragment.handler.sendEmptyMessage(CustomerDialogFragment.SHOW_PROCESSING);
                            isActive = false;
                        }
                        break;
                    case CustomerDialogFragment.PIN_CURRENT: // 创建
                        if (!isNFC) { // ble
                            CustomerDialogFragment.customerUI.put("pin", pin);
                            new Handler().postDelayed(this::getResult, (long) 0.2);
                        } else { // nfc
                            ready = true;
                        }
                        break;
                    default:
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
