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
import org.haobtc.wallet.utils.NfcUtils;

import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TouchHardwareActivity extends BaseActivity {
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
        NfcUtils.nfc(this);

    }
    @Override
    protected void onResume() {
        super.onResume();
        if (NfcUtils.mNfcAdapter != null && NfcUtils.mNfcAdapter.isEnabled()) {
            // enable nfc discovery for the app
            System.out.println("为本App启用NFC感应");
            NfcUtils.mNfcAdapter.enableForegroundDispatch(this, NfcUtils.mPendingIntent, NfcUtils.mIntentFilter, NfcUtils.mTechList);
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
            PyObject nfc = Global.py.getModule("trezorlib.transport.nfc");
            PyObject nfcHandler = nfc.get("NFCHandle");
            nfcHandler.put("device", tag);
            PyObject trezor = Global.py.getModule("trezorlib.client");
            trezor.callAttr("ping1","nfc");
/*            Log.i("tag", "tag in nfc");
            PyObject instance = nfcHandler.call();
            PyObject tagInPy = instance.get("device");
            Tag tag1 = Objects.requireNonNull(tagInPy).toJava(Tag.class);
            Log.i("assert", tag1.equals(tag) + "");
            IsoDep isoDep = IsoDep.get(tag);
            System.out.println(isoDep);
            instance.callAttr("open");
            System.out.println(isoDep.getMaxTransceiveLength());*/

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
