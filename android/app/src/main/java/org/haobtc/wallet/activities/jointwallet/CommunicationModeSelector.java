package org.haobtc.wallet.activities.jointwallet;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import com.chaquo.python.PyObject;
import com.google.gson.Gson;
import com.tbruyelle.rxpermissions2.RxPermissions;

import org.greenrobot.eventbus.EventBus;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.ActivatedProcessing;
import org.haobtc.wallet.activities.PinSettingActivity;
import org.haobtc.wallet.activities.ResetDeviceProcessing;
import org.haobtc.wallet.activities.TransactionDetailsActivity;
import org.haobtc.wallet.activities.WalletUnActivatedActivity;
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
import org.haobtc.wallet.activities.settings.recovery_set.RecoverySetActivity;
import org.haobtc.wallet.activities.sign.SignActivity;
import org.haobtc.wallet.asynctask.BusinessAsyncTask;
import org.haobtc.wallet.bean.HardwareFeatures;
import org.haobtc.wallet.dfu.service.DfuService;
import org.haobtc.wallet.event.FirstEvent;
import org.haobtc.wallet.event.ResultEvent;
import org.haobtc.wallet.event.SignMessageEvent;
import org.haobtc.wallet.event.SignResultEvent;
import org.haobtc.wallet.fragment.BleDeviceRecyclerViewAdapter;
import org.haobtc.wallet.fragment.BluetoothConnectingFragment;
import org.haobtc.wallet.fragment.BluetoothFragment;
import org.haobtc.wallet.fragment.ReadingPubKeyDialogFragment;
import org.haobtc.wallet.fragment.ReadingPubKeyFailedDialogFragment;
import org.haobtc.wallet.utils.CommonUtils;
import org.haobtc.wallet.utils.Daemon;
import org.haobtc.wallet.utils.Global;
import org.haobtc.wallet.utils.NfcUtils;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
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
import cn.com.heaton.blelibrary.ble.callback.BleConnectCallback;
import cn.com.heaton.blelibrary.ble.callback.BleNotiftCallback;
import cn.com.heaton.blelibrary.ble.callback.BleScanCallback;
import cn.com.heaton.blelibrary.ble.callback.BleWriteCallback;
import cn.com.heaton.blelibrary.ble.model.BleDevice;
import no.nordicsemi.android.dfu.DfuServiceInitiator;

import static org.haobtc.wallet.activities.sign.SignActivity.strinputAddress;


public class CommunicationModeSelector extends DialogFragment implements View.OnClickListener, BusinessAsyncTask.Helper {

    // private static final int PIN_New_SECOND = 3;
    public static final String TAG = "BLE";
    public static final int PIN_REQUEST = 5;
    public static final int REQUEST_ACTIVE = 4;
    public static final int SHOW_PROCESSING = 7;
    public static final int PIN_CURRENT = 1;
    public static final int PIN_NEW_FIRST = 2;
    public static final int PASS_NEW_PASSPHRASS = 6;
    public static final int PASS_PASSPHRASS = 3;
    private static final int REQUEST_ENABLE_BT = 1;
    public static final int PASSPHRASS_INPUT = 8;
    public static volatile String pin = "";
    public static PyObject pyHandler, customerUI;
    private PyObject ble;
    public static MyHandler handler;
    public static FutureTask<PyObject> futureTask;
    public static ExecutorService executorService = Executors.newCachedThreadPool();
    public static String xpub;
    public static volatile boolean isActive = false;
    public static volatile boolean isNFC;
    private static boolean isErrorOccurred;
    private final IntentFilter bondFilter = new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
    private BleDeviceRecyclerViewAdapter adapter;
    private RelativeLayout relativeLayout;
    private String tag;
    private String extras;
    private List<Runnable> runnables;
    private BluetoothConnectingFragment fragment;
    private Ble<BleDevice> mBle;
    private ReadingPubKeyDialogFragment dialogFragment;
    private BluetoothFragment bleFragment;
    private boolean isBonded;
    public static volatile boolean isDfu;
    private RxPermissions permissions;
    private static final String COMMUNICATION_MODE_BLE = "bluetooth";
    public static final String COMMUNICATION_MODE_NFC = "nfc";


    private final BleWriteCallback<BleDevice> writeCallBack = new BleWriteCallback<BleDevice>() {

        @Override
        public void onWriteSuccess(BleDevice device, BluetoothGattCharacteristic characteristic) {
            Log.d(TAG, "send successful:" + CommonUtils.bytes2hex(characteristic.getValue()));
            ble.put("WRITE_SUCCESS", true);
        }

        @Override
        public void onWiteFailed(BleDevice device, int failedCode) {
            super.onWiteFailed(device, failedCode);
            Log.e(TAG, "send failed:" + failedCode);
        }
    };

    private final BleScanCallback<BleDevice> scanCallback = new BleScanCallback<BleDevice>() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onLeScan(final BleDevice device, int rssi, byte[] scanRecord) {
            synchronized (mBle.getLocker()) {
                Log.d(TAG, "BLE Device Find====" + device.getBleName());
                Activity activity = getActivity();
                if (activity != null) {
                    getActivity().runOnUiThread(() -> adapter.add(device));
                } else {
                    mBle.stopScan();
                }
            }
        }
    };

    private final Runnable retry = new Runnable() {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void run() {
            if (isNFC) {
                return;
            }
            if (BleDeviceRecyclerViewAdapter.device.getBondState() == BluetoothDevice.BOND_BONDED) {
                if (BleDeviceRecyclerViewAdapter.mBleDevice.isConnected()) {
                    connectCallback.onReady(BleDeviceRecyclerViewAdapter.mBleDevice);
                } else {
                    mBle.connect(BleDeviceRecyclerViewAdapter.mBleDevice, connectCallback);
                }
            } else {
                BleDeviceRecyclerViewAdapter.device.createBond();
                isBonded = true;
            }
        }
    };
    private final BleConnectCallback<BleDevice> connectCallback = new BleConnectCallback<BleDevice>() {
        @Override
        public void onConnectionChanged(BleDevice device) {
            if (device.isConnectting()) {
                Log.i(TAG, "connecting---" + device.getBleName());
                showConnecting();
            }
            if (BleDeviceRecyclerViewAdapter.device.getBondState() != BluetoothDevice.BOND_BONDED) {
                return;
            }
            if (device.isConnected()) {
                Log.i(TAG, "connected---" + device.getBleName());
            }

        }

        @Override
        public void onServicesDiscovered(BleDevice device, List<BluetoothGattService> gattServices) {
            super.onServicesDiscovered(device, gattServices);
            setNotify(device);
        }

        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onReady(BleDevice device) {
            super.onReady(device);
            showConnecting();
            isNFC = false;
            pyHandler.put("BLE", mBle);
            pyHandler.put("BLE_DEVICE", device);
            pyHandler.put("CALL_BACK", writeCallBack);
            Instant begin = Instant.now();
            while (true) {
                BluetoothGattCharacteristic characteristic = mBle.getBleRequest().getReadCharacteristic(BleDeviceRecyclerViewAdapter.device.getAddress());
                BluetoothGattDescriptor notify = null;
                if (characteristic != null) {
                    notify = characteristic.getDescriptor(UUID.fromString("00002902-0000-1000-8000-00805f9b34fb"));
                }
                boolean isNotify = false;
                if (notify != null) {
                    isNotify = Arrays.equals(notify.getValue(), BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                }
                if (isNotify) {
                    new Handler().postDelayed(() -> {
                        if (VersionUpgradeActivity.TAG.equals(tag)) {
                            if ("hardware".equals(extras)) {
                                Intent intent = new Intent(getActivity(), UpgradeBixinKEYActivity.class);
                                intent.putExtra("way", COMMUNICATION_MODE_BLE);
                                intent.putExtra("tag", 1);
                                startActivity(intent);
                            } else if ("ble".equals(extras)) {
                                dfu();
                            }
                        } else if (HardwareDetailsActivity.TAG.equals(tag)) {
                            dealWithChangePin();
                        } else if (RecoverySetActivity.TAG.equals(tag)) {
                            dealWithWipeDevice();
                        } else {
                            dealWithBusiness();
                        }
                    }, isBonded ? 3000 : 1600);
                    break;
                } else {
                    setNotify(BleDeviceRecyclerViewAdapter.mBleDevice);
                }
                if (Duration.between(begin, Instant.now()).toMillis() > 10000) {
                    showReadingFailedDialog(R.string.timeout_error);
                    break;
                }
                if (isErrorOccurred) {
                    isErrorOccurred = false;
                    dismiss();
                    break;
                }
            }
        }

        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onConnectException(BleDevice device, int errorCode) {
            super.onConnectException(device, errorCode);
            Log.e(TAG, String.format("连接异常，异常状态码: %d", errorCode));
            isErrorOccurred = true;
            if (isDfu) {
                return;
            }
            switch (errorCode) {
                case 2523:
                    Activity activity = getActivity();
                    if (activity != null) {
                        activity.runOnUiThread(() -> Toast.makeText(getContext(), getString(R.string.bluetooth_abnormal), Toast.LENGTH_LONG).show());
                    }
                    dismiss();
                    break;
                case 133:
                    mBle.disconnect(device);
                    handler.postDelayed(() -> mBle.connect(device, connectCallback), 1_000);
                    break;
                case 8:
                    break;
                case 2521:
                    Activity activity1 = getActivity();
                    if (activity1 != null) {
                        activity1.runOnUiThread(() -> Toast.makeText(getContext(), getString(R.string.bluetooth_fail), Toast.LENGTH_LONG).show());
                    }
                    dismiss();
                    break;
                default:
                    dismiss();
            }
        }

        @Override
        public void onConnectCancel(BleDevice device) {
            super.onConnectCancel(device);
            Log.i(TAG, "正在取消连接");

        }

        @Override
        public void onConnectTimeOut(BleDevice device) {
            super.onConnectTimeOut(device);
            Log.e(TAG, String.format("连接设备==%s超时", device.getBleName()));
            dismiss();
        }
    };


    @SuppressLint("SdCardPath")
    private void dfu() {
        List<BleDevice> devices = Ble.getInstance().getConnetedDevices();
        if (devices.size() == 0) {
            Log.d(TAG, "没有已连接设备");
            dismiss();
            return;
        }
        final DfuServiceInitiator starter = new DfuServiceInitiator(devices.get(0).getBleAddress());
        starter.setDeviceName(devices.get(0).getBleName());
        /*
        调用此方法使Nordic nrf52832进入bootloader模式
*/
        starter.setUnsafeExperimentalButtonlessServiceInSecureDfuEnabled(true);
        starter.setZip(null, "/sdcard/Android/data/org.haobtc.wallet/cache/ble.zip");
        DfuServiceInitiator.createDfuNotificationChannel(Objects.requireNonNull(getContext()));
        starter.start(getContext(), DfuService.class);
        isDfu = true;
    }

    private final BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i(TAG, "receive broadcast===" + action);
            if (BleDeviceRecyclerViewAdapter.device == null) {
                return;
            }
            if (fragment == null && BleDeviceRecyclerViewAdapter.device.getBondState() == BluetoothDevice.BOND_BONDING) {
                fragment = new BluetoothConnectingFragment();
                getChildFragmentManager().beginTransaction().replace(R.id.ble_device, fragment).commitNow();
                relativeLayout.setVisibility(View.GONE);
            }
            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                if (BleDeviceRecyclerViewAdapter.device != null) {
                    if (BleDeviceRecyclerViewAdapter.device.getBondState() == BluetoothDevice.BOND_BONDED &&
                            !BleDeviceRecyclerViewAdapter.mBleDevice.isConnected()) {
                        mBle.connect(BleDeviceRecyclerViewAdapter.mBleDevice, connectCallback);
                        isBonded = true;
                    }
                }
            }
        }
    };

    public CommunicationModeSelector(String tag, @Nullable List<Runnable> runnables, String extra) {
        this.tag = tag;
        this.runnables = runnables;
        this.extras = extra;
    }

    private void dealWithChangePin() {
        HardwareFeatures features;
        try {
            features = getFeatures();
        } catch (Exception e) {
            if ("bootloader mode".equals(e.getMessage())) {
                Toast.makeText(getContext(), R.string.bootloader_mode, Toast.LENGTH_LONG).show();
                dismiss();
            }
            return;
        }
        if (features.isInitialized()) {
            new BusinessAsyncTask().setHelper(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, BusinessAsyncTask.CHANGE_PIN, COMMUNICATION_MODE_BLE);
        } else {
            Toast.makeText(getActivity(), R.string.wallet_un_activated_pin, Toast.LENGTH_LONG).show();
            dismiss();
        }


    }

    private void dealWithWipeDevice() {
        HardwareFeatures features;
        try {
            features = getFeatures();
        } catch (Exception e) {
            if ("bootloader mode".equals(e.getMessage())) {
                Toast.makeText(getContext(), R.string.bootloader_mode, Toast.LENGTH_LONG).show();
                dismiss();
            }
            return;
        }
        if (features.isInitialized()) {
            new BusinessAsyncTask().setHelper(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, BusinessAsyncTask.WIPE_DEVICE, COMMUNICATION_MODE_BLE);
            Intent intent = new Intent(getActivity(), ResetDeviceProcessing.class);
            Objects.requireNonNull(getActivity()).startActivity(intent);
        } else {
            Toast.makeText(getActivity(), R.string.wallet_un_activated, Toast.LENGTH_LONG).show();
            dismiss();
        }
    }

    private void dealWithBusiness() {
        HardwareFeatures features;
        try {
            features = getFeatures();
        } catch (Exception e) {
            if ("bootloader mode".equals(e.getMessage())) {
                Toast.makeText(getContext(), R.string.bootloader_mode, Toast.LENGTH_LONG).show();
                dismiss();
            }
            return;
        }
        boolean isInit = features.isInitialized();
        if (isInit) {
            if (MultiSigWalletCreator.TAG.equals(tag) || SingleSigWalletCreator.TAG.equals(tag) || PersonalMultiSigWalletCreator.TAG.equals(tag) || HideWalletActivity.TAG.equals(tag) || ImportHistoryWalletActivity.TAG.equals(tag)) {
                if (SingleSigWalletCreator.TAG.equals(tag) || HideWalletActivity.TAG.equals(tag)) {
                    if (HideWalletActivity.TAG.equals(tag)) {
                        customerUI.callAttr("set_pass_state", 1);
                    }
                    new BusinessAsyncTask().setHelper(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, BusinessAsyncTask.GET_EXTEND_PUBLIC_KEY_SINGLE, COMMUNICATION_MODE_BLE, "p2wpkh");
                } else {
                    new BusinessAsyncTask().setHelper(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, BusinessAsyncTask.GET_EXTEND_PUBLIC_KEY, COMMUNICATION_MODE_BLE);
                }
            } else if (TransactionDetailsActivity.TAG.equals(tag) || SignActivity.TAG.equals(tag) || SignActivity.TAG1.equals(tag) || SignActivity.TAG2.equals(tag) || SignActivity.TAG3.equals(tag)) {
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
                        if (features.isPinCached()) {
                            Objects.requireNonNull(getActivity()).runOnUiThread(runnables.get(0));
                        }
                    }
                    new BusinessAsyncTask().setHelper(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, BusinessAsyncTask.SIGN_TX, extras);
                }
            } else if (BackupRecoveryActivity.TAG.equals(tag)) {
                if (TextUtils.isEmpty(extras)) {
                    Log.i(TAG, "java ==== backup");
                    new BusinessAsyncTask().setHelper(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, BusinessAsyncTask.BACK_UP, COMMUNICATION_MODE_BLE);
                } else {
                    new BusinessAsyncTask().setHelper(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, BusinessAsyncTask.RECOVER, COMMUNICATION_MODE_BLE, extras);
                }
            }
        } else {
            Intent intent = new Intent(getContext(), WalletUnActivatedActivity.class);
            startActivityForResult(intent, REQUEST_ACTIVE);
        }
    }

    private void showConnecting() {
        Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
            if (fragment == null) {
                fragment = new BluetoothConnectingFragment();
            }
            getChildFragmentManager().beginTransaction().replace(R.id.ble_device, fragment, "connecting").commitNow();
            relativeLayout.setVisibility(View.GONE);
        });
    }

    private void setNotify(BleDevice device) {
        /*Set up notifications when the connection is successful*/
        mBle.enableNotify(device, true, new BleNotiftCallback<BleDevice>() {
            @Override
            public void onChanged(BleDevice device, BluetoothGattCharacteristic characteristic) {
                Log.e(TAG, "receive from hardware: " + CommonUtils.bytes2hex(characteristic.getValue()));
                pyHandler.put("RESPONSE", characteristic.getValue());
            }

            @Override
            public void onNotifySuccess(BleDevice device) {
                super.onNotifySuccess(device);
                Log.d(TAG, "onNotifySuccess: " + device.getBleName());
            }

            @Override
            public void onNotifyCanceled(BleDevice device) {
                super.onNotifyCanceled(device);
            }
        });
    }

    private HardwareFeatures getFeatures() throws Exception {
        String feature;
        try {
            futureTask = new FutureTask<>(() -> Daemon.commands.callAttr("get_feature", COMMUNICATION_MODE_BLE));
            executorService.submit(futureTask);
            feature = futureTask.get(10, TimeUnit.SECONDS).toString();
            HardwareFeatures features = new Gson().fromJson(feature, HardwareFeatures.class);
            if (features.isBootloaderMode()) {
                throw new Exception("bootloader mode");
            }
            SharedPreferences devices = Objects.requireNonNull(getActivity()).getSharedPreferences("devices", Context.MODE_PRIVATE);
            if (!devices.contains(features.getDeviceId())) {
                String bleName = Ble.getInstance().getConnetedDevices().get(0).getBleName();
                features.setBleName(bleName);
                feature = features.toString();
                devices.edit().putString(features.getDeviceId(), feature).apply();
            }
            return features;
        } catch (ExecutionException | InterruptedException | TimeoutException e) {
            Toast.makeText(getContext(), getString(R.string.no_message), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            throw e;
        }
    }

    public ReadingPubKeyDialogFragment showReadingDialog() {
        getChildFragmentManager().beginTransaction().replace(R.id.ble_device, bleFragment).commit();
        ReadingPubKeyDialogFragment fragment = new ReadingPubKeyDialogFragment();
        fragment.show(getChildFragmentManager(), "");
        return fragment;
    }

    public void showReadingFailedDialog(int error) {
        ReadingPubKeyFailedDialogFragment fragment = new ReadingPubKeyFailedDialogFragment(error);
        fragment.setRunnable(retry);
        fragment.setActivity(getActivity());
        fragment.show(getChildFragmentManager(), "");
    }

    private void turnOnBlueTooth() {
        // Ensures Bluetooth is enabled on the device.  If Bluetooth is not currently enabled,
        // fire an intent to display a dialog asking the user to grant permission to enable it.
        if (!mBle.isBleEnable()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ImageView imageViewCancel;
        View view = inflater.inflate(R.layout.bluetooth_nfc, null);
        SharedPreferences preferences = getActivity().getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        boolean bluetoothStatus = preferences.getBoolean("bluetoothStatus", false);
        ImageView imageView = view.findViewById(R.id.touch_nfc);
        TextView textView = view.findViewById(R.id.text_prompt);
        FrameLayout frameLayout = view.findViewById(R.id.ble_device);
        RadioGroup radioGroup = view.findViewById(R.id.radio_group);
        RadioButton radioBle = view.findViewById(R.id.radio_ble);
        imageViewCancel = view.findViewById(R.id.img_cancel);
        TextView textViewInputByHand = view.findViewById(R.id.text_input_publickey_by_hand);
        relativeLayout = view.findViewById(R.id.input_layout);
        textViewInputByHand.setOnClickListener(this);
        imageViewCancel.setOnClickListener(this);
        if (!bluetoothStatus) {
            radioBle.setVisibility(View.GONE);
        }
        mBle = Ble.getInstance();
        adapter = new BleDeviceRecyclerViewAdapter(this.getActivity());
        adapter.setConnectCallback(connectCallback);
        bleFragment = new BluetoothFragment(adapter);
        ble = Global.py.getModule("trezorlib.transport.bluetooth");
        pyHandler = ble.get("BlueToothHandler");
        customerUI = Global.py.getModule("trezorlib.customer_ui").get("CustomerUI");
        handler = MyHandler.getInstance(getActivity());
        customerUI.put("handler", handler);
        NfcUtils.nfc(getActivity(), true);
        if (!MultiSigWalletCreator.TAG.equals(tag)) {
            relativeLayout.setVisibility(View.GONE);
        }
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.radio_ble:
                    frameLayout.setVisibility(View.VISIBLE);
                    imageView.setVisibility(View.GONE);
                    textView.setVisibility(View.GONE);
                    getChildFragmentManager().beginTransaction().replace(R.id.ble_device, bleFragment).commit();
                    permissions = new RxPermissions(this);
                    permissions.request(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION).subscribe(
                            granted -> {
                                if (granted) {
                                    group.check(R.id.radio_ble);
                                    turnOnBlueTooth();
                                    refreshDeviceList(true);
                                } else {
                                    Toast.makeText(getContext(), getString(R.string.blurtooth_need_permission), Toast.LENGTH_LONG).show();
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
        Window window = Objects.requireNonNull(getDialog()).getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(R.color.transparent);
            window.getDecorView().setPadding(0, 0, 0, 0);
            WindowManager.LayoutParams wlp = window.getAttributes();
            wlp.gravity = Gravity.BOTTOM;
            wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
            wlp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(wlp);
        }
        return view;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
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


    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_cancel:
                refreshDeviceList(false);
                dismiss();
                break;
            case R.id.text_input_publickey_by_hand:
                refreshDeviceList(false);
                Activity activity = getActivity();
                if (activity != null) getActivity().runOnUiThread(runnables.get(0));
                dismiss();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(getContext(), getString(R.string.open_bluetooth), Toast.LENGTH_LONG).show();
        } else if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK) {
            refreshDeviceList(true);
        } else if (requestCode == REQUEST_ACTIVE && resultCode == Activity.RESULT_OK) { // Bluetooth activation
            if (data != null) {
                isActive = data.getBooleanExtra("isActive", false);
                if (isActive) {
                    new BusinessAsyncTask().setHelper(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, BusinessAsyncTask.INIT_DEVICE, COMMUNICATION_MODE_BLE);
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Objects.requireNonNull(getActivity()).registerReceiver(broadcastReceiver, bondFilter);
        if (NfcUtils.mNfcAdapter != null && NfcUtils.mNfcAdapter.isEnabled()) {
            // enable nfc discovery for the app
            Log.i("NFC", "为本App启用NFC感应");
            NfcUtils.mNfcAdapter.enableForegroundDispatch(getActivity(), NfcUtils.mPendingIntent, NfcUtils.mIntentFilter, NfcUtils.mTechList);
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
            NfcUtils.mNfcAdapter.disableForegroundDispatch(getActivity());
            Log.i("NFC", "禁用本App的NFC感应");
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        MyHandler.myHandler = null;
        Objects.requireNonNull(getActivity()).unregisterReceiver(broadcastReceiver);
    }

    @Override
    public void onPreExecute() {
        if (isActive) {
            return;
        }
        if (MultiSigWalletCreator.TAG.equals(tag) || SingleSigWalletCreator.TAG.equals(tag) || PersonalMultiSigWalletCreator.TAG.equals(tag) || HideWalletActivity.TAG.equals(tag) || ImportHistoryWalletActivity.TAG.equals(tag)) {
            // 获取公钥之前需完成的工作
            dialogFragment = showReadingDialog();
        } /*else if (TransactionDetailsActivity.TAG.equals(tag)|| SignActivity.TAG.equals(tag)|| SignActivity.TAG1.equals(tag)) {
            Objects.requireNonNull(getActivity()).runOnUiThread(runnables.get(0));
        }*/
    }

    @Override
    public void onException(Exception e) {
        dialogFragment.dismiss();
        if ("BaseException: (7, 'PIN invalid')".equals(e.getMessage())) {
            showReadingFailedDialog(R.string.pin_wrong);
        } else {
            showReadingFailedDialog(R.string.read_pk_failed);
        }
    }

    @Override
    public void onResult(String s) {
        Log.i(TAG, "onResult:…………………………………………………………………… " + s);
        if (dialogFragment != null) {
            dialogFragment.dismiss();
        }
        if (isActive) {
            EventBus.getDefault().post(new ResultEvent(s));
            isActive = false;
            return;
        }
        if (MultiSigWalletCreator.TAG.equals(tag) || SingleSigWalletCreator.TAG.equals(tag) || PersonalMultiSigWalletCreator.TAG.equals(tag) || HideWalletActivity.TAG.equals(tag) || ImportHistoryWalletActivity.TAG.equals(tag)) {
            // todo: 获取公钥
            xpub = s;
            if (ImportHistoryWalletActivity.TAG.equals(tag)) {
                Intent intent1 = new Intent(getActivity(), ChooseHistryWalletActivity.class);
                intent1.putExtra("histry_xpub", xpub);
                startActivity(intent1);
            } else {
                Objects.requireNonNull(getActivity()).runOnUiThread(runnables.get(1));
            }
        } else if (TransactionDetailsActivity.TAG.equals(tag) || SignActivity.TAG.equals(tag) || SignActivity.TAG1.equals(tag) || SignActivity.TAG2.equals(tag) || SignActivity.TAG3.equals(tag)) {
            if (SignActivity.TAG1.equals(tag) || SignActivity.TAG3.equals(tag)) {
                EventBus.getDefault().post(new SignMessageEvent(s));
            } else {
                EventBus.getDefault().post(new SignResultEvent(s));
            }
            // 获取签名后的动作
        } else if (BackupRecoveryActivity.TAG.equals(tag)) {
            if (TextUtils.isEmpty(extras)) {
                // TODO: 获取加密后的私钥
            } else {
                // todo: 恢复结果
            }
        } else if (RecoverySetActivity.TAG.equals(tag) || HardwareDetailsActivity.TAG.equals(tag)) {
            EventBus.getDefault().post(new ResultEvent(s));
        }
    }

    @Override
    public void onCancelled() {
        Activity activity = getActivity();
        if (activity != null) {
            activity.runOnUiThread(() -> Toast.makeText(getActivity(), getString(R.string.task_cancle), Toast.LENGTH_SHORT).show());
        }
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
                case PIN_CURRENT:
                    Intent intent = new Intent(fragmentActivity, PinSettingActivity.class);
                    intent.putExtra("pin", PIN_CURRENT);
                    fragmentActivity.startActivityForResult(intent, PIN_REQUEST);
                    break;
                case PIN_NEW_FIRST:
                    Intent intent1 = new Intent(fragmentActivity, PinSettingActivity.class);
                    intent1.putExtra("pin", PIN_NEW_FIRST);
                    fragmentActivity.startActivityForResult(intent1, PIN_REQUEST);
                    break;
                case SHOW_PROCESSING:
                    EventBus.getDefault().post(new FirstEvent("33"));
                    Intent intent2 = new Intent(fragmentActivity, ActivatedProcessing.class);
                    fragmentActivity.startActivity(intent2);
                    break;
                case PASS_NEW_PASSPHRASS:
                    //Set password
                    Intent intent3 = new Intent(fragmentActivity, HideWalletSetPassActivity.class);
                    fragmentActivity.startActivityForResult(intent3, PASSPHRASS_INPUT);
                    break;
                case PASS_PASSPHRASS:
                    //Set password
                    Intent intent4 = new Intent(fragmentActivity, HideWalletSetPassActivity.class);
                    fragmentActivity.startActivityForResult(intent4, PASSPHRASS_INPUT);
                    break;
            }
        }
    }
}
