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
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import com.chaquo.python.Kwarg;
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
import org.haobtc.wallet.activities.personalwallet.hidewallet.HideWalletSetPassActivity;
import org.haobtc.wallet.activities.settings.BixinKeyBluetoothSettingActivity;
import org.haobtc.wallet.activities.settings.CheckXpubResultActivity;
import org.haobtc.wallet.activities.settings.HardwareDetailsActivity;
import org.haobtc.wallet.activities.settings.UpgradeBixinKEYActivity;
import org.haobtc.wallet.activities.settings.VersionUpgradeActivity;
import org.haobtc.wallet.activities.settings.recovery_set.BackupMessageActivity;
import org.haobtc.wallet.activities.settings.recovery_set.BackupRecoveryActivity;
import org.haobtc.wallet.activities.settings.recovery_set.RecoveryActivity;
import org.haobtc.wallet.activities.settings.recovery_set.RecoveryResult;
import org.haobtc.wallet.activities.settings.recovery_set.RecoverySetActivity;
import org.haobtc.wallet.activities.sign.SignActivity;
import org.haobtc.wallet.asynctask.BusinessAsyncTask;
import org.haobtc.wallet.bean.HardwareFeatures;
import org.haobtc.wallet.event.ButtonRequestEvent;
import org.haobtc.wallet.event.ChangePinEvent;
import org.haobtc.wallet.event.CheckHideWalletEvent;
import org.haobtc.wallet.event.ConnectingEvent;
import org.haobtc.wallet.event.ExecuteEvent;
import org.haobtc.wallet.event.ExistEvent;
import org.haobtc.wallet.event.HandlerEvent;
import org.haobtc.wallet.event.InitEvent;
import org.haobtc.wallet.event.PinEvent;
import org.haobtc.wallet.event.ReadingEvent;
import org.haobtc.wallet.event.ReceiveXpub;
import org.haobtc.wallet.event.ResultEvent;
import org.haobtc.wallet.event.SendSignBroadcastEvent;
import org.haobtc.wallet.event.SendXpubToSigwallet;
import org.haobtc.wallet.event.SetBluetoothEvent;
import org.haobtc.wallet.event.SignMessageEvent;
import org.haobtc.wallet.event.SignResultEvent;
import org.haobtc.wallet.event.WipeEvent;
import org.haobtc.wallet.fragment.BleDeviceRecyclerViewAdapter;
import org.haobtc.wallet.fragment.BluetoothConnectingFragment;
import org.haobtc.wallet.fragment.BluetoothFragment;
import org.haobtc.wallet.fragment.ErrorDialogFragment;
import org.haobtc.wallet.fragment.ReadingOrSendingDialogFragment;
import org.haobtc.wallet.fragment.mainwheel.CheckHideWalletFragment;
import org.haobtc.wallet.utils.CustomerUsbManager;
import org.haobtc.wallet.utils.Daemon;
import org.haobtc.wallet.utils.Global;
import org.haobtc.wallet.utils.NfcUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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

import static org.haobtc.wallet.activities.sign.SignActivity.strinputAddress;


public class CommunicationModeSelector extends AppCompatActivity implements View.OnClickListener, BusinessAsyncTask.Helper {

    public static final String TAG = "BLE";
    public static final int BUTTON_REQUEST = 9;
    public static final int PIN_CURRENT = 1;
    public static final int PIN_NEW_FIRST = 2;
    public static final int PASS_NEW_PASSPHRASS = 6;
    public static final int PASS_PASSPHRASS = 3;
    private static final int REQUEST_ENABLE_BT = 1;
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
    public static final String COMMUNICATION_MODE_BLE = "bluetooth";
    public static final String COMMUNICATION_MODE_NFC = "nfc";
    private CustomerUsbManager usbManager;
    private UsbDevice device;
    private HardwareFeatures features;
    private boolean isChangePin;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth_nfc);
        ImageView imageViewCancel;
        EventBus.getDefault().post(new ExistEvent());
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
        EventBus.getDefault().register(this);
        extras = getIntent().getStringExtra("extras");
        if (!MultiSigWalletCreator.TAG.equals(tag)) {
            relativeLayout.setVisibility(View.GONE);
        }
        adapter = new BleDeviceRecyclerViewAdapter(this);
        bleFragment = new BluetoothFragment(adapter);
        // usb init
        usbManager = CustomerUsbManager.getInstance(this);
        usbManager.register(this);
        device = usbManager.findBixinKEYDevice();
        if (device != null) {
            radioGroup.setVisibility(View.GONE);
            setVisible(false);
            try {
                usbManager.doBusiness(device);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            mBle = Ble.getInstance();
            NfcUtils.nfc(this, true);
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
                        mBle.disconnectAll();
                        refreshDeviceList(false);
                        imageView.setVisibility(View.VISIBLE);
                        textView.setVisibility(View.VISIBLE);
                        frameLayout.setVisibility(View.GONE);
                }
            });
        }
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
        // 由于设备被连接时，会停止广播导致该设备无法被搜索到,所以要添加本APP以连接的设备到列表中
        BleDeviceRecyclerViewAdapter.mValues.addAll(Ble.getInstance().getConnetedDevices());
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
            if (Ble.getInstance().getConnetedDevices().size() != 0) {
                startService(new Intent(CommunicationModeSelector.this, BleService.class));
            } else {
                usbManager.doBusiness(device);
            }
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
            if (!futureTask.isDone()) {
                futureTask.cancel(true);
            }
            HardwareFeatures features = HardwareFeatures.objectFromData(feature);
            if (features.isInitialized()) {
                SharedPreferences devices = getSharedPreferences("devices", Context.MODE_PRIVATE);
                devices.edit().putString(features.getBleName(), feature).apply();
            }
            return features;
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.no_message), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            throw e;
        }
    }

    private void handlerEverything(boolean isNFC) {
        isSign = false;
        isActive = false;
        try {
            features = getFeatures(isNFC);
        } catch (Exception e) {
            finish();
            return;
        }
        if (VersionUpgradeActivity.TAG.equals(tag)) {
            // stm32 firmware update by nfc or ble
            if ("hardware".equals(extras)) {
                Intent intent = new Intent(CommunicationModeSelector.this, UpgradeBixinKEYActivity.class);
                intent.putExtra("way", isNFC ? COMMUNICATION_MODE_NFC : COMMUNICATION_MODE_BLE);
                intent.putExtras(getIntent().getExtras());
                intent.putExtra("tag", 1);
                startActivity(intent);
                if (isNFC) {
                    new Handler().postDelayed(() -> EventBus.getDefault().postSticky(new ExecuteEvent()), 1000);
                }
                // ble firmware update by nfc only
            } else if ("ble".equals(extras)) {
                if (isNFC) {
                    Intent intent = new Intent(CommunicationModeSelector.this, UpgradeBixinKEYActivity.class);
                    intent.putExtra("way", COMMUNICATION_MODE_NFC);
                    intent.putExtra("tag", 2);
                    intent.putExtras(getIntent().getExtras());
                    startActivity(intent);
                    new Handler().postDelayed(() -> EventBus.getDefault().postSticky(new ExecuteEvent()), 2000);
                }
            }
        } else if (HardwareDetailsActivity.TAG.equals(tag) || SettingActivity.TAG_CHANGE_PIN.equals(tag)) {
            dealWithChangePin(isNFC);
        } else if (RecoverySetActivity.TAG.equals(tag)) {
            dealWithWipeDevice(isNFC);
        } else if (SettingActivity.TAG.equals(tag)) {
            String strRandom = UUID.randomUUID().toString().replaceAll("-", "");
            new BusinessAsyncTask().setHelper(this).execute(BusinessAsyncTask.COUNTER_VERIFICATION, strRandom, isNFC ? COMMUNICATION_MODE_NFC : COMMUNICATION_MODE_BLE);
            startActivity(new Intent(this, VerificationKEYActivity.class));
//        } else if (ActivatedProcessing.TAG.equals(tag)) {
//            Intent intent = new Intent(this, PinSettingActivity.class);
//            intent.putExtra("tag", tag);
//            intent.putExtra("pin_type", 2);
//            startActivity(intent);
//            finish();
        } else if (HideWalletSetPassActivity.TAG.equals(tag)) {
            String passphrase = getIntent().getStringExtra("passphrase");
            setPassphrass(new PinEvent("", passphrase));
            finish();
        } else {
            dealWithBusiness(isNFC);
        }
    }

    private void dealWithChangePin(boolean isNFC) {
        if (features.isBootloaderMode()) {
            Toast.makeText(this, R.string.bootloader_mode, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        if (features.isInitialized()) {
            isChangePin = true;
            new BusinessAsyncTask().setHelper(this).execute(BusinessAsyncTask.CHANGE_PIN, isNFC ? COMMUNICATION_MODE_NFC : COMMUNICATION_MODE_BLE);
        } else {
            Toast.makeText(this, R.string.wallet_un_activated_pin, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    /**
     * dealwith the wipe device logic
     *
     * @param isNFC which communication way we use
     */
    private void dealWithWipeDevice(boolean isNFC) {
        if (features.isBootloaderMode()) {
            Toast.makeText(this, R.string.bootloader_mode, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        if (features.isInitialized()) {
            new BusinessAsyncTask().setHelper(this).execute(BusinessAsyncTask.WIPE_DEVICE, isNFC ? COMMUNICATION_MODE_NFC : COMMUNICATION_MODE_BLE);
//            Intent intent = new Intent(this, PinSettingActivity.class);
//            intent.putExtra("tag", tag);
//            intent.putExtra("pin_type", 1);
//            startActivity(intent);
        } else {
            Toast.makeText(this, R.string.wallet_un_activated, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    /**
     * dealwith the create wallet 、sign、init logic
     *
     * @param isNFC which communication way we use
     */
    private void dealWithBusiness(boolean isNFC) {
        if (features.isBootloaderMode()) {
            Toast.makeText(this, R.string.bootloader_mode, Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        boolean isInit = features.isInitialized();//isInit -->  Judge whether it is activated
        if (isInit) {
            if (MultiSigWalletCreator.TAG.equals(tag) || SingleSigWalletCreator.TAG.equals(tag) || PersonalMultiSigWalletCreator.TAG.equals(tag) || CheckHideWalletFragment.TAG.equals(tag) || ImportHistoryWalletActivity.TAG.equals(tag) || "check_xpub".equals(tag)) {
//                // todo: remove the below pin about code
//                if (!features.isPinCached() && features.isPinProtection()) {
//                    Intent intent = new Intent(this, PinSettingActivity.class);
//                    intent.putExtra("tag", tag);
//                    startActivity(intent);
//                    dialogFragment = showReadingDialog(R.string.reading_dot);
//                }
                if (SingleSigWalletCreator.TAG.equals(tag) || CheckHideWalletFragment.TAG.equals(tag)) {
                    if (CheckHideWalletFragment.TAG.equals(tag)) {
                        customerUI.callAttr("set_pass_state", 1);
                    }
                    new BusinessAsyncTask().setHelper(this).execute(BusinessAsyncTask.GET_EXTEND_PUBLIC_KEY_SINGLE, isNFC ? COMMUNICATION_MODE_NFC : COMMUNICATION_MODE_BLE, "p2wpkh");
                } else {
                    new BusinessAsyncTask().setHelper(this).execute(BusinessAsyncTask.GET_EXTEND_PUBLIC_KEY, isNFC ? COMMUNICATION_MODE_NFC : COMMUNICATION_MODE_BLE);
                }
            } else if (TransactionDetailsActivity.TAG.equals(tag) || SignActivity.TAG.equals(tag) || SignActivity.TAG1.equals(tag) || SignActivity.TAG2.equals(tag) || SignActivity.TAG3.equals(tag) || SendOne2OneMainPageActivity.TAG.equals(tag) || SendOne2ManyMainPageActivity.TAG.equals(tag) || TransactionDetailsActivity.TAG_HIDE_WALLET.equals(tag)) {
                isSign = true;
                if (SignActivity.TAG1.equals(tag) || SignActivity.TAG3.equals(tag)) {
                    if (SignActivity.TAG3.equals(tag)) {
                        //hide wallet sign message -->set_pass_state
                        customerUI.callAttr("set_pass_state", 1);
                    }
                    new BusinessAsyncTask().setHelper(this).execute(BusinessAsyncTask.SIGN_MESSAGE, strinputAddress, extras);
                } else {
                    if (SignActivity.TAG2.equals(tag) || TransactionDetailsActivity.TAG_HIDE_WALLET.equals(tag)) {
                        //hide wallet sign transaction -->set_pass_state
                        customerUI.callAttr("set_pass_state", 1);
                    }
                    new BusinessAsyncTask().setHelper(this).execute(BusinessAsyncTask.SIGN_TX, extras);
                }
//                // todo: remove the if , pin always required
//                if (!features.isPinCached() && features.isPinProtection()) {
//                    Intent intent = new Intent(this, PinSettingActivity.class);
//                    intent.putExtra("tag", "signature");
//                    startActivity(intent);
//                }
            } else if (BackupRecoveryActivity.TAG.equals(tag) || RecoveryActivity.TAG.equals(tag) || BackupMessageActivity.TAG.equals(tag)) {
                if (TextUtils.isEmpty(extras)) {
                    Log.i(TAG, "java ==== backup");
                    new BusinessAsyncTask().setHelper(this).execute(BusinessAsyncTask.BACK_UP, isNFC ? COMMUNICATION_MODE_NFC : COMMUNICATION_MODE_BLE);
                }
            } else if (SettingActivity.TAG.equals(tag)) {
                String strRandom = UUID.randomUUID().toString().replaceAll("-", "");
                //Anti counterfeiting verification
                new BusinessAsyncTask().setHelper(this).execute(BusinessAsyncTask.COUNTER_VERIFICATION, strRandom, isNFC ? COMMUNICATION_MODE_NFC : COMMUNICATION_MODE_BLE);
            } else if (BixinKeyBluetoothSettingActivity.TAG_TRUE.equals(tag) || BixinKeyBluetoothSettingActivity.TAG_FALSE.equals(tag)) {
                if (BixinKeyBluetoothSettingActivity.TAG_TRUE.equals(tag)) {
                    new BusinessAsyncTask().setHelper(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, BusinessAsyncTask.APPLY_SETTING, isNFC ? COMMUNICATION_MODE_NFC : COMMUNICATION_MODE_BLE, "one");
                } else {
                    new BusinessAsyncTask().setHelper(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, BusinessAsyncTask.APPLY_SETTING, isNFC ? COMMUNICATION_MODE_NFC : COMMUNICATION_MODE_BLE, "zero");
                }
            }
        } else {
            if (!TextUtils.isEmpty(extras) && BackupMessageActivity.TAG.equals(tag)) {
                new BusinessAsyncTask().setHelper(this).execute(BusinessAsyncTask.RECOVER, isNFC ? COMMUNICATION_MODE_NFC : COMMUNICATION_MODE_BLE, extras);
                return;
            }
            isActive = true;
            Intent intent = new Intent(this, WalletUnActivatedActivity.class);
            if (SingleSigWalletCreator.TAG.equals(tag)) {
                intent.putExtra("tag_Xpub", tag);
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            startActivity(intent);
        }
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

    //Activate interface
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doInit(InitEvent event) {
        if ("Activate".equals(event.getName())) {
            SharedPreferences preferences = getSharedPreferences("Preferences", MODE_PRIVATE);
            String locate = preferences.getString("language", "");
            String language = "English".equals(locate) ? "english" : "chinese";
            boolean useSe = Optional.of(event.isUseSE()).orElse(true);
            new BusinessAsyncTask().setHelper(this).execute(BusinessAsyncTask.INIT_DEVICE, isNFC ? COMMUNICATION_MODE_NFC : COMMUNICATION_MODE_BLE, "BiXinKEY", language, useSe ? "1" : "");
        }
    }

    //Activate success ,then,get xpub and to back
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void backXpub(SendXpubToSigwallet event) {
        isActive = false;
        //TODO: 获取xpub
        if ("get_xpub_and_send".equals(event.getXpub())) {
            new BusinessAsyncTask().setHelper(this).execute(BusinessAsyncTask.GET_EXTEND_PUBLIC_KEY_SINGLE, isNFC ? COMMUNICATION_MODE_NFC : COMMUNICATION_MODE_BLE, "p2wpkh");
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void doWipe(WipeEvent event) {
        new BusinessAsyncTask().setHelper(this).execute(BusinessAsyncTask.WIPE_DEVICE, isNFC ? COMMUNICATION_MODE_NFC : COMMUNICATION_MODE_BLE);
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onButtonRequest(ButtonRequestEvent event) {
        if (isSign) {
            if (runnables.size() != 0) {
                runOnUiThread(runnables.get(0));
                // in order to prevent repeat invoke
                runnables.clear();
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void exist(ExistEvent event) {
        finish();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_cancel:
                if (mBle != null) {
                    refreshDeviceList(false);
                    mBle.disconnectAll();
                }
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
            if (SignActivity.TAG1.equals(tag) || SignActivity.TAG3.equals(tag)) {
                dialogFragment = showReadingDialog(R.string.message_loading);
            } else {
                dialogFragment = showReadingDialog(R.string.transaction_loading);
            }

        } else if (MultiSigWalletCreator.TAG.equals(tag) || SingleSigWalletCreator.TAG.equals(tag) || PersonalMultiSigWalletCreator.TAG.equals(tag) || CheckHideWalletFragment.TAG.equals(tag) || ImportHistoryWalletActivity.TAG.equals(tag)) {
            // 获取公钥之前需完成的工作
            if (!SingleSigWalletCreator.TAG.equals(tag)) {
                dialogFragment = showReadingDialog(R.string.reading_dot);
            }
        }
    }

    @Override
    public void onException(Exception e) {
        if (dialogFragment != null) {
            dialogFragment.dismiss();
        }
        // EventBus.getDefault().post(new SendingFailedEvent(e));
//        if (hasWindowFocus()) {
        Log.i("TAG-ErrorMsgDialog", "onException: " + e.getMessage());
        if (e.getMessage().contains("(7, 'PIN invalid')") || e.getMessage().contains("May be BiXin cannot pair with your device or invaild password")) {
            showErrorDialog(0, R.string.pin_wrong);
        } else if ("BaseException: BiXin cannot pair with your Trezor.".equals(e.getMessage())) {
            showErrorDialog(R.string.try_another_key, R.string.unpair);
        } else if ("BaseException: failed to recognize transaction encoding for txt: craft fury pig target diagram ...".equals(e.getMessage())) {
            showErrorDialog(R.string.sign_failed, R.string.transaction_parse_error);
        } else if ("BaseException: Sign failed, May be BiXin cannot pair with your device".equals(e.getMessage())) {
            showErrorDialog(R.string.try_another_key, R.string.sign_failed_device);
        } else {
            showErrorDialog(R.string.key_wrong_prompte, R.string.read_pk_failed);
        }
//        }

    }

    @Override
    public void onResult(String s) {
        Log.i("CheckHideWalletFragment", "onResult:sssssssssssssssssssss:::::: "+s);
        if (dialogFragment != null) {
            dialogFragment.dismiss();
        }
        if (isActive) {
            EventBus.getDefault().post(new ResultEvent(s));
            finish();
            return;
        }
        if (MultiSigWalletCreator.TAG.equals(tag) || SingleSigWalletCreator.TAG.equals(tag) || PersonalMultiSigWalletCreator.TAG.equals(tag) || CheckHideWalletFragment.TAG.equals(tag) || ImportHistoryWalletActivity.TAG.equals(tag) || "check_xpub".equals(tag)) {
            xpub = s;
            if (ImportHistoryWalletActivity.TAG.equals(tag)) {
                Intent intent1 = new Intent(this, ChooseHistryWalletActivity.class);
                intent1.putExtra("histry_xpub", xpub);
                startActivity(intent1);
            } else if (SingleSigWalletCreator.TAG.equals(tag)) {
                EventBus.getDefault().post(new ReceiveXpub(xpub));
            } else if ("check_xpub".equals(tag)) {
                Intent intent = new Intent(this, CheckXpubResultActivity.class);
                intent.putExtra("label", features.getLabel());
                intent.putExtra("xpub", s);
                startActivity(intent);
            } else if (CheckHideWalletFragment.TAG.equals(tag)) {
                EventBus.getDefault().post(new CheckHideWalletEvent(xpub));
            } else {
                runOnUiThread(runnables.get(1));
            }
        } else if (isSign) {
            if (SignActivity.TAG1.equals(tag) || SignActivity.TAG3.equals(tag)) {
                EventBus.getDefault().post(new SignMessageEvent(s));
            } else if (SendOne2OneMainPageActivity.TAG.equals(tag) || SendOne2ManyMainPageActivity.TAG.equals(tag)) {
                EventBus.getDefault().postSticky(new SendSignBroadcastEvent(s));
            } else {
                EventBus.getDefault().post(new SignResultEvent(s));
            }
            // runOnUiThread(runnables.get(0));
        } else if (BackupRecoveryActivity.TAG.equals(tag) || BackupMessageActivity.TAG.equals(tag)) {
            if (TextUtils.isEmpty(extras)) {
                SharedPreferences devices = getSharedPreferences("devices", Context.MODE_PRIVATE);
                features.setBackupMessage(s);
                devices.edit().putString(features.getBleName(), features.toString()).apply();
                Intent intent = new Intent(this, BackupMessageActivity.class);
                intent.putExtra("label", Strings.isNullOrEmpty(features.getLabel()) ? features.getBleName(): features.getLabel());
                intent.putExtra("tag", "backup");
                intent.putExtra("message", s);
                startActivity(intent);
            } else {
                Intent intent = new Intent(this, RecoveryResult.class);
                intent.putExtra("tag", s);
                startActivity(intent);
            }
        } else if (RecoverySetActivity.TAG.equals(tag) || HardwareDetailsActivity.TAG.equals(tag) || SettingActivity.TAG_CHANGE_PIN.equals(tag) || SettingActivity.TAG.equals(tag)) {
            EventBus.getDefault().postSticky(new ResultEvent(s));
        } else if (BixinKeyBluetoothSettingActivity.TAG_TRUE.equals(tag) || BixinKeyBluetoothSettingActivity.TAG_FALSE.equals(tag)) {
            EventBus.getDefault().post(new SetBluetoothEvent(s));
        }
        finish();
    }

    @Override
    public void onCancelled() {
//        runOnUiThread(() -> Toast.makeText(this, getString(R.string.task_cancle), Toast.LENGTH_SHORT).show());
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
            String tag = ((CommunicationModeSelector) fragmentActivity).tag;
            boolean isSign = ((CommunicationModeSelector) fragmentActivity).isSign;
            boolean isChangePin = ((CommunicationModeSelector) fragmentActivity).isChangePin;
            switch (msg.what) {
                case PIN_CURRENT:
                    Intent intent = new Intent(fragmentActivity, PinSettingActivity.class);
                    if (!isSign) {
                        intent.putExtra("tag", tag);
                    }
                    if (isChangePin) {
                        intent.putExtra("pin_type", 3);
                    }
                    fragmentActivity.startActivity(intent);
                    break;
                case PIN_NEW_FIRST:
                    Intent intent1 = new Intent(fragmentActivity, PinSettingActivity.class);
                    intent1.putExtra("pin_type", PIN_NEW_FIRST);
                    intent1.putExtra("tag", SettingActivity.TAG_CHANGE_PIN);
                    fragmentActivity.startActivity(intent1);
                    break;
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
