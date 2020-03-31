package org.haobtc.wallet.activities;

import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.chaquo.python.PyObject;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.activities.settings.BlockChooseActivity;
import org.haobtc.wallet.activities.settings.ElectrumNodeChooseActivity;
import org.haobtc.wallet.activities.settings.QuotationServerActivity;
import org.haobtc.wallet.utils.Daemon;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ServerSettingActivity extends BaseActivity {

    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.switch_cynchronez)
    Switch switchCynchronez;
    @BindView(R.id.rel_quotationChoose)
    RelativeLayout relQuotationChoose;
    @BindView(R.id.rel_blockChoose)
    RelativeLayout relBlockChoose;
    @BindView(R.id.rel_Electrum_Choose)
    RelativeLayout relElectrumChoose;
    @BindView(R.id.tet_defaultServer)
    TextView tetDefaultServer;
    private SharedPreferences.Editor edit;
    private SharedPreferences preferences;

    public int getLayoutId() {
        return R.layout.server_setting;
    }

    public void initView() {
        ButterKnife.bind(this);
        preferences = getSharedPreferences("Preferences", MODE_PRIVATE);
        edit = preferences.edit();
        inits();

    }

    private void inits() {
        boolean set_syn_server = preferences.getBoolean("set_syn_server", false);
        if (set_syn_server){
            switchCynchronez.setChecked(true);
        }else{
            switchCynchronez.setChecked(false);
        }
    }

    @Override
    public void initData() {
        switchCyn();
        getdefaultServer();

    }

    private void switchCyn() {
        switchCynchronez.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    try {
                        Daemon.commands.callAttr("set_syn_server",true);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    edit.putBoolean("set_syn_server",true);
                    edit.apply();
                    mToast(getString(R.string.set_success));
                }else{
                    try {
                        Daemon.commands.callAttr("set_syn_server",false);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    edit.putBoolean("set_syn_server",false);
                    edit.apply();
                }
            }
        });
    }

    private void getdefaultServer() {
        PyObject get_exchanges = null;
        try {
            get_exchanges = Daemon.commands.callAttr("get_exchanges");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (get_exchanges != null) {
            Log.i("get_exchanges", "getExchangelist: " + get_exchanges);
            List<PyObject> pyObjects = get_exchanges.asList();
            String defalutServer = pyObjects.get(0).toString();
            tetDefaultServer.setText(defalutServer);
        }
    }

    @OnClick({R.id.img_back, R.id.rel_quotationChoose, R.id.rel_blockChoose, R.id.rel_Electrum_Choose})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.rel_quotationChoose:
                mIntent(QuotationServerActivity.class);
                break;
            case R.id.rel_blockChoose:
                mIntent(BlockChooseActivity.class);
                break;
            case R.id.rel_Electrum_Choose:
                mIntent(ElectrumNodeChooseActivity.class);
                break;
        }
    }

}
