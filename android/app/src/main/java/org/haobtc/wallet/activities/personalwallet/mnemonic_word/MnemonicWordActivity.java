package org.haobtc.wallet.activities.personalwallet.mnemonic_word;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.recyclerview.widget.RecyclerView;

import com.chaquo.python.PyObject;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.adapter.ChoosePayAddressAdapetr;
import org.haobtc.wallet.bean.AddressEvent;
import org.haobtc.wallet.utils.Daemon;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MnemonicWordActivity extends BaseActivity {


    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.tet_keyName)
    TextView tetKeyName;
    @BindView(R.id.lin_Bitype)
    LinearLayout linBitype;
    @BindView(R.id.edit_one)
    EditText editOne;
    @BindView(R.id.edit_two)
    EditText editTwo;
    @BindView(R.id.edit_three)
    EditText editThree;
    @BindView(R.id.edit_four)
    EditText editFour;
    @BindView(R.id.edit_five)
    EditText editFive;
    @BindView(R.id.edit_six)
    EditText editSix;
    @BindView(R.id.edit_seven)
    EditText editSeven;
    @BindView(R.id.edit_eight)
    EditText editEight;
    @BindView(R.id.edit_nine)
    EditText editNine;
    @BindView(R.id.edit_ten)
    EditText editTen;
    @BindView(R.id.edit_eleven)
    EditText editEleven;
    @BindView(R.id.edit_twelve)
    EditText editTwelve;
    @BindView(R.id.btn_setPin)
    Button btnSetPin;
    private ArrayList<AddressEvent> dataListName;
    private ChoosePayAddressAdapetr choosePayAddressAdapetr;
    private String wallet_name;
    private String newWallet_type = "";
    private SharedPreferences preferences;

    @Override
    public int getLayoutId() {
        return R.layout.activity_mnemonic_word;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        preferences = getSharedPreferences("Preferences", MODE_PRIVATE);

    }

    @Override
    public void initData() {
        dataListName = new ArrayList<>();
        payAddressMore();

    }

    private void payAddressMore() {
        dataListName.add(new AddressEvent("bitpie"));
        dataListName.add(new AddressEvent("cobo"));
        dataListName.add(new AddressEvent("trezor"));
        dataListName.add(new AddressEvent("ledger"));
        dataListName.add(new AddressEvent("electrum"));
    }

    @OnClick({R.id.img_back, R.id.lin_Bitype, R.id.btn_setPin})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.lin_Bitype:
                if (dataListName == null || dataListName.size() == 0) {
                    mToast(getString(R.string.none_wallet));
                    return;
                }
                showDialogs(MnemonicWordActivity.this, R.layout.selectwallet_item);
                break;
            case R.id.btn_setPin:
                String strone = editOne.getText().toString();
                String strtwo = editTwo.getText().toString();
                String strthree = editThree.getText().toString();
                String strfour = editFour.getText().toString();
                String strfive = editFive.getText().toString();
                String strsix = editSix.getText().toString();
                String strseven = editSeven.getText().toString();
                String streight = editEight.getText().toString();
                String strnine = editNine.getText().toString();
                String strten = editTen.getText().toString();
                String streleven = editEleven.getText().toString();
                String strtwelve = editTwelve.getText().toString();
                Log.i("newWallet_name", "newWallet_name: " + newWallet_type);
                if (dataListName == null || dataListName.size() == 0) {
                    mToast(getString(R.string.none_wallet));
                    return;
                }
                if (TextUtils.isEmpty(newWallet_type)) {
                    mToast(getString(R.string.please_selectwallet));
                    return;
                }
                if ((TextUtils.isEmpty(strone) || TextUtils.isEmpty(strtwo) || TextUtils.isEmpty(strthree) || TextUtils.isEmpty(strfour))
                        || TextUtils.isEmpty(strfive) || TextUtils.isEmpty(strsix) || TextUtils.isEmpty(strseven) || TextUtils.isEmpty(streight)
                        || TextUtils.isEmpty(strnine) || TextUtils.isEmpty(strten) || TextUtils.isEmpty(streleven) || TextUtils.isEmpty(strtwelve)) {
                    mToast(getString(R.string._12_help_word));
                    return;
                }
                String strNewseed = strone + " " + strtwo + " " + strthree + " " + strfour + " " + strfive + " " + strsix + " " + strseven + " " + streight + " " + strnine + " " + strten + " " + streleven + " " + strtwelve;
                judgeSeedorrectC(strNewseed);
                break;

        }
    }

    private void judgeSeedorrectC(String newSeed) {
        PyObject is_seed = null;
        try {
            is_seed = Daemon.commands.callAttr("is_seed", newSeed);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (is_seed!=null){
            boolean isSeed = is_seed.toBoolean();
            if (isSeed){
                Intent intent = new Intent(MnemonicWordActivity.this, CreateHelpWordWalletActivity.class);
                intent.putExtra("newWallet_type", newWallet_type);
                intent.putExtra("strNewseed",newSeed);
                startActivity(intent);
            }else{
                mToast(getString(R.string.helpword_wrong));
            }
        }
    }

    private void showDialogs(Context context, @LayoutRes int resource) {
        //set see view
        View view = View.inflate(context, resource, null);
        Dialog dialogBtom = new Dialog(context, R.style.dialog);
        //cancel dialog
        view.findViewById(R.id.cancel_select_wallet).setOnClickListener(v -> {
            dialogBtom.cancel();
        });
        view.findViewById(R.id.bn_select_wallet).setOnClickListener(v -> {
            newWallet_type = wallet_name;
            tetKeyName.setText(newWallet_type);
            dialogBtom.cancel();

        });
        RecyclerView recyPayaddress = view.findViewById(R.id.recy_payAdress);

//        recyPayaddress.setLayoutManager(new LinearLayoutManager(SendOne2OneMainPageActivity.this));
        choosePayAddressAdapetr = new ChoosePayAddressAdapetr(MnemonicWordActivity.this, dataListName);
        recyPayaddress.setAdapter(choosePayAddressAdapetr);
        recyclerviewOnclick();


        dialogBtom.setContentView(view);
        Window window = dialogBtom.getWindow();
        //set pop_up size
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        //set locate
        window.setGravity(Gravity.BOTTOM);
        //set animal
        window.setWindowAnimations(R.style.AnimBottom);
        dialogBtom.show();
    }

    private void recyclerviewOnclick() {
        choosePayAddressAdapetr.setmOnItemClickListener(new ChoosePayAddressAdapetr.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                wallet_name = dataListName.get(position).getName();
            }
        });
    }

}
