package org.haobtc.wallet.activities.personalwallet;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.yzq.zxinglibrary.common.Constant;

import org.greenrobot.eventbus.EventBus;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.activities.service.CommunicationModeSelector;
import org.haobtc.wallet.adapter.AddBixinKeyAdapter;
import org.haobtc.wallet.aop.SingleClick;
import org.haobtc.wallet.event.AddBixinKeyEvent;
import org.haobtc.wallet.event.FirstEvent;
import org.haobtc.wallet.utils.Daemon;
import org.haobtc.wallet.utils.MyDialog;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static org.haobtc.wallet.activities.service.CommunicationModeSelector.xpub;


public class PersonalMultiSigWalletCreator extends BaseActivity {

    @BindView(R.id.img_backCreat)
    ImageView imgBackCreat;
    @BindView(R.id.tet_personalNum)
    TextView tetPersonalNum;
    @BindView(R.id.recl_BinxinKey)
    RecyclerView reclBinxinKey;
    @BindView(R.id.bn_add_key)
    LinearLayout bnAddKey;
    @BindView(R.id.bn_complete_add_cosigner)
    Button bnCompleteAddCosigner;
    private int sigNum;
    public static final String TAG = PersonalMultiSigWalletCreator.class.getSimpleName();
    private String walletNames;
    private int walletNameNum;
    private ArrayList<AddBixinKeyEvent> addEventsDatas;
    private SharedPreferences.Editor edit;
    private MyDialog myDialog;
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                ArrayList<String> pubList = new ArrayList<>();
                for (int i = 0; i < addEventsDatas.size(); i++) {
                    String keyaddress = addEventsDatas.get(i).getKeyaddress();
                    pubList.add("\"" + keyaddress + "\"");
                }
                try {
                    Daemon.commands.callAttr("import_create_hw_wallet", walletNames, 1, sigNum, pubList.toString());
                } catch (Exception e) {
                    e.printStackTrace();
                    myDialog.dismiss();
                    String message = e.getMessage();
                    if ("BaseException: file already exists at path".equals(message)) {
                        mToast(getString(R.string.changewalletname));
                    }else if (message.contains("The same xpubs have create wallet")){
                        String haveWalletName = message.substring(message.indexOf("name=")+5);
                        mToast(getString(R.string.xpub_have_wallet) + haveWalletName);

                    }
                    return;
                }
                edit.putInt("defaultName", walletNameNum);
                edit.apply();
                myDialog.dismiss();
                EventBus.getDefault().post(new FirstEvent("11"));
                Intent intent = new Intent(PersonalMultiSigWalletCreator.this, CreatFinishPersonalActivity.class);
                intent.putExtra("walletNames", walletNames);
                intent.putExtra("flagTag", "onlyChoose");
                intent.putExtra("strBixinlist", (Serializable) addEventsDatas);
                startActivity(intent);
                finish();
            }
        }
    };
    private SharedPreferences preferences;
    private int defaultKeyNameNum;

    @Override
    public int getLayoutId() {
        return R.layout.activity_create_only_choose;
    }

    @SuppressLint("CommitPrefEdits")
    @Override
    public void initView() {
        ButterKnife.bind(this);
        preferences = getSharedPreferences("Preferences", MODE_PRIVATE);
        edit = preferences.edit();
        myDialog = MyDialog.showDialog(this);
        Intent intent = getIntent();
        sigNum = intent.getIntExtra("sigNum", 0);
        walletNames = intent.getStringExtra("walletNames");
        walletNameNum = intent.getIntExtra("walletNameNum", 0);
        init();

    }

    @SuppressLint("DefaultLocale")
    private void init() {
        tetPersonalNum.setText(String.format("%s(0/%d)", getString(R.string.creat_personal), sigNum));
    }

    @Override
    public void initData() {
        addEventsDatas = new ArrayList<>();
    }

    @SingleClick
    @OnClick({R.id.img_backCreat, R.id.bn_add_key, R.id.bn_complete_add_cosigner})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_backCreat:
                finish();
                break;
            case R.id.bn_add_key:
                // new version code
                CommunicationModeSelector.runnables.clear();
                CommunicationModeSelector.runnables.add(null);
                CommunicationModeSelector.runnables.add(runnable2);
                Intent intent1 = new Intent(this, CommunicationModeSelector.class);
                intent1.putExtra("tag", TAG);
                startActivity(intent1);
                break;
            case R.id.bn_complete_add_cosigner:
                handler.sendEmptyMessage(1);
                break;
        }
    }

    private Runnable runnable2 = () -> showConfirmPubDialog(this, R.layout.bixinkey_confirm, xpub);


    private void showConfirmPubDialog(Context context, @LayoutRes int resource, String xpub) {
        //set see view
        View view = View.inflate(context, resource, null);
        Dialog dialogBtoms = new Dialog(context, R.style.dialog);

        EditText edit_bixinName = view.findViewById(R.id.edit_keyName);
        TextView tet_Num = view.findViewById(R.id.txt_textNum);
        TextView textView = view.findViewById(R.id.text_public_key_cosigner_popup);
        textView.setText(xpub);
        int defaultKeyNum = preferences.getInt("defaultKeyNum", 0);
        defaultKeyNameNum = defaultKeyNum + 1;
        edit_bixinName.setText(String.format("pub%s", String.valueOf(defaultKeyNameNum)));
        edit_bixinName.addTextChangedListener(new TextWatcher() {
            CharSequence input;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                input = s;
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tet_Num.setText(String.format(Locale.CHINA, "%d/20", input.length()));
                if (input.length() > 19) {
                    Toast.makeText(PersonalMultiSigWalletCreator.this, R.string.moreinput_text, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        view.findViewById(R.id.btn_confirm).setOnClickListener(v -> {
            String strBixinname = edit_bixinName.getText().toString();
            String strSweep = textView.getText().toString();
            if (TextUtils.isEmpty(strBixinname)) {
                mToast(getString(R.string.input_name));
                return;
            }
            if (TextUtils.isEmpty(strSweep)) {
                mToast(getString(R.string.input_public_address));
                return;
            }
            AddBixinKeyEvent addBixinKeyEvent = new AddBixinKeyEvent();
            addBixinKeyEvent.setKeyname(strBixinname);
            addBixinKeyEvent.setKeyaddress(strSweep);
            addEventsDatas.add(addBixinKeyEvent);
            //bixinKEY
            AddBixinKeyAdapter addBixinKeyAdapter = new AddBixinKeyAdapter(addEventsDatas);
            reclBinxinKey.setAdapter(addBixinKeyAdapter);
            tetPersonalNum.setText(String.format(getString(R.string.creat_personal) + "(%d/%d)", addEventsDatas.size(), sigNum));

            if (addEventsDatas.size() == sigNum) {
                bnCompleteAddCosigner.setEnabled(true);
                bnCompleteAddCosigner.setBackground(getDrawable(R.drawable.little_radio_blue));
                bnAddKey.setVisibility(View.GONE);
            }
            edit.putInt("defaultKeyNum",defaultKeyNameNum);
            edit.apply();
            addBixinKeyAdapter.setOnItemChildClickListener(new BaseQuickAdapter.OnItemChildClickListener() {
                @Override
                public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
                    if (view.getId() == R.id.img_deleteKey) {
                        try {
                            Daemon.commands.callAttr("delete_xpub", strSweep);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        addEventsDatas.remove(position);
                        addBixinKeyAdapter.notifyDataSetChanged();
                        bnAddKey.setVisibility(View.VISIBLE);
                        tetPersonalNum.setText(String.format(getString(R.string.creat_personal) + "(%d/%d)", addEventsDatas.size(), sigNum));
                        bnCompleteAddCosigner.setBackground(getDrawable(R.drawable.little_radio_qian));
                        bnCompleteAddCosigner.setEnabled(false);
                    }
                }
            });
            dialogBtoms.cancel();
            // todo:关闭弹窗

        });

        //cancel dialog
        view.findViewById(R.id.img_cancle).setOnClickListener(v -> {
            dialogBtoms.cancel();
        });


        dialogBtoms.setContentView(view);
        Window window = dialogBtoms.getWindow();
        //set pop_up size
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        //set locate
        window.setGravity(Gravity.BOTTOM);
        //set animal
        window.setWindowAnimations(R.style.AnimBottom);
        dialogBtoms.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 0 && resultCode == RESULT_OK) {
            if (data != null) {
                String content = data.getStringExtra(Constant.CODED_CONTENT);
            }
        }
    }
}
