package org.haobtc.wallet.activities.service;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.usb.UsbDevice;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import com.chaquo.python.PyObject;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.tbruyelle.rxpermissions2.RxPermissions;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.PinSettingActivity;
import org.haobtc.wallet.activities.SendOne2ManyMainPageActivity;
import org.haobtc.wallet.activities.SendOne2OneMainPageActivity;
import org.haobtc.wallet.activities.SetNameActivity;
import org.haobtc.wallet.activities.SettingActivity;
import org.haobtc.wallet.activities.TransactionDetailsActivity;
import org.haobtc.wallet.activities.VerificationKEYActivity;
import org.haobtc.wallet.activities.WalletUnActivatedActivity;
import org.haobtc.wallet.activities.jointwallet.MultiSigWalletCreator;
import org.haobtc.wallet.activities.personalwallet.ChooseHistryWalletActivity;
import org.haobtc.wallet.activities.personalwallet.ImportHistoryWalletActivity;
import org.haobtc.wallet.activities.personalwallet.PersonalMultiSigWalletCreator;
import org.haobtc.wallet.activities.personalwallet.SingleSigWalletCreator;
import org.haobtc.wallet.activities.personalwallet.hidewallet.HideWalletActivity;
import org.haobtc.wallet.activities.personalwallet.hidewallet.HideWalletSetPassActivity;
import org.haobtc.wallet.activities.settings.HardwareDetailsActivity;
import org.haobtc.wallet.activities.settings.UpgradeBixinKEYActivity;
import org.haobtc.wallet.activities.settings.VersionUpgradeActivity;
import org.haobtc.wallet.activities.settings.recovery_set.BackupRecoveryActivity;
import org.haobtc.wallet.activities.settings.recovery_set.RecoveryActivity;
import org.haobtc.wallet.activities.settings.recovery_set.RecoverySetActivity;
import org.haobtc.wallet.activities.sign.SignActivity;
import org.haobtc.wallet.asynctask.BusinessAsyncTask;
import org.haobtc.wallet.bean.HardwareFeatures;
import org.haobtc.wallet.dfu.service.DfuService;
import org.haobtc.wallet.event.ButtonRequestEvent;
import org.haobtc.wallet.event.ChangePinEvent;
import org.haobtc.wallet.event.ConnectingEvent;
import org.haobtc.wallet.event.DfuEvent;
import org.haobtc.wallet.event.ExecuteEvent;
import org.haobtc.wallet.event.HandlerEvent;
import org.haobtc.wallet.event.InitEvent;
import org.haobtc.wallet.event.PinEvent;
import org.haobtc.wallet.event.ReadingEvent;
import org.haobtc.wallet.event.ResultEvent;
import org.haobtc.wallet.event.SendSignBroadcastEvent;
import org.haobtc.wallet.event.SendingFailedEvent;
import org.haobtc.wallet.event.SignMessageEvent;
import org.haobtc.wallet.event.SignResultEvent;
import org.haobtc.wallet.event.WipeEvent;
import org.haobtc.wallet.fragment.BleDeviceRecyclerViewAdapter;
import org.haobtc.wallet.fragment.BluetoothConnectingFragment;
import org.haobtc.wallet.fragment.BluetoothFragment;
import org.haobtc.wallet.fragment.ErrorDialogFragment;
import org.haobtc.wallet.fragment.ReadingOrSendingDialogFragment;
import org.haobtc.wallet.utils.CustomerUsbManager;
import org.haobtc.wallet.utils.Daemon;
import org.haobtc.wallet.utils.Global;
import org.haobtc.wallet.utils.NfcUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import cn.com.heaton.blelibrary.ble.Ble;
import cn.com.heaton.blelibrary.ble.callback.BleScanCallback;
import cn.com.heaton.blelibrary.ble.model.BleDevice;
import no.nordicsemi.android.dfu.DfuServiceInitiator;

import static org.haobtc.wallet.activities.sign.SignActivity.strinputAddress;


public class CommunicationModeSelector extends AppCompatActivity implements View.OnClickListener, BusinessAsyncTask.Helper {

    public static final String TAG = "BLE";
    public static final int PIN_REQUEST = 5;
    public static final int SHOW_PROCESSING = 7;
    public static final int BUTTON_REQUEST = 9;
    public static final int PIN_CURRENT = 1;
    public static final int PIN_NEW_FIRST = 2;
    public static final int PASS_NEW_PASSPHRASS = 6;
    public static final int PASS_PASSPHRASS = 3;
    private static final int REQUEST_ENABLE_BT = 1;
    public static final int PASSPHRASS_INPUT = 8;
    public static PyObject ble, customerUI, nfc, usb, bleHandler, nfcHandler, bleTransport, nfcTransport, usbTransport;
    public static MyHandler handler;
    public static FutureTask<PyObject> futureTask;
    public static ExecutorService executorService = Executors.newCachedThreadPool();
    public static String xpub;
    private boolean isActive = false;
    private boolean isSign = false;
    public static volatile boolean isNFC;
    private BleDeviceRecyclerViewAdapter adapter;
    private RelativeLayout relativeLayout;
    private String tag;
    private String extras;
    public static List<Runnable> runnables = new ArrayList<>();
    private BluetoothConnectingFragment fragment;
    private Ble<BleDevice> mBle;
    private ReadingOrSendingDialogFragment dialogFragment;
    private BluetoothFragment bleFragment;
    public static volatile boolean isDfu;
    private RxPermissions permissions;
    private static final String COMMUNICATION_MODE_BLE = "bluetooth";
    public static final String COMMUNICATION_MODE_NFC = "nfc";


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth_nfc);
        ImageView imageViewCancel;
        SharedPreferences preferences = getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        boolean bluetoothStatus = preferences.getBoolean("bluetoothStatus", false);
        ImageView imageView = findViewById(R.id.touch_nfc);
        TextView textView = findViewById(R.id.text_prompt);
        FrameLayout frameLayout = findViewById(R.id.ble_device);
        RadioGroup radioGroup = findViewById(R.id.radio_group);
        RadioButton radioBle = findViewById(R.id.radio_ble);
        imageViewCancel = findViewById(R.id.img_cancel);
        TextView textViewInputByHand = findViewById(R.id.text_input_publickey_by_hand);
        relativeLayout = findViewById(R.id.input_layout);
        textViewInputByHand.setOnClickListener(this);
        imageViewCancel.setOnClickListener(this);
        tag = getIntent().getStringExtra("tag");
        if (!bluetoothStatus || SetNameActivity.TAG.equals(tag) || HideWalletSetPassActivity.TAG.equals(tag)) {
            radioBle.setVisibility(View.GONE);
        }
        mBle = Ble.getInstance();
        adapter = new BleDeviceRecyclerViewAdapter(this);
        bleFragment = new BluetoothFragment(adapter);
        nfc = Global.py.getModule("trezorlib.transport.nfc");
        ble = Global.py.getModule("trezorlib.transport.bluetooth");
        usb = Global.py.getModule("trezorlib.transport.android_usb");
        bleHandler = ble.get("BlueToothHandler");
        nfcHandler = nfc.get("NFCHandle");
        usbTransport = usb.get("AndroidUsbTransport");
        nfcTransport = nfc.get("NFCTransport");
        bleTransport = ble.get("BlueToothTransport");
        customerUI = Global.py.getModule("trezorlib.customer_ui").get("CustomerUI");
        handler = MyHandler.getInstance(this);
        customerUI.put("handler", handler);
        NfcUtils.nfc(this, true);
        EventBus.getDefault().register(this);
        extras = getIntent().getStringExtra("extras");
        if (!MultiSigWalletCreator.TAG.equals(tag)) {
            relativeLayout.setVisibility(View.GONE);
        }
        // usb init
        CustomerUsbManager usbManager = CustomerUsbManager.getInstance(this);
        usbManager.register(this);
        UsbDevice device = usbManager.findBixinKEYDevice();
        if (device != null) {
            usbManager.doBusiness(device);
        }
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.radio_ble:
                    frameLayout.setVisibility(View.VISIBLE);
                    imageView.setVisibility(View.GONE);
                    textView.setVisibility(View.GONE);
                    getSupportFragmentManager().beginTransaction().replace(R.id.ble_device, bleFragment).commit();
                    permissions = new RxPermissions(this);
                    permissions.request(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION).subscribe(
                            granted -> {
                                if (granted) {
                                    group.check(R.id.radio_ble);
                                    turnOnBlueTooth();
                                    refreshDeviceList(true);
                                } else {
                                    Toast.makeText(this, getString(R.string.blurtooth_need_permission), Toast.LENGTH_LONG).show();
                                }
                            }
                    ).dispose();
                    break;
                case R.id.radio_nfc:
                    group.check(R.id.radio_nfc);
                    mBle.getConnetedDevices().forEach(bleDevice -> mBle.disconnect(bleDevice));
                    refreshDeviceList(false);
                    imageView.setVisibility(View.VISIBLE);
                    textView.setVisibility(View.VISIBLE);
                    frameLayout.setVisibility(View.GONE);
            }
        });
        Window window = getWindow();
        if (window != null) {
            WindowManager.LayoutParams wlp = window.getAttributes();
            wlp.gravity = Gravity.BOTTOM;
            wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
            wlp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(wlp);
        }
    }


    private final BleScanCallback<BleDevice> scanCallback = new BleScanCallback<BleDevice>() {
        @Override
        public void onLeScan(final BleDevice device, int rssi, byte[] scanRecord) {
            synchronized (mBle.getLocker()) {
                Log.d(TAG, "BLE Device Find====" + device.getBleName());
                adapter.add(device);
            }
        }
    };

    private void turnOnBlueTooth() {
        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBle.isBleEnable()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    private void refreshDeviceList(boolean start) {
        if (mBle.isScanning()) {
            mBle.stopScan();
        }
        BleDeviceRecyclerViewAdapter.mValues.clear();
        adapter.notifyDataSetChanged();
        if (start) {
            mBle.startScan(scanCallback);
        }
    }


    private final Runnable retry = new Runnable() {
        @Override
        public void run() {
            if (isNFC) {
                return;
            }
            startService(new Intent(CommunicationModeSelector.this, BleService.class));
        }
    };

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String action = intent.getAction(); // get the action of the coming intent
        if (Objects.equals(action, NfcAdapter.ACTION_NDEF_DISCOVERED) // NDEF type
                || Objects.equals(action, NfcAdapter.ACTION_TECH_DISCOVERED)
                || Objects.requireNonNull(action).equals(NfcAdapter.ACTION_TAG_DISCOVERED)) {
            isNFC = true;
            Tag tags = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            nfcTransport.put("ENABLED", true);
            bleHandler.put("ENABLED", false);
            usbTransport.put("ENABLED", false);
            nfcHandler.put("device", tags);
            handlerEverything(true);
        }

    }

    @NonNull
    private HardwareFeatures getFeatures(boolean isNFC) throws Exception {
        String feature;
        try {
            futureTask = new FutureTask<>(() -> Daemon.commands.callAttr("get_feature", isNFC ? COMMUNICATION_MODE_NFC : COMMUNICATION_MODE_BLE));
            executorService.submit(futureTask);
            feature = futureTask.get(5, TimeUnit.SECONDS).toString();
            HardwareFeatures features = new Gson().fromJson(feature, HardwareFeatures.class);
            if (features.isBootloaderMode()) {
                throw new Exception("bootloader mode");
            }
            if (features.isInitialized()) {
                SharedPreferences devices = getSharedPreferences("devices", Context.MODE_PRIVATE);
                if (!devices.contains(features.getDeviceId())) {
                    devices.edit().putString(features.getDeviceId(), feature).apply();
                }
            }
            return features;
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            Toast.makeText(this, getString(R.string.no_message), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            throw e;
        }
    }

    private void handlerEverything(boolean isNFC) {
        isSign = false;
        isActive = false;
        if (VersionUpgradeActivity.TAG.equals(tag)) {
            if ("hardware".equals(extras)) {
                Intent intent = new Intent(CommunicationModeSelector.this, UpgradeBixinKEYActivity.class);
                intent.putExtra("way", isNFC ? COMMUNICATION_MODE_NFC : COMMUNICATION_MODE_BLE);
                intent.putExtra("tag", 1);
                startActivity(intent);
                if (isNFC) {
                    new Handler().postDelayed(() -> EventBus.getDefault().postSticky(new ExecuteEvent()), 1000);
                }
            } else if ("ble".equals(extras)) {
                if (isNFC) {
                    Intent intent = new Intent(CommunicationModeSelector.this, UpgradeBixinKEYActivity.class);
                    intent.putExtra("way", COMMUNICATION_MODE_NFC);
                    intent.putExtra("tag", 2);
                    startActivity(intent);
                    new Handler().postDelayed(() -> EventBus.getDefault().postSticky(new ExecuteEvent()), 2000);
                } else {
                    dfu();
                }
            }
        } else if (HardwareDetailsActivity.TAG.equals(tag) || SettingActivity.TAG_CHANGE_PIN.equals(tag)) {
            dealWithChangePin(isNFC);
        } else if (RecoverySetActivity.TAG.equals(tag)) {
            dealWithWipeDevice(isNFC);
        } else if (SettingActivity.TAG.equals(tag)) {
            String strRandom = UUID.randomUUID().toString().replaceAll("-", "");
            new BusinessAsyncTask().setHelper(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, BusinessAsyncTask.COUNTER_VERIFICATION, strRandom, isNFC ? COMMUNICATION_MODE_NFC : COMMUNICATION_MODE_BLE);
            startActivity(new Intent(this, VerificationKEYActivity.class));
        } else if (SetNameActivity.TAG.equals(tag)) {
            Intent intent = new Intent(this, PinSettingActivity.class);
            intent.putExtra("tag", tag);
            intent.putExtra("pin_type", 2);
            startActivity(intent);
            doInit(new InitEvent(getIntent().getStringExtra("name")));
            finish();
        } else if (HideWalletSetPassActivity.TAG.equals(tag)) {
            String passphrase = getIntent().getStringExtra("passphrase");
            setPassphrass(new PinEvent("", passphrase));
            finish();
        } else {
            dealWithBusiness(isNFC);
        }
    }

    private void dealWithChangePin(boolean isNFC) {
        HardwareFeatures features;
        try {
            features = getFeatures(isNFC);
        } catch (Exception e) {
            if ("bootloader mode".equals(e.getMessage())) {
                Toast.makeText(this, R.string.bootloader_mode, Toast.LENGTH_LONG).show();
                finish();
            }
            return;
        }
        if (features.isInitialized()) {
            Intent intent = new Intent(this, PinSettingActivity.class);
            if (SettingActivity.TAG_CHANGE_PIN.equals(tag)) {
                intent.putExtra("tag", SettingActivity.TAG_CHANGE_PIN);
            } else if (HardwareDetailsActivity.TAG.equals(tag)) {
                intent.putExtra("tag", HardwareDetailsActivity.TAG);
            }
            intent.putExtra("pin_type", 3);
            startActivity(intent);
            new BusinessAsyncTask().setHelper(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, BusinessAsyncTask.CHANGE_PIN, isNFC ? COMMUNICATION_MODE_NFC : COMMUNICATION_MODE_BLE);
        } else {
            Toast.makeText(this, R.string.wallet_un_activated_pin, Toast.LENGTH_LONG).show();
            finish();
        }


    }

    private void dealWithWipeDevice(boolean isNFC) {
        HardwareFeatures features;
        try {
            features = getFeatures(isNFC);
        } catch (Exception e) {
            if ("bootloader mode".equals(e.getMessage())) {
                Toast.makeText(this, R.string.bootloader_mode, Toast.LENGTH_LONG).show();
                finish();
            }
            return;
        }
        if (features.isInitialized()) {
            new BusinessAsyncTask().setHelper(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, BusinessAsyncTask.WIPE_DEVICE, isNFC ? COMMUNICATION_MODE_NFC : COMMUNICATION_MODE_BLE);
            // todo: 跳转PIN码页面，验证PIN
            Intent intent = new Intent(this, PinSettingActivity.class);
            intent.putExtra("tag", tag);
            intent.putExtra("pin_type", 1);
            startActivity(intent);
        } else {
            Toast.makeText(this, R.string.wallet_un_activated, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void dealWithBusiness(boolean isNFC) {
        HardwareFeatures features;
        try {
            features = getFeatures(isNFC);
        } catch (Exception e) {
            if ("bootloader mode".equals(e.getMessage())) {
                Toast.makeText(this, R.string.bootloader_mode, Toast.LENGTH_LONG).show();
                finish();
            }
            return;
        }
        boolean isInit = features.isInitialized();
        if (isInit) {
            if (MultiSigWalletCreator.TAG.equals(tag) || SingleSigWalletCreator.TAG.equals(tag) || PersonalMultiSigWalletCreator.TAG.equals(tag) || HideWalletActivity.TAG.equals(tag) || ImportHistoryWalletActivity.TAG.equals(tag)) {
                // todo: remove the below pin about code
                if (!features.isPinCached() && features.isPinProtection()) {
                    Intent intent = new Intent(this, PinSettingActivity.class);
                    intent.putExtra("tag", tag);
                    startActivity(intent);
                }
                if (SingleSigWalletCreator.TAG.equals(tag) || HideWalletActivity.TAG.equals(tag)) {
                    if (HideWalletActivity.TAG.equals(tag)) {
                        customerUI.callAttr("set_pass_state", 1);
                    }
                    new BusinessAsyncTask().setHelper(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, BusinessAsyncTask.GET_EXTEND_PUBLIC_KEY_SINGLE, isNFC ? COMMUNICATION_MODE_NFC : COMMUNICATION_MODE_BLE, "p2wpkh");
                } else {
                    new BusinessAsyncTask().setHelper(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, BusinessAsyncTask.GET_EXTEND_PUBLIC_KEY, isNFC ? COMMUNICATION_MODE_NFC : COMMUNICATION_MODE_BLE);
                }
            } else if (TransactionDetailsActivity.TAG.equals(tag) || SignActivity.TAG.equals(tag) || SignActivity.TAG1.equals(tag) || SignActivity.TAG2.equals(tag) || SignActivity.TAG3.equals(tag) || SendOne2OneMainPageActivity.TAG.equals(tag) || SendOne2ManyMainPageActivity.TAG.equals(tag)) {
                isSign = true;
                if (SignActivity.TAG1.equals(tag) || SignActivity.TAG3.equals(tag)) {
                    if (SignActivity.TAG3.equals(tag)) {
                        //hide wallet sign message -->set_pass_state
                        customerUI.callAttr("set_pass_state", 1);
                    }
                    new BusinessAsyncTask().setHelper(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, BusinessAsyncTask.SIGN_MESSAGE, strinputAddress, extras);
                } else {
                    if (SignActivity.TAG2.equals(tag)) {
                        //hide wallet sign transaction -->set_pass_state
                        customerUI.callAttr("set_pass_state", 1);
                    } else {
                        // todo: 收到交易传输完成的信号，才能跳转，👇代码要删除

                      //  if (features.isPinCached() || !features.isPinProtection())
                            runOnUiThread(runnables.get(0));
                      //  }
                    }
                    new BusinessAsyncTask().setHelper(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, BusinessAsyncTask.SIGN_TX, extras);
                }
                // todo: remove the if , pin always required
                if (!features.isPinCached() && features.isPinProtection()) {
                    Intent intent = new Intent(this, PinSettingActivity.class);
                    intent.putExtra("tag", "signature");
                    startActivity(intent);
                }
            } else if (BackupRecoveryActivity.TAG.equals(tag) || RecoveryActivity.TAG.equals(tag)) {
                if (!TextUtils.isEmpty(extras)) {
                    new BusinessAsyncTask().setHelper(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, BusinessAsyncTask.RECOVER, isNFC ? COMMUNICATION_MODE_NFC : COMMUNICATION_MODE_BLE, extras);
                } else {
                    Log.i(TAG, "java ==== backup");
                    new BusinessAsyncTask().setHelper(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, BusinessAsyncTask.BACK_UP, isNFC ? COMMUNICATION_MODE_NFC : COMMUNICATION_MODE_BLE);
                }
            } else if (SettingActivity.TAG.equals(tag)) {
                String strRandom = UUID.randomUUID().toString().replaceAll("-", "");
                //Anti counterfeiting verification
                new BusinessAsyncTask().setHelper(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, BusinessAsyncTask.COUNTER_VERIFICATION, strRandom, isNFC ? COMMUNICATION_MODE_NFC : COMMUNICATION_MODE_BLE);
            }
        } else {
            isActive = true;
            Intent intent = new Intent(this, WalletUnActivatedActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            startActivity(intent);
        }
    }

    private void dfu() {
        BleDevice device = BleDeviceRecyclerViewAdapter.mBleDevice;
        Ble.getInstance().disconnect(device);
        new Handler().postDelayed(() -> {
            final DfuServiceInitiator starter = new DfuServiceInitiator(device.getBleAddress());
            starter.setDeviceName(device.getBleName());
            starter.setKeepBond(true);
        /*
           Call this method to put Nordic nrf52832 into bootloader mode
        */
            starter.setUnsafeExperimentalButtonlessServiceInSecureDfuEnabled(true);
            if (TextUtils.isEmpty(VersionUpgradeActivity.filePath)) {
                File file = new File(String.format("%s/bixin.zip", getExternalCacheDir().getPath()));
                if (!file.exists()) {
                    Toast.makeText(this, R.string.update_file_not_exist, Toast.LENGTH_LONG).show();

                    EventBus.getDefault().post(new ExecuteEvent());
                    finish();
                    return;
                }
            } else if (!VersionUpgradeActivity.filePath.endsWith(".zip")) {
                Toast.makeText(this, R.string.update_file_format_error, Toast.LENGTH_LONG).show();
                EventBus.getDefault().post(new ExecuteEvent());
                finish();
                return;
            }
            starter.setZip(null, TextUtils.isEmpty(VersionUpgradeActivity.filePath) ? String.format("%s/bixin.zip", getExternalCacheDir().getPath()) : VersionUpgradeActivity.filePath);
            DfuServiceInitiator.createDfuNotificationChannel(this);
            starter.start(this, DfuService.class);
            isDfu = true;
        }, 2000);

    }

    private void showConnecting() {
        runOnUiThread(() -> {
            if (fragment == null) {
                fragment = new BluetoothConnectingFragment();
            }
            if (hasWindowFocus() && !isFinishing()) {
                getSupportFragmentManager().beginTransaction().replace(R.id.ble_device, fragment, "connecting").commitNow();
                relativeLayout.setVisibility(View.GONE);
            }
        });
    }

    public ReadingOrSendingDialogFragment showReadingDialog(int res) {
        getSupportFragmentManager().beginTransaction().replace(R.id.ble_device, bleFragment).commit();
        ReadingOrSendingDialogFragment fragment = new ReadingOrSendingDialogFragment(res);
        fragment.show(getSupportFragmentManager(), "");
        return fragment;
    }

    public void showErrorDialog(int error, int title) {
        ErrorDialogFragment fragment = new ErrorDialogFragment(error, title);
        fragment.setRunnable(retry);
        fragment.setActivity(this);
        fragment.show(getSupportFragmentManager(), "");
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doInit(InitEvent event) {
        new BusinessAsyncTask().setHelper(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, BusinessAsyncTask.INIT_DEVICE, isNFC ? COMMUNICATION_MODE_NFC : COMMUNICATION_MODE_BLE, event.getName());
    }
    @Subscribe(threadMode = ThreadMode.POSTING)
    public void doDfu(DfuEvent dfuEvent) {
        if (dfuEvent.getType() == DfuEvent.START_DFU) {
            dfu();
            EventBus.getDefault().post(new DfuEvent(1));
        }
    }
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doWipe(WipeEvent event) {
        new BusinessAsyncTask().setHelper(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, BusinessAsyncTask.WIPE_DEVICE, isNFC ? COMMUNICATION_MODE_NFC : COMMUNICATION_MODE_BLE);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doBusiness(HandlerEvent event) {
        handlerEverything(false);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void connecting(ConnectingEvent event) {
        showConnecting();
    }

    @Subscribe
    public void setPin(PinEvent event) {
        if (!Strings.isNullOrEmpty(event.getPinCode())) {
            customerUI.put("pin", event.getPinCode());
        }
    }
    @Subscribe
    public void changePin(ChangePinEvent event) {
        if (!Strings.isNullOrEmpty(event.toString())) {
            customerUI.put("pin", event.toString());
        }
    }
    @Subscribe
    public void setPassphrass(PinEvent event) {
        if (!Strings.isNullOrEmpty(event.getPassphrass())) {
            customerUI.put("passphrase", event.getPassphrass());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void showReading(ReadingEvent event) {
        dialogFragment = showReadingDialog(R.string.reading_dot);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_cancel:
                refreshDeviceList(false);
                finish();
                break;
            case R.id.text_input_publickey_by_hand:
                refreshDeviceList(false);
                runOnUiThread(runnables.get(0));
                finish();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(this, getString(R.string.open_bluetooth), Toast.LENGTH_LONG).show();
        } else if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK) {
            refreshDeviceList(true);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (NfcUtils.mNfcAdapter != null && NfcUtils.mNfcAdapter.isEnabled()) {
            // enable nfc discovery for the app
            Log.i("NFC", "为本App启用NFC感应");
            NfcUtils.mNfcAdapter.enableForegroundDispatch(this, NfcUtils.mPendingIntent, NfcUtils.mIntentFilter, NfcUtils.mTechList);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (dialogFragment != null) {
            dialogFragment.dismiss();
        }
        if (NfcUtils.mNfcAdapter != null && NfcUtils.mNfcAdapter.isEnabled()) {
            // disable nfc discovery for the app
            NfcUtils.mNfcAdapter.disableForegroundDispatch(this);
            Log.i("NFC", "禁用本App的NFC感应");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MyHandler.myHandler = null;
        EventBus.getDefault().unregister(this);
        CustomerUsbManager.getInstance(this).unRegister(this);
    }

    @Override
    public void onPreExecute() {
        if (isActive) {
            return;
        }
        if (isSign) {
            dialogFragment = showReadingDialog(R.string.transaction_loading);
        } else if (MultiSigWalletCreator.TAG.equals(tag) || SingleSigWalletCreator.TAG.equals(tag) || PersonalMultiSigWalletCreator.TAG.equals(tag) || HideWalletActivity.TAG.equals(tag) || ImportHistoryWalletActivity.TAG.equals(tag)) {
            // 获取公钥之前需完成的工作
            dialogFragment = showReadingDialog(R.string.reading_dot);
        }
    }

    @Override
    public void onException(Exception e) {
        dialogFragment.dismiss();
        EventBus.getDefault().post(new SendingFailedEvent(e));
        Log.i("TAG-ErrorMsgDialog", "onException: " + e.getMessage());
        if ("BaseException: (7, 'PIN invalid')".equals(e.getMessage())) {
            showErrorDialog(0, R.string.pin_wrong);
        } else if ("DeviceUnpairableError: BiXin cannot pair with your Trezor.".equals(e.getMessage())) {
            showErrorDialog(R.string.try_another_key, R.string.unpair);
        } else if ("BaseException: failed to recognize transaction encoding for txt: craft fury pig target diagram ...".equals(e.getMessage())) {
            showErrorDialog(R.string.sign_failed, R.string.transaction_parse_error);
        } else {
            showErrorDialog(R.string.key_wrong_prompte, R.string.read_pk_failed);
        }
    }

    @Override
    public void onResult(String s) {
        if (dialogFragment != null) {
            dialogFragment.dismiss();
        }
        if (isActive) {
            EventBus.getDefault().post(new ResultEvent(s));
            finish();
            return;
        }
        if (MultiSigWalletCreator.TAG.equals(tag) || SingleSigWalletCreator.TAG.equals(tag) || PersonalMultiSigWalletCreator.TAG.equals(tag) || HideWalletActivity.TAG.equals(tag) || ImportHistoryWalletActivity.TAG.equals(tag)) {
            // todo: 获取公钥
            xpub = s;
            if (ImportHistoryWalletActivity.TAG.equals(tag)) {
                Intent intent1 = new Intent(this, ChooseHistryWalletActivity.class);
                intent1.putExtra("histry_xpub", xpub);
                startActivity(intent1);
            } else {
                runOnUiThread(runnables.get(1));
            }
        } else if (isSign) {
            // todo : 获取签名后的动作, 传输交易结果，签名结果需要重新读取
            if (SignActivity.TAG1.equals(tag) || SignActivity.TAG3.equals(tag)) {
                EventBus.getDefault().post(new SignMessageEvent(s));
            } else if (SendOne2OneMainPageActivity.TAG.equals(tag) || SendOne2ManyMainPageActivity.TAG.equals(tag)) {
                EventBus.getDefault().postSticky(new SendSignBroadcastEvent(s));
            } else {
                EventBus.getDefault().post(new SignResultEvent(s));
            }
            // todo: 收到传输完成的结果再跳转
           // runOnUiThread(runnables.get(0));
        } else if (BackupRecoveryActivity.TAG.equals(tag)) {
            if (TextUtils.isEmpty(extras)) {
                // TODO: 获取加密后的私钥

            } else {
                // todo: 恢复结果
            }
        } else if (RecoverySetActivity.TAG.equals(tag) || HardwareDetailsActivity.TAG.equals(tag) || SettingActivity.TAG_CHANGE_PIN.equals(tag)) {
            EventBus.getDefault().post(new ResultEvent(s));
        } else if (SettingActivity.TAG.equals(tag)) {
            EventBus.getDefault().post(new ResultEvent(s));
        }
        finish();
    }

    @Override
    public void onCancelled() {
        runOnUiThread(() -> Toast.makeText(this, getString(R.string.task_cancle), Toast.LENGTH_SHORT).show());
    }

    public static class MyHandler extends Handler {
        private static volatile MyHandler myHandler;
        private FragmentActivity fragmentActivity;

        private MyHandler(FragmentActivity activity) {
            this.fragmentActivity = activity;
        }

        static MyHandler getInstance(FragmentActivity fragmentActivity) {
            if (myHandler == null) {
                synchronized (MyHandler.class) {
                    if (myHandler == null) {
                        myHandler = new MyHandler(fragmentActivity);
                    }
                }
            }
            return myHandler;
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                /*case PIN_CURRENT:
                    Intent intent = new Intent(fragmentActivity, PinSettingActivity.class);
                    intent.putExtra("pin_type", PIN_CURRENT);
                    fragmentActivity.startActivityForResult(intent, PIN_REQUEST);
                    break;
                case PIN_NEW_FIRST:
                    Intent intent1 = new Intent(fragmentActivity, PinSettingActivity.class);
                    intent1.putExtra("pin_type", PIN_NEW_FIRST);
                    fragmentActivity.startActivityForResult(intent1, PIN_REQUEST);
                    break;
                case SHOW_PROCESSING:
                    EventBus.getDefault().post(new FirstEvent("33"));
                    Intent intent2 = new Intent(fragmentActivity, ActivatedProcessing.class);
                    fragmentActivity.startActivity(intent2);
                    break;*/
                case BUTTON_REQUEST:
                    EventBus.getDefault().post(new ButtonRequestEvent());
                    break;
                case PASS_NEW_PASSPHRASS:
                case PASS_PASSPHRASS:
                    //Set password
                    Intent intent3 = new Intent(fragmentActivity, HideWalletSetPassActivity.class);
                    fragmentActivity.startActivity(intent3);
                    break;
            }
        }
    }
}
