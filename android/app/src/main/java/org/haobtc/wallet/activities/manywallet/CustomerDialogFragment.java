package org.haobtc.wallet.activities.manywallet;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import android.os.Handler;
import android.os.Message;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;


import com.chaquo.python.PyObject;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.ActivatedProcessing;
import org.haobtc.wallet.activities.ConfirmOnHardware;
import org.haobtc.wallet.activities.PinSettingActivity;
import org.haobtc.wallet.activities.TransactionDetailsActivity;
import org.haobtc.wallet.activities.WalletUnActivatedActivity;
import org.haobtc.wallet.fragment.BleDeviceRecyclerViewAdapter;
import org.haobtc.wallet.fragment.BluetoothConnectingFragment;
import org.haobtc.wallet.fragment.BluetoothFragment;
import org.haobtc.wallet.fragment.ReadingPubKeyDialogFragment;
import org.haobtc.wallet.fragment.ReadingPubKeyFailedDialogFragment;
import org.haobtc.wallet.utils.Daemon;
import org.haobtc.wallet.utils.Global;
import org.haobtc.wallet.utils.NfcUtils;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import cn.com.heaton.blelibrary.ble.callback.BleConnectCallback;
import cn.com.heaton.blelibrary.ble.callback.BleNotiftCallback;
import cn.com.heaton.blelibrary.ble.callback.BleScanCallback;
import cn.com.heaton.blelibrary.ble.callback.BleWriteCallback;
import cn.com.heaton.blelibrary.ble.model.BleDevice;
import cn.com.heaton.blelibrary.ble.Ble;


public class CustomerDialogFragment extends DialogFragment implements View.OnClickListener {

    private BleDeviceRecyclerViewAdapter adapter;
    private RelativeLayout relativeLayout;
    private String tag;
    private String extras;
    public static String pin;
    private Runnable runnable;
    public static PyObject pyHandler, customerUI;
    private MyHandler handler;
    public static FutureTask<PyObject> futureTask;
    private static final int PIN_CURRENT = 1;
    private static final int PIN_NEW_FIRST = 2;
    // private static final int PIN_New_SECOND = 3;
    public static final int PIN_REQUEST = 5;
    public static final int REQUEST_ACTIVE = 4;
    public static final int REQUEST_SIGN = 6;
    private static final int REQUEST_ENABLE_BT = 1;
    private BluetoothConnectingFragment fragment;
    public static String xpub;
    private Ble<BleDevice> mBle;
    private ReadingPubKeyDialogFragment dialogFragment;
    private boolean isActive = false;
    private BluetoothFragment bleFragment;

    public CustomerDialogFragment(String tag, @Nullable Runnable runnable, String extra) {
        this.tag = tag;
        this.runnable = runnable;
        this.extras = extra;
    }

    public static class MyHandler extends Handler {
        private WeakReference<FragmentActivity> reference;
        private static volatile MyHandler myHandler;

        private MyHandler(FragmentActivity activity) {
            this.reference = new WeakReference<>(activity);
        }

        public static MyHandler getInstance(FragmentActivity fragmentActivity) {
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
                    Intent intent = new Intent(reference.get(), PinSettingActivity.class);
                    intent.putExtra("pin", PIN_CURRENT);
                    reference.get().startActivityForResult(intent, PIN_REQUEST);
                    break;
                case PIN_NEW_FIRST:
                    Intent intent1 = new Intent(reference.get(), PinSettingActivity.class);
                    intent1.putExtra("pin", PIN_NEW_FIRST);
                    reference.get().startActivityForResult(intent1, PIN_REQUEST);
                    break;
                /*case PIN_New_SECOND:
                    Intent intent2 = new Intent(reference.get(), PinSettingActivity.class);
                    intent2.putExtra("pin", PIN_New_SECOND);
                    reference.get().startActivityForResult(intent2, PIN_REQUEST);  */
            }
        }
    }

    private BleScanCallback<BleDevice> scanCallback = new BleScanCallback<BleDevice>() {
        @Override
        public void onLeScan(final BleDevice device, int rssi, byte[] scanRecord) {
            synchronized (mBle.getLocker()) {
                Log.i("BLE Device Find", device.getBleAddress());
                Activity activity = getActivity();
                if (activity != null) {
                    getActivity().runOnUiThread(() -> adapter.add(device));
                } else {
                    mBle.stopScan();
                }
            }
        }
    };
    private BleConnectCallback<BleDevice> connectCallback = new BleConnectCallback<BleDevice>() {
        @Override
        public void onConnectionChanged(BleDevice device) {
            Log.e("BLE", "onConnectionChanged0: " + device.isConnected());
            if (device.isConnectting()) {
                Objects.requireNonNull(getActivity()).runOnUiThread(() -> {
                    fragment = new BluetoothConnectingFragment();
                    getChildFragmentManager().beginTransaction().replace(R.id.ble_device, fragment).commitNow();
                    relativeLayout.setVisibility(View.GONE);
                });
            }
            Log.e("BLE", "onConnectionChanged1: " + device.isConnected());
            if (device.isConnected()) {
              // if (ManyWalletTogetherActivity.TAG.equals(tag)) {
                    getChildFragmentManager().beginTransaction().replace(R.id.ble_device, bleFragment).commit();
                    dialogFragment = (ReadingPubKeyDialogFragment) showReadingDialog();
              //  }


            }

            Log.e("BLE", "onConnectionChanged: " + device.isConnected());
        }

        @Override
        public void onServicesDiscovered(BleDevice device) {
            super.onServicesDiscovered(device);
            setNotify(device);
        }

        @Override
        public void onReady(BleDevice device) {
            super.onReady(device);
            pyHandler.put("BLE", mBle);
            pyHandler.put("BLE_DEVICE", device);
           // pyHandler.put("BLE_ADDRESS", device.getBleAddress());
            pyHandler.put("CALL_BACK", writeCallBack);
            new Handler().postDelayed(()-> {
                boolean isInit = false; //should be false
                    try {
                        isInit = isInitialized();
                    } catch (Exception e) {
                        Log.e("BLE", e.getMessage() == null ? "unknown error": e.getMessage());
                        if (dialogFragment != null) {
                            dialogFragment.dismiss();
                        }
                        Toast.makeText(getContext(), "communication error, check init status error", Toast.LENGTH_SHORT).show();
                    }
                        if (isInit) {
                            boolean pinCached = false;
                            try {
                                pinCached = Daemon.commands.callAttr("get_pin_status", "bluetooth").toBoolean();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            if (ManyWalletTogetherActivity.TAG.equals(tag)) {
                                // todo: get xpub
                                futureTask = new FutureTask<>(() -> Daemon.commands.callAttr("get_xpub_from_hw"));
                                new Thread(futureTask).start();
                                if (pinCached) {
                                    try {
                                        xpub = futureTask.get(5, TimeUnit.SECONDS).toString();
                                        dialogFragment.dismiss();
                                    } catch (ExecutionException | InterruptedException e) {
                                        dialogFragment.dismiss();
                                        showReadingFailedDialog();
                                    } catch (TimeoutException e) {
                                        e.printStackTrace();
                                    }
                                }
                            } else if (TransactionDetailsActivity.TAG.equals(tag)) {
                                // todo： sign
                                futureTask = new FutureTask<>(() -> Daemon.commands.callAttr("sign_tx", extras));
                                new Thread(futureTask).start();
                                if (pinCached) {
                                    Objects.requireNonNull(getActivity()).runOnUiThread(runnable);
                                    dismiss();
                                }
                            }

                        } else {
                            // todo: Initialized
                            Intent intent = new Intent(getContext(), WalletUnActivatedActivity.class);
                            startActivityForResult(intent, REQUEST_ACTIVE);
                        }


            }, 550);

        }

        @Override
        public void onConnectException(BleDevice device, int errorCode) {
            super.onConnectException(device, errorCode);
            Log.e("BLE", "连接异常，异常状态码:" + errorCode);
        }
    };
    private BleWriteCallback<BleDevice> writeCallBack = new BleWriteCallback<BleDevice>() {

        @Override
        public void onWriteSuccess(BleDevice device, BluetoothGattCharacteristic characteristic) {
            Log.i("BLE", "发送数据成功");
        }

        @Override
        public void onWiteFailed(BleDevice device, String message) {
            super.onWiteFailed(device, message);
        }
    };


    private void setNotify(BleDevice device) {
        /*连接成功后，设置通知*/
        mBle.startNotify(device, new BleNotiftCallback<BleDevice>() {
            @Override
            public void onChanged(BleDevice device, BluetoothGattCharacteristic characteristic) {
                Log.e("BLE", "onChanged: " + Arrays.toString(characteristic.getValue()));
                pyHandler.put("RESPONSE", characteristic.getValue());
            }

            @Override
            public void onNotifySuccess(BleDevice device, BluetoothGatt gatt) {
                super.onNotifySuccess(device, gatt);
            }

            @Override
            public void onNotifyCanceled(BleDevice device) {
                super.onNotifyCanceled(device);
            }
        });
    }

    private boolean isInitialized() throws Exception {
        boolean isInitialized = false;
        try {
            isInitialized = Daemon.commands.callAttr("is_initialized", "bluetooth").toBoolean();
        } catch (Exception e) {
            Toast.makeText(getContext(), "communication error", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            throw e;
        }
        return isInitialized;
    }


    public  DialogFragment showReadingDialog() {
        ReadingPubKeyDialogFragment fragment = new ReadingPubKeyDialogFragment();
        fragment.show(getChildFragmentManager(), "");
        return fragment;
    }

    public void showReadingFailedDialog() {
        ReadingPubKeyFailedDialogFragment fragment = new ReadingPubKeyFailedDialogFragment();
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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ImageView imageViewCancel;
        View view = inflater.inflate(R.layout.bluetooth_nfc, null);
        ImageView imageView = view.findViewById(R.id.touch_nfc);
        TextView textView = view.findViewById(R.id.text_prompt);
        FrameLayout frameLayout = view.findViewById(R.id.ble_device);
        RadioGroup radioGroup = view.findViewById(R.id.radio_group);
        imageViewCancel = view.findViewById(R.id.img_cancel);
        TextView textViewInputByHand = view.findViewById(R.id.text_input_publickey_by_hand);
        relativeLayout = view.findViewById(R.id.input_layout);
        textViewInputByHand.setOnClickListener(this);
        imageViewCancel.setOnClickListener(this);
        mBle = Ble.getInstance();
        adapter = new BleDeviceRecyclerViewAdapter(this.getActivity());
        adapter.setConnectCallback(connectCallback);
        bleFragment = new BluetoothFragment(adapter);
        pyHandler = Global.py.getModule("trezorlib.transport.bluetooth").get("BlueToothHandler");
        customerUI = Global.py.getModule("trezorlib.customer_ui").get("CustomerUI");
        handler = MyHandler.getInstance(getActivity());
        customerUI.put("handler", handler);
        NfcUtils.nfc(getActivity());
        if (TransactionDetailsActivity.TAG.equals(tag)) {
            relativeLayout.setVisibility(View.GONE);
        }
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            switch (checkedId) {
                case R.id.radio_ble:
                    group.check(R.id.radio_ble);
                    frameLayout.setVisibility(View.VISIBLE);
                    imageView.setVisibility(View.GONE);
                    textView.setVisibility(View.GONE);
                    turnOnBlueTooth();
                    if (mBle.isScanning()) {
                        mBle.stopScan();
                    }
                    mBle.startScan(scanCallback);
                    getChildFragmentManager().beginTransaction().replace(R.id.ble_device, bleFragment).commit();
                    break;
                case R.id.radio_nfc:
                    group.check(R.id.radio_nfc);
                    if (mBle.isScanning()) {
                        mBle.stopScan();
                    }
                    BleDeviceRecyclerViewAdapter.mValues.clear();
                    adapter.notifyDataSetChanged();
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


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_cancel:
                if (mBle.isScanning()) {
                    mBle.stopScan();
                }
                dismiss();
                break;
            case R.id.text_input_publickey_by_hand:
                if (mBle.isScanning()) {
                    mBle.stopScan();
                }
                dismiss();
                Activity activity = getActivity();
                if (activity != null) getActivity().runOnUiThread(runnable);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
        } else if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK) {
            if (mBle.isScanning()) {
                mBle.stopScan();
            }
            mBle.startScan(scanCallback);
        } else if (requestCode == REQUEST_ACTIVE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                isActive = data.getBooleanExtra("isActive", false);
                if (isActive) {
                    new Thread(() ->
                            Daemon.commands.callAttr("init")
                    ).start();
                    Intent intent = new Intent(getActivity(), ActivatedProcessing.class);
                    startActivity(intent);
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (NfcUtils.mNfcAdapter != null && NfcUtils.mNfcAdapter.isEnabled()) {
            // enable nfc discovery for the app
            System.out.println("为本App启用NFC感应");
            NfcUtils.mNfcAdapter.enableForegroundDispatch(getActivity(), NfcUtils.mPendingIntent, NfcUtils.mIntentFilter, NfcUtils.mTechList);
        }
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);
        if (NfcUtils.mNfcAdapter != null && NfcUtils.mNfcAdapter.isEnabled()) {
            // disable nfc discovery for the app
            NfcUtils.mNfcAdapter.disableForegroundDispatch(getActivity());
            System.out.println("禁用本App的NFC感应");
        }
    }
}
