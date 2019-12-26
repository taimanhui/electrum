package org.haobtc.wallet.activities;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.gyf.immersionbar.ImmersionBar;
import com.tbruyelle.rxpermissions2.RxPermissions;
import com.yzq.zxinglibrary.android.CaptureActivity;
import com.yzq.zxinglibrary.common.Constant;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.adapter.SendmoreAddressAdapter;
import org.haobtc.wallet.event.SendMoreAddressEvent;
import org.haobtc.wallet.utils.CommonUtils;

import java.util.ArrayList;

public class Send2ManyActivity extends BaseActivity implements View.OnClickListener {
    public static final String TOTAL_AMOUNT = "org.haobtc.wallet.activities.Send2ManyActivity.TOTAL";
    public static final String ADDRESS = "org.haobtc.activities.Send2ManyActivity.ADDRESS";
    private Button buttonSweep, buttonPaste, buttonNext;
    private EditText editTextAddress, editTextAmount;
    private LinearLayout buttonAdd;
    private TextView textViewTotal;
    private RecyclerView recyclerView;
    private static final int REQUEST_CODE = 0;
    private RxPermissions rxPermissions;
    private String straddress;
    private String strSmount;
    private ArrayList<SendMoreAddressEvent> sendMoreAddressList;
    private String wallet_name;
    private int totalAmount = 0;

    public int getLayoutId() {
        return R.layout.send_to_many;
    }

    public void initView() {
        CommonUtils.enableToolBar(this, R.string.send2many);
        rxPermissions = new RxPermissions(this);
        buttonAdd = findViewById(R.id.lin_add_to);
        buttonSweep = findViewById(R.id.bn_sweep_2many);
        buttonPaste = findViewById(R.id.bn_paste_2many);
        buttonNext = findViewById(R.id.bn_send2many_next);
        editTextAddress = findViewById(R.id.edit_address_2many);
        editTextAmount = findViewById(R.id.edit_amount_2many);
        textViewTotal = findViewById(R.id.total);
        recyclerView = findViewById(R.id.recycler_add_to);
        buttonAdd.setOnClickListener(this);
        buttonSweep.setOnClickListener(this);
        buttonPaste.setOnClickListener(this);
        buttonNext.setOnClickListener(this);
        buttonNext.setEnabled(false);
        buttonNext.setBackground(getResources().getDrawable(R.color.button_bk_grey));
        init();
        // recyclerView.setAdapter();
    }

    private void init() {
        Intent intent = getIntent();
        wallet_name = intent.getStringExtra("wallet_name");
    }

    @Override
    public void initData() {
        sendMoreAddressList = new ArrayList<>();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.lin_add_to:
                addressList();

                break;
            case R.id.bn_sweep_2many:
                rxPermissions
                        .request(Manifest.permission.CAMERA)
                        .subscribe(granted -> {
                            if (granted) { // Always true pre-M
                                //If you have already authorized it, you can directly jump to the QR code scanning interface
                                Intent intent2 = new Intent(this, CaptureActivity.class);
                                startActivityForResult(intent2, REQUEST_CODE);
                            } else { // Oups permission denied
                                Toast.makeText(this, R.string.photopersion, Toast.LENGTH_SHORT).show();
                            }
                        }).dispose();
                break;
            case R.id.bn_paste_2many:
                ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                if (clipboard != null) {
                    ClipData data = clipboard.getPrimaryClip();
                    if (data != null && data.getItemCount() > 0) {
                        editTextAddress.setText(data.getItemAt(0).getText());
                    }
                }
                break;
            case R.id.bn_send2many_next:
                int size = sendMoreAddressList.size();
                if (size == 0) {
                    mToast(getResources().getString(R.string.pleas_add_outputAdrs));
                }else{
                    Intent intent = new Intent(this, SendOne2ManyMainPageActivity.class);
                    intent.putExtra(TOTAL_AMOUNT, textViewTotal.getText().toString());
                    intent.putExtra(ADDRESS, "");
                    intent.putExtra("wallet_name", wallet_name);
                    intent.putExtra("addressNum",size);
                    intent.putExtra("totalAmount",totalAmount);
                    startActivity(intent);
                }

                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Scan QR code / barcode return
        if (requestCode == 0 && resultCode == RESULT_OK) {
            if (data != null) {
                String content = data.getStringExtra(Constant.CODED_CONTENT);
                Log.i("content", "on------: " + content);
                if (!TextUtils.isEmpty(content)) {
                    if (content.contains("bitcoin:")) {
                        String replace = content.replaceAll("bitcoin:", "");
                        editTextAddress.setText(replace);
                    } else {
                        editTextAddress.setText(content);
                    }
                }
            }
        }
    }

    private void addressList() {
        straddress = editTextAddress.getText().toString();
        strSmount = editTextAmount.getText().toString();
        if (TextUtils.isEmpty(straddress)) {
            mToast(getResources().getString(R.string.please_input_address));
            return;
        }
        if (TextUtils.isEmpty(strSmount)) {
            mToast(getResources().getString(R.string.please_input_amount));
            return;
        }
        int sAmount = Integer.parseInt(strSmount);
        //Total transfer quantity
        totalAmount = totalAmount + sAmount;
        textViewTotal.setText(String.format("%d",totalAmount));
        SendMoreAddressEvent sendMoreAddressEvent = new SendMoreAddressEvent();
        sendMoreAddressEvent.setInputAddress(straddress);
        sendMoreAddressEvent.setInputAmount(strSmount);
        sendMoreAddressList.add(sendMoreAddressEvent);
        editTextAddress.setText("");
        editTextAmount.setText("");
        buttonNext.setEnabled(true);
        buttonNext.setBackground(getResources().getDrawable(R.color.button_bk));
        SendmoreAddressAdapter sendmoreAddressAdapter = new SendmoreAddressAdapter(sendMoreAddressList);
        recyclerView.setAdapter(sendmoreAddressAdapter);

    }
}
