package org.haobtc.wallet.activities;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.chaquo.python.Kwarg;
import com.chaquo.python.PyObject;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.utils.Daemon;
import org.haobtc.wallet.utils.Global;
import org.haobtc.wallet.utils.NfcUtils;

import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class TouchHardwareActivity extends BaseActivity {
    public final static String FROM = "org.haobtc.wallet.from";
    public static FutureTask<PyObject> futureTask;
    boolean pinCached = false;
    private String tag;
    private boolean executable = true;
    private boolean cached = false;
    private String pin = "";


    public int getLayoutId() {
        return R.layout.touch;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        tag = getIntent().getStringExtra(FROM);
    }

    @Override
    public void initData() {

        NfcUtils.nfc(this, true);
        // use in udp
        if (NfcUtils.mNfcAdapter == null || !NfcUtils.mNfcAdapter.isEnabled()) {
            boolean isInitialized = true;
            try {
                isInitialized = Daemon.commands.callAttr("is_initialized", new Kwarg("path", PyObject.fromJava("udp:192.168.1.110:21324"))).toBoolean();
            } catch (Exception e) {
                Toast.makeText(this, "the point device is useless", Toast.LENGTH_SHORT).show();
                finish();
                e.printStackTrace();
                return;
            }

            if (isInitialized || WalletUnActivatedActivity.TAG.equals(tag) || TransactionDetailsActivity.TAG.equals(tag)) {
                if (WalletUnActivatedActivity.TAG.equals(tag)) {
                    new Thread(() ->
                            Daemon.commands.callAttr("init")
                    ).start();
                    startNewPage(PinSettingActivity.class, tag);
                } else {
                    //  if (executable) {
                    try {
                        pinCached = Daemon.commands.callAttr("get_pin_status", new Kwarg("path", PyObject.fromJava("udp:192.168.1.110:21324"))).toBoolean();
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "communication error", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    if (TransactionDetailsActivity.TAG.equals(tag)) {
                        futureTask = new FutureTask<>(() -> Daemon.commands.callAttr("sign_tx", getIntent().getStringExtra("unsignedRowTx")));
                        new Thread(futureTask).start();
                    } else {
                        futureTask = new FutureTask<>(() -> Daemon.commands.callAttr("get_xpub_from_hw"));
                        new Thread(futureTask).start();
                    }
                    //   }
                    if (!pinCached && !cached) {
                        Intent intent1 = new Intent(this, PinSettingActivity.class);
                        intent1.putExtra(FROM, tag);
                        startActivityForResult(intent1, 2);
                    } /*else {
                        *//*if (TransactionDetailsActivity.TAG.equals(tag)) {*//*
                            Intent intent1 = new Intent(this, ConfirmOnHardware.class);
                            intent1.putExtra("raw",getIntent().getStringExtra("unsignedRowTx"));
                            startActivity(intent1);
                      //  }
                    }*/
                }

            } else {
                startNewPage(WalletUnActivatedActivity.class, "");
            }


        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (NfcUtils.mNfcAdapter != null && NfcUtils.mNfcAdapter.isEnabled()) {
            // enable nfc discovery for the app
            System.out.println("为本App启用NFC感应");
            NfcUtils.mNfcAdapter.enableForegroundDispatch(this, NfcUtils.mPendingIntent, NfcUtils.mIntentFilter, NfcUtils.mTechList);
        } else {
            // used in udp
            if ((pinCached || cached) && !TransactionDetailsActivity.TAG.equals(tag)) {

         /*   if (!pin.isEmpty()) {
                System.out.println("java----------set_pin");
                PyObject ui = Global.py.getModule("trezorlib.customer_ui");
                PyObject customerUI = ui.get("CustomerUI");
                customerUI.put("pin", pin);
            }*/
                String result;
                try {
                    result = futureTask.get().toString();
                } catch (ExecutionException | InterruptedException e) {
                    Toast.makeText(this, R.string.pin_input_again, Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                    finish();
                    return;
                }
            /*if (TransactionDetailsActivity.TAG.equals(tag)) {
                Intent intent1 = new Intent();
                intent1.putExtra("rawTx", result);
                setResult(RESULT_OK, intent1);
                finish();
            } else {*/
                Intent intent1 = new Intent();
                intent1.putExtra("xpub", result);
                setResult(RESULT_OK, intent1);
                finish();
                //  }
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (NfcUtils.mNfcAdapter != null && NfcUtils.mNfcAdapter.isEnabled()) {
            // disable nfc discovery for the app
            NfcUtils.mNfcAdapter.disableForegroundDispatch(this);
            System.out.println("禁用本App的NFC感应");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        NfcUtils.mNfcAdapter = null;
    }

    private void startNewPage(Class<?> cls, String tags) {//TODO:
        Intent intent = new Intent(this, cls);
        intent.putExtra(FROM, tags);
        startActivity(intent);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String action = intent.getAction(); // get the action of the coming intent
        if (Objects.equals(action, NfcAdapter.ACTION_NDEF_DISCOVERED) // NDEF type
                || Objects.equals(action, NfcAdapter.ACTION_TECH_DISCOVERED)
                || Objects.requireNonNull(action).equals(NfcAdapter.ACTION_TAG_DISCOVERED)) {
            // get the i/o handle from the intent
            boolean isInitialized = true;
            boolean pinCached = false;
            if (executable) {
                Tag tags = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                PyObject nfc = Global.py.getModule("trezorlib.transport.nfc");
                PyObject nfcHandler = nfc.get("NFCHandle");
                nfcHandler.put("device", tags);
                System.out.println("java set tag===" + tags);
                try {
                    isInitialized = Daemon.commands.callAttr("is_initialized").toBoolean();
                } catch (Exception e) {
                    Toast.makeText(this, "communication error", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                    finish();
                    return;
                }
            }

            if (isInitialized || WalletUnActivatedActivity.TAG.equals(tag) || TransactionDetailsActivity.TAG.equals(tag)) {
                if (tag.equals(WalletUnActivatedActivity.TAG)) {
                    new Thread(() ->
                            Daemon.commands.callAttr("init")
                    ).start();
                } else {
                    if (executable) {
                        try {
                            pinCached = Daemon.commands.callAttr("get_pin_status").toBoolean();
                        } catch (Exception e) {
                            Toast.makeText(this, "communication error", Toast.LENGTH_SHORT).show();
                            e.printStackTrace();
                            finish();
                            return;
                        }
                        if (TransactionDetailsActivity.TAG.equals(tag)) {
                            System.out.println("java sign");
                            futureTask = new FutureTask<>(() -> Daemon.commands.callAttr("sign_tx", getIntent().getStringExtra("unsignedRowTx")));
                            new Thread(futureTask).start();
                        } else {
                            futureTask = new FutureTask<>(() -> Daemon.commands.callAttr("get_xpub_from_hw"));
                            new Thread(futureTask).start();
                        }
                    }
                    if (pinCached || cached) {
                        if (!pin.isEmpty()) {
                            PyObject ui = Global.py.getModule("trezorlib.customer_ui");
                            PyObject customerUI = ui.get("CustomerUI");
                            customerUI.put("pin", pin);
                        }
                        if (!TransactionDetailsActivity.TAG.equals(tag)) {
                            String result;
                            try {
                                result = futureTask.get().toString();
                            } catch (ExecutionException | InterruptedException e) {
                                Toast.makeText(this, R.string.pin_input_again, Toast.LENGTH_SHORT).show();
                                finish();
                                return;
                            }
                            Intent intent1 = new Intent();
                            intent1.putExtra("xpub", result);
                            setResult(RESULT_OK, intent1);
                            finish();
                        }
                        return;
                    }

                }
                Intent intent1 = new Intent(this, PinSettingActivity.class);
                intent1.putExtra(FROM, tag);
                startActivityForResult(intent1, 2);

            } else {
                startNewPage(WalletUnActivatedActivity.class, "");
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {

            pin = data.getStringExtra("pin");
            executable = false;
            cached = true;
            if (TransactionDetailsActivity.TAG.equals(data.getStringExtra(TouchHardwareActivity.FROM))) {
                Intent intent1 = new Intent(this, ConfirmOnHardware.class);
                intent1.putExtra("raw",getIntent().getStringExtra("unsignedRowTx"));
                startActivity(intent1);
            }
        }
    }

    @OnClick(R.id.img_back)
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
        }
    }
}
