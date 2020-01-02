package org.haobtc.wallet.activities;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.tbruyelle.rxpermissions2.RxPermissions;
import com.yzq.zxinglibrary.android.CaptureActivity;
import com.yzq.zxinglibrary.common.Constant;

import org.haobtc.wallet.R;
import org.haobtc.wallet.adapter.CosignerAdapter;
import org.haobtc.wallet.utils.CommonUtils;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.utils.Daemon;
import org.haobtc.wallet.utils.MyDialog;

import java.util.ArrayList;
import java.util.Locale;

public class CoSignerAddActivity extends BaseActivity implements View.OnClickListener {
    private static final int REQUEST_CODE = 0;
    private Button buttonComplete, buttonSweep, buttonPaste;
    private LinearLayout buttonAdd;
    private RecyclerView recyclerView;
    private PopupWindow popupWindow;
    private View rootView;
    EditText editTextPublicKey;
    private Intent intent;
    private int addNum = 0;
    private RxPermissions rxPermissions;
    private int cosignerNum;
    private ArrayList<String> listData;
    private ArrayList<Integer> nameNums;
    private String strContent;
    private CosignerAdapter cosignerAdapter;
    public static final String WALLET_NAME = "org.haobtc.wallet.activities.walletName";
    private String nameExtra;
    private ImageView imgBack;
    private MyDialog myDialog;


    @Override
    public int getLayoutId() {
        return R.layout.add_cosigner;
    }

    @Override
    public void initView() {
        intent = getIntent();
        buttonAdd = findViewById(R.id.bn_add_cosigner);
        buttonComplete = findViewById(R.id.bn_complete_add_cosigner);
        imgBack = findViewById(R.id.img_back);
        cosignerNum = intent.getIntExtra(CreateWalletPageActivity.COSIGNER_NUM, 1);
        nameExtra = intent.getStringExtra(WALLET_NAME);
        buttonComplete.setText(String.format(Locale.CHINA, getResources().getString(R.string.finish) + "（%d-%d)", addNum, cosignerNum));
        buttonComplete.setEnabled(false);
        buttonComplete.setBackground(getResources().getDrawable(R.drawable.little_radio_qian));
        buttonAdd.setOnClickListener(this);
        buttonComplete.setOnClickListener(this);
        imgBack.setOnClickListener(this);
        recyclerView = findViewById(R.id.recycler_add_cosigner);
        rxPermissions = new RxPermissions(this);
    }

    @Override
    public void initData() {
        myDialog = MyDialog.showDialog(CoSignerAddActivity.this);
        nameNums = new ArrayList<>();
        listData = new ArrayList<>();
    }

    private void showPopupAddCosigner() {
        TextView buttonUseHardware;
        ImageView imageViewCancel;
        View view = LayoutInflater.from(this).inflate(R.layout.add_cosiger_popwindow, null);
        buttonUseHardware = view.findViewById(R.id.bn_use_hardware_add_cosigner_popup);
        buttonSweep = view.findViewById(R.id.sweep_cosigner_popup);
        buttonPaste = view.findViewById(R.id.paste_cosigner_popup);
        imageViewCancel = view.findViewById(R.id.cancel_cosigner_add_popup);
        editTextPublicKey = view.findViewById(R.id.edit_public_key_cosigner_popup);
        editTextPublicKey.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() != 0) {
                    buttonSweep.setText(R.string.clear);
                    buttonSweep.setTextColor(getResources().getColor(android.R.color.white));
                    buttonSweep.setBackground(getResources().getDrawable(R.drawable.button_bk_small_grey_big_radius));
                    buttonSweep.setCompoundDrawables(null, null, null, null);
                    buttonSweep.setGravity(Gravity.CENTER);

                    buttonPaste.setText(R.string.confirm);
                    buttonPaste.setTextColor(getResources().getColor(android.R.color.white));
                    buttonPaste.setBackground(getResources().getDrawable(R.drawable.button_bk_small_big_radius));
                    buttonPaste.setCompoundDrawables(null, null, null, null);
                    buttonPaste.setGravity(Gravity.CENTER);

                } else {
                    buttonSweep.setText(R.string.sweep);
                    buttonSweep.setTextColor(getResources().getColor(R.color.button_bk));
                    Drawable sweepIcon = getResources().getDrawable(R.drawable.saoyisao);
                    sweepIcon.setBounds(0, 0, sweepIcon.getMinimumWidth(), sweepIcon.getMinimumHeight());
                    buttonSweep.setCompoundDrawables(sweepIcon, null, null, null);
                    buttonSweep.setBackground(new ColorDrawable(getResources().getColor(R.color.paste_bk)));
                    buttonSweep.setGravity(Gravity.CENTER);

                    buttonPaste.setText(R.string.paste);
                    buttonPaste.setTextColor(getResources().getColor(R.color.button_bk));
                    Drawable pasteIcon = getResources().getDrawable(R.drawable.zhantie);
                    pasteIcon.setBounds(0, 0, pasteIcon.getMinimumWidth(), pasteIcon.getMinimumHeight());
                    buttonPaste.setCompoundDrawables(pasteIcon, null, null, null);
                    buttonPaste.setBackground(new ColorDrawable(getResources().getColor(R.color.paste_bk)));
                    buttonPaste.setGravity(Gravity.CENTER);
                }

            }
        });
        popupWindow = new PopupWindow();
        popupWindow.setContentView(view);
        popupWindow.setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        popupWindow.setHeight(ViewGroup.LayoutParams.WRAP_CONTENT);
        popupWindow.setBackgroundDrawable(new BitmapDrawable(null, ""));
        rootView = LayoutInflater.from(this).inflate(R.layout.add_cosigner, null);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setFocusable(true);
        popupWindow.setOnDismissListener(() -> {
            setBackgroundAlpha(1f);
        });
        popupWindow.showAtLocation(rootView, Gravity.BOTTOM, 0, 0);
        buttonUseHardware.setOnClickListener(this);
        buttonSweep.setOnClickListener(this);
        buttonPaste.setOnClickListener(this);
        imageViewCancel.setOnClickListener(this);

    }

    public void setBackgroundAlpha(float bgAlpha) {
        WindowManager.LayoutParams lp = getWindow()
                .getAttributes();
        lp.alpha = bgAlpha;
        getWindow().setAttributes(lp);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bn_add_cosigner:
                showPopupAddCosigner();
                setBackgroundAlpha(0.5f);
                break;

            case R.id.bn_complete_add_cosigner:
                myDialog.show();
                try {
                    Daemon.commands.callAttr("create_multi_wallet", nameExtra);
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.i("JXM", "onClick: " + e.getMessage());
                    return;
                }
                myDialog.dismiss();
                Intent intent = new Intent(this, CreateWalletSuccessfulActivity.class);
                // intent.putExtra();
                startActivity(intent);

                break;
            case R.id.bn_use_hardware_add_cosigner_popup:
                Intent intent1 = new Intent(this, TouchHardwareActivity.class);
                startActivity(intent1);
                break;
            case R.id.sweep_cosigner_popup:
                String bnText = buttonSweep.getText().toString();
                if ("Clear".equals(bnText) || bnText.equals(getResources().getString(R.string.clear))) {
                    editTextPublicKey.setText("");
                } else {
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
                }
                break;
            case R.id.paste_cosigner_popup:
                String bnText1 = buttonPaste.getText().toString();
                if ("Confirm".equals(bnText1) || bnText1.equals(getResources().getString(R.string.confirm))) {
                    strContent = editTextPublicKey.getText().toString();

                    try {
                        //add
                        Daemon.commands.callAttr("add_xpub", strContent);

                    } catch (Exception e) {
                        Toast.makeText(this, R.string.changeaddress, Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                        break;
                    }

                    addNum = addNum + 1;
                    nameNums.add(addNum);
                    listData.add(strContent);
                    cosignerAdapter = new CosignerAdapter(CoSignerAddActivity.this, listData, nameNums);
                    recyclerView.setAdapter(cosignerAdapter);
                    buttonComplete.setText(String.format(Locale.CHINA, getResources().getString(R.string.finish) + "（%d-%d)", addNum, cosignerNum));
                    if (addNum == cosignerNum) {
                        buttonComplete.setEnabled(true);
                        buttonComplete.setBackground(getResources().getDrawable(R.drawable.little_radio_blue));
                        buttonAdd.setVisibility(View.GONE);
                    }

                    //delete
                    cosignerAdapter.setOnItemDeteleLisoner(new CosignerAdapter.onItemDeteleLisoner() {
                        @Override
                        public void onClick(int pos) {
                            try {
                                Daemon.commands.callAttr("delete_xpub", strContent);

                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            nameNums.remove(pos);
                            listData.remove(pos);
                            cosignerAdapter.notifyDataSetChanged();
                            addNum = addNum - 1;
                            buttonAdd.setVisibility(View.VISIBLE);
                            buttonComplete.setText(String.format(Locale.CHINA, getResources().getString(R.string.finish) + "（%d-%d)", addNum, cosignerNum));
                            buttonComplete.setBackground(getResources().getDrawable(R.drawable.little_radio_qian));
                            buttonComplete.setEnabled(false);

                        }
                    });

                    //todo:
                    popupWindow.dismiss();

                } else {
                    //get Shear plate TODO：
                    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                    if (clipboard != null) {
                        ClipData data = clipboard.getPrimaryClip();
                        if (data != null && data.getItemCount() > 0) {
                            editTextPublicKey.setText(data.getItemAt(0).getText());
                        }
                    }
                }
                break;
            case R.id.cancel_cosigner_add_popup:
                popupWindow.dismiss();
                break;
            case R.id.img_back:
                finish();
                break;

            default:
                popupWindow.dismiss();

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Scan QR code / barcode return
        if (requestCode == 0 && resultCode == RESULT_OK) {
            if (data != null) {
                String content = data.getStringExtra(Constant.CODED_CONTENT);
                editTextPublicKey.setText(content);
            }
        }
    }
}
