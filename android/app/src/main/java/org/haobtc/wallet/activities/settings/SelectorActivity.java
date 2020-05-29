package org.haobtc.wallet.activities.settings;

import android.content.SharedPreferences;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;

import androidx.cardview.widget.CardView;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

//
// Created by liyan on 2020/5/28.
//
public class SelectorActivity extends BaseActivity {
    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.nfc)
    CheckBox nfc;
    @BindView(R.id.nfc_item)
    CardView nfcItem;
    @BindView(R.id.ble)
    CheckBox ble;
    @BindView(R.id.ble_item)
    CardView bleItem;
    @BindView(R.id.usb)
    CheckBox usb;
    @BindView(R.id.usb_item)
    CardView usbItem;
    SharedPreferences preferences;

    @Override
    public int getLayoutId() {
        return R.layout.communication_way;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);

    }

    @Override
    public void initData() {
        preferences = getSharedPreferences("Preferences", MODE_PRIVATE);
        String way = preferences.getString("way", "nfc");
        switch (way) {
            case "nfc":
                nfc.setChecked(true);
                ble.setChecked(false);
                usb.setChecked(false);
                break;
            case "ble":
                nfc.setChecked(false);
                ble.setChecked(true);
                usb.setChecked(false);
                break;
            case "usb":
                nfc.setChecked(false);
                ble.setChecked(false);
                usb.setChecked(true);
                break;
        }
    }


    @OnClick({R.id.nfc, R.id.nfc_item, R.id.ble, R.id.ble_item, R.id.usb, R.id.usb_item, R.id.img_back})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.nfc:
            case R.id.nfc_item:
                nfc.setChecked(true);
                ble.setChecked(false);
                usb.setChecked(false);
                break;
            case R.id.ble:
            case R.id.ble_item:
                nfc.setChecked(false);
                ble.setChecked(true);
                usb.setChecked(false);
                break;
            case R.id.usb:
            case R.id.usb_item:
                nfc.setChecked(false);
                ble.setChecked(false);
                usb.setChecked(true);
                break;
            case R.id.img_back:
                saveSetting();
                finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        saveSetting();
    }

    private void saveSetting() {
        if (nfc.isChecked()) {
            preferences.edit().putString("way", "nfc").apply();
        } else if (ble.isChecked()) {
            preferences.edit().putString("way", "ble").apply();
        } else if (usb.isChecked()) {
            preferences.edit().putString("way", "usb").apply();
        }
    }
}
