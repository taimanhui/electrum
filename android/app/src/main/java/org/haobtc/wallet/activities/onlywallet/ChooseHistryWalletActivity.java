package org.haobtc.wallet.activities.onlywallet;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import androidx.recyclerview.widget.RecyclerView;
import com.chaquo.python.PyObject;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.utils.Daemon;
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
//        ArrayList<String> strings = new ArrayList<>();
//        for (int i = 0; i < 10; i++) {
//            strings.add("钱包" + i);
//        }
        PyObject infoFromServer = null;
        try {
            infoFromServer = Daemon.commands.callAttr("get_wallet_info_from_server", histry_xpub);
            Log.i("infoFromServer", "infoFromServer: " + infoFromServer);
        } catch (Exception e) {
            e.printStackTrace();
            Log.i("infoFromServer", "Exception: "+e.getMessage());
        }
        if (infoFromServer != null) {
            String strfromServer = infoFromServer.toString();
            Log.i("infoFromServer", "initData: " + infoFromServer);
            if (strfromServer.length() != 2) {

            }
        }
//        ImportHistryWalletAdapter histryWalletAdapter = new ImportHistryWalletAdapter(ChooseHistryWalletActivity.this, strings);
//        reclImportWallet.setAdapter(histryWalletAdapter);
//        histryWalletAdapter.setOnItemClickListener(new ImportHistryWalletAdapter.OnItemClickListener() {
//            @Override
//            public void onItemClick(int position) {
//                mToast(position+"");
//            }
//        });

    }

    @OnClick({R.id.img_backCreat, R.id.btn_Finish})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_backCreat:
                finish();
                break;
            case R.id.btn_Finish:
//                try {
//                    Daemon.commands.callAttr("set_multi_wallet_info", name, 1, 1);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }

//                try {
//                    //add
//                    Daemon.commands.callAttr("add_xpub", strContent);
//                } catch (Exception e) {
//                    Toast.makeText(this, R.string.changeaddress, Toast.LENGTH_SHORT).show();
//                    e.printStackTrace();
//                    break;
//                }

//                try {
//                    Daemon.commands.callAttr("create_multi_wallet", walletNames);
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    return;
//                }

                break;
        }
    }
}
