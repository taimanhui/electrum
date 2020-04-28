package org.haobtc.wallet.activities.settings;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Build;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.chaquo.python.PyObject;
import com.tbruyelle.rxpermissions2.RxPermissions;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.activities.jointwallet.CommunicationModeSelector;
import org.haobtc.wallet.entries.FsActivity;
import org.haobtc.wallet.utils.Global;

import java.util.ArrayList;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import dr.android.fileselector.FileSelectConstant;
import no.nordicsemi.android.dfu.DfuProgressListener;
import no.nordicsemi.android.dfu.DfuProgressListenerAdapter;
import no.nordicsemi.android.dfu.DfuServiceListenerHelper;

public class VersionUpgradeActivity extends BaseActivity {

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
    public String pin = "";
    public final static String TAG = VersionUpgradeActivity.class.getSimpleName();
    @BindView(R.id.btn_import_file)
    Button btnImportFile;
    private int checkWitch = 1;
    public static final String UPDATE_PROCESS = "org.haobtc.wallet.activities.settings.percent";
    CommunicationModeSelector dialog;
    private RxPermissions rxPermissions;
    public static String filePath;

    @Override
    public int getLayoutId() {
        return R.layout.activity_version_upgrade;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        rxPermissions = new RxPermissions(this);
        Intent intent = getIntent();
        String firmwareVersion = intent.getStringExtra("firmwareVersion");
        String bleVerson = intent.getStringExtra("bleVerson");
        tetFirmware.setText(firmwareVersion);
        tetBluetooth.setText(bleVerson);

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void initData() {
        checkBoxClick();
        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        assert usbManager != null;
        System.out.println("deveice size" + usbManager.getDeviceList().size());
        usbManager.getDeviceList().entrySet().forEach(stringUsbDeviceEntry -> {
            System.out.println("fonud device===" + stringUsbDeviceEntry.getValue().getDeviceName() + "====" + stringUsbDeviceEntry.getValue().getProductId()
                    + "=====" + stringUsbDeviceEntry.getValue().getProductName() + "===" + stringUsbDeviceEntry.getValue().getVendorId());
        });
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

    @OnClick({R.id.img_back, R.id.btn_toUpgrade, R.id.btn_import_file})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.btn_toUpgrade:
                switch (checkWitch) {
                    case 0:
                        mToast(getString(R.string.please_choose_firmware));
                        break;
                    case 1:
                        dialog = new CommunicationModeSelector(TAG, null, "hardware");
                        dialog.show(getSupportFragmentManager(), "");
                        break;
                    case 2:
                        dialog = new CommunicationModeSelector(TAG, null, "ble");
                        dialog.show(getSupportFragmentManager(), "");
                        break;
                }
                break;
            case R.id.btn_import_file:
                rxPermissions
                        .request(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .subscribe(granted -> {
                            if (granted) { // Always true pre-M
                                Intent intent1 = new Intent();
                                intent1.setClass(getApplicationContext(), FsActivity.class);
                                intent1.putExtra(FileSelectConstant.SELECTOR_REQUEST_CODE_KEY, FileSelectConstant.SELECTOR_MODE_FILE);
                                intent1.addCategory(Intent.CATEGORY_OPENABLE);
                                intent1.putExtra("keyFile", "1");
                                startActivityForResult(intent1, 1);

                            } else { // Oups permission denied
                                Toast.makeText(this, R.string.reservatpion_photo, Toast.LENGTH_SHORT).show();
                            }
                        }).dispose();
                break;
        }
    }


    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String action = intent.getAction(); // get the action of the coming intent
        if (Objects.equals(action, NfcAdapter.ACTION_NDEF_DISCOVERED) // NDEF type
                || Objects.equals(action, NfcAdapter.ACTION_TECH_DISCOVERED)
                || Objects.requireNonNull(action).equals(NfcAdapter.ACTION_TAG_DISCOVERED)) {
            Tag tags = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            PyObject nfc = Global.py.getModule("trezorlib.transport.nfc");
            PyObject nfcHandler = nfc.get("NFCHandle");
            nfcHandler.put("device", tags);
            Intent intent1 = new Intent(this, UpgradeBixinKEYActivity.class);
            switch (checkWitch) {
                case 1:
                    intent1.putExtra("tag", 1);
                    break;
                case 2:
                    intent1.putExtra("tag", 2);
                    break;
            }
            startActivity(intent1);
            Intent intent2 = new Intent();
            intent2.setAction(UpgradeBixinKEYActivity.EXECUTE_TASK);
            new Handler().postDelayed(() -> sendBroadcast(intent2), 1000);

        } else if (action.equals(UsbManager.ACTION_USB_DEVICE_ATTACHED)) {
            UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
            UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            assert device != null;
            Toast.makeText(this, "productid===" + device.getProductId() + "venderid====" + device.getVendorId(), Toast.LENGTH_LONG).show();
            device.getInterfaceCount();


        }
    }

    private final DfuProgressListener dfuProgressListener = new DfuProgressListenerAdapter() {
        @Override
        public void onDfuCompleted(@NonNull String deviceAddress) {
            super.onDfuCompleted(deviceAddress);
            CommunicationModeSelector.isDfu = false;
            mIntent(UpgradeFinishedActivity.class);
        }

        @Override
        public void onDfuProcessStarted(@NonNull String deviceAddress) {
            super.onDfuProcessStarted(deviceAddress);
        }

        Intent intent;

        @Override
        public void onDfuProcessStarting(@NonNull String deviceAddress) {
            super.onDfuProcessStarting(deviceAddress);
            if (intent == null) {
                intent = new Intent(VersionUpgradeActivity.this, UpgradeBixinKEYActivity.class);
                intent.putExtra("tag", 2);
                startActivity(intent);
            }
        }

        @Override
        public void onDfuAborted(@NonNull String deviceAddress) {
            super.onDfuAborted(deviceAddress);
        }

        @Override
        public void onProgressChanged(@NonNull String deviceAddress, int percent, float speed, float avgSpeed, int currentPart, int partsTotal) {
            super.onProgressChanged(deviceAddress, percent, speed, avgSpeed, currentPart, partsTotal);
            Intent intent = new Intent();
            intent.setAction(UPDATE_PROCESS);
            intent.putExtra("process", percent);
            LocalBroadcastManager.getInstance(VersionUpgradeActivity.this).sendBroadcast(intent);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        DfuServiceListenerHelper.registerProgressListener(this, dfuProgressListener);
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DfuServiceListenerHelper.unregisterProgressListener(this, dfuProgressListener);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK) {
            //import file
            assert data != null;
            ArrayList<String> listExtra = data.getStringArrayListExtra(FileSelectConstant.SELECTOR_BUNDLE_PATHS);
            assert listExtra != null;
            String str = listExtra.toString();
            String substring = str.substring(1);
            filePath = substring.substring(0, substring.length() - 1);
            Log.i("listExtra", "listExtra--: " + listExtra + "   strPath ---  " + filePath);
            mToast(filePath);
        }
    }

    BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (device != null) {
                    // call your method that cleans up and closes communication with the device
                    UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
                    device.getInterface(0);
                    UsbInterface intf = device.getInterface(0);
                    UsbEndpoint endpoint = intf.getEndpoint(0);
                    UsbDeviceConnection connection = usbManager.openDevice(device);
                    connection.close();
                    /*connection.claimInterface(intf, forceClaim);
                    connection.bulkTransfer(endpoint, bytes, bytes.length, TIMEOUT);*/
                }
            }
        }
    };
}
