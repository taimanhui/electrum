package org.haobtc.wallet.activities.personalwallet;

import android.app.AlertDialog;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chaquo.python.Kwarg;
import com.chaquo.python.PyObject;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.haobtc.wallet.MainActivity;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.adapter.ImportHistryWalletAdapter;
import org.haobtc.wallet.bean.ImportHistryWalletBean;
import org.haobtc.wallet.event.AddBixinKeyEvent;
import org.haobtc.wallet.event.FirstEvent;
import org.haobtc.wallet.utils.Daemon;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ChooseHistryWalletActivity extends BaseActivity {

    @BindView(R.id.img_backCreat)
    ImageView imgBackCreat;
    @BindView(R.id.recl_importWallet)
    RecyclerView reclImportWallet;
    @BindView(R.id.btn_Finish)
    Button btnFinish;
    private String histry_xpub;
    private ArrayList<AddBixinKeyEvent> xpubList;
    private boolean chooseWallet = false;
    private String keyaddress;
    private String walletType;

    @Override
    public int getLayoutId() {
        return R.layout.activity_choose_histry_wallet;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        Intent intent = getIntent();
        histry_xpub = intent.getStringExtra("histry_xpub");
        Log.i("histry_xpub", "initView: "+histry_xpub);

    }

    @Override
    public void initData() {
        xpubList = new ArrayList<>();
        //get histry wallet
        getHistryWallet();

    }

    private void getHistryWallet() {
        PyObject infoFromServer = null;
        try {
            infoFromServer = Daemon.commands.callAttr("get_wallet_info_from_server", histry_xpub);
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("infoFromServer", "Exception: "+e.getMessage());
        }
        if (infoFromServer != null) {
            String strfromServer = infoFromServer.toString();
            Log.i("infoFromServer", "initData: " + infoFromServer);
            if (strfromServer.length() != 2) {
                Gson gson = new Gson();
                ImportHistryWalletBean importHistryWalletBean = gson.fromJson(strfromServer, ImportHistryWalletBean.class);
                List<ImportHistryWalletBean.WalltesBean> walltes = importHistryWalletBean.getWalltes();
                for (int i = 0; i < walltes.size(); i++) {
                    String walletType = walltes.get(i).getWalletType();
                    String xpubs = walltes.get(i).getXpubs();
                    AddBixinKeyEvent addBixinKeyEvent = new AddBixinKeyEvent();
                    addBixinKeyEvent.setKeyname(walletType);
                    addBixinKeyEvent.setKeyaddress(xpubs);
                    xpubList.add(addBixinKeyEvent);
                }
                ImportHistryWalletAdapter histryWalletAdapter = new ImportHistryWalletAdapter(ChooseHistryWalletActivity.this, xpubList);
                reclImportWallet.setAdapter(histryWalletAdapter);
                histryWalletAdapter.setOnItemClickListener(new ImportHistryWalletAdapter.OnItemClickListener() {
                    @Override
                    public void onItemClick(int position) {
                        btnFinish.setBackground(getDrawable(R.drawable.little_radio_blue));
                        btnFinish.setEnabled(true);
                        chooseWallet = true;
                        keyaddress = xpubList.get(position).getKeyaddress();
                        walletType = xpubList.get(position).getKeyname();
                    }
                });
            }
        }else{
            mToast(getString(R.string.no_import_wallet));
        }
    }

    @OnClick({R.id.img_backCreat, R.id.btn_Finish})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_backCreat:
                finish();
                break;
            case R.id.btn_Finish:
                setWalletnameDialog();
                break;
        }
    }

    private void setWalletnameDialog() {
        if (chooseWallet){
            View view1 = LayoutInflater.from(ChooseHistryWalletActivity.this).inflate(R.layout.set_walletname, null, false);
            AlertDialog alertDialog = new AlertDialog.Builder(ChooseHistryWalletActivity.this).setView(view1).create();
            EditText walletName = view1.findViewById(R.id.inputName);
            view1.findViewById(R.id.btn_enter_wallet).setOnClickListener(v -> {
                String wallet_name = walletName.getText().toString();
                importWallet(wallet_name);

            });
            view1.findViewById(R.id.cancel_select_wallet).setOnClickListener(v -> {

                alertDialog.dismiss();
            });
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();
        }else{
            mToast(getString(R.string.please_import_wallet));
        }

    }

    private void importWallet(String wallet_name) {
        //signum
        String strsigNum=walletType.substring(0, walletType.indexOf("-"));
        int sigNum = Integer.parseInt(strsigNum);
        //public  num
        String strpubNum = walletType.substring(walletType.indexOf("-")+1);
        int pubNum = Integer.parseInt(strpubNum);

        try {
            Daemon.commands.callAttr("import_create_hw_wallet",wallet_name,sigNum,pubNum,keyaddress);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        mIntent(MainActivity.class);

    }
}
