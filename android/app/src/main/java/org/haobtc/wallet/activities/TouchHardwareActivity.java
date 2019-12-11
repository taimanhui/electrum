package org.haobtc.wallet.activities;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.nfc.tech.Ndef;
import android.nfc.tech.NfcF;
import android.nfc.tech.NfcV;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.chaquo.python.PyObject;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.utils.Global;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TouchHardwareActivity extends BaseActivity {
    private NfcAdapter nfcAdapter; // NFC manager
    // nfc foreground dispatch system
    private PendingIntent pendingIntent;
    public String[][] tenchlists;
    public IntentFilter[] filters;
    public final static String FROM = "org.haobtc.wallet.from";
    @BindView(R.id.img_back)
    ImageView imgBack;
    private String tag;

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
        initNFC();

    }

    private void initNFC() {
        // tag tech_list
        tenchlists = new String[][]{{Ndef.class.getName()}, {NfcV.class.getName()}, {NfcF.class.getName()}, {IsoDep.class.getName()}};

        // get default nfc Adapter
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {

            Log.d("h_bl", "设备不支持NFC！");
            Toast.makeText(this, "请在系统设置中先启用NFC功能！", Toast.LENGTH_SHORT).show();
            return;
        } else if (!nfcAdapter.isEnabled()) {
            Toast.makeText(this, "请在系统设置中先启用NFC功能！", Toast.LENGTH_SHORT).show();
            return;
        }
        // PendingIntent，the intent processing the coming tag
        pendingIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        try {
            // filters to filter the nice tag
            filters = new IntentFilter[]{new IntentFilter(NfcAdapter.ACTION_TECH_DISCOVERED, "*/*")};
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (nfcAdapter != null && nfcAdapter.isEnabled()) {
            // enable nfc discovery for the app
            System.out.println("为本App启用NFC感应");
            nfcAdapter.enableForegroundDispatch(this, pendingIntent, filters, tenchlists);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (nfcAdapter != null && nfcAdapter.isEnabled()) {
            // disable nfc discovery for the app
            nfcAdapter.disableForegroundDispatch(this);
            System.out.println("禁用本App的NFC感应");
        }
    }

    private void startNewPage() {//TODO:
        Intent intent = new Intent(this, PinSettingActivity.class);
        intent.putExtra(FROM, tag);
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
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            PyObject nfc = Global.py.getModule("python-trezor.src.trezorlib.transport.nfc");
            PyObject nfcHandler = nfc.get("NFCHandle");
            nfcHandler.put("device", tag);
            Log.i("tag", "tag in nfc");
            PyObject instance = nfcHandler.call();
            PyObject tagInPy = instance.get("device");
            Tag tag1 = Objects.requireNonNull(tagInPy).toJava(Tag.class);
            Log.i("assert", tag1.equals(tag) + "");
            IsoDep isoDep = IsoDep.get(tag);
            System.out.println(isoDep);
            instance.callAttr("open");
            System.out.println(isoDep.getMaxTransceiveLength());
            startNewPage();
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
