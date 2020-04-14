package org.haobtc.wallet.activities.personalwallet;

import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.haobtc.wallet.MainActivity;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.ReceivedPageActivity;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.adapter.HelpWordAdapter;
import org.haobtc.wallet.event.FirstEvent;
import org.haobtc.wallet.utils.MyDialog;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MnemonicActivity extends BaseActivity {


    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.recl_helpWord)
    RecyclerView reclHelpWord;
    @BindView(R.id.btn_setPin)
    Button btnSetPin;
    @BindView(R.id.tet_jump)
    TextView tetJump;
    @BindView(R.id.text_all_word)
    TextView textAllWord;
    @BindView(R.id.text_copy_mnemonic)
    TextView textCopyMnemonic;
    private String strSeed;
    private String strPass1;
    private String strName;
    private MyDialog myDialog;

    @Override
    public int getLayoutId() {
        return R.layout.activity_remeber_mnemonic_word;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        myDialog = MyDialog.showDialog(this);
        Intent intent = getIntent();
        strPass1 = intent.getStringExtra("strPass1");
        strName = intent.getStringExtra("strName");

    }

    @Override
    public void initData() {
        myDialog.show();
    }

    @OnClick({R.id.img_back, R.id.btn_setPin, R.id.tet_jump,R.id.text_copy_mnemonic})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.btn_setPin:
                Intent intent1 = new Intent(MnemonicActivity.this, ImportMnemonicActivity.class);
                intent1.putExtra("strSeeds", strSeed);
                intent1.putExtra("strPass1", strPass1);
                intent1.putExtra("strName", strName);
                startActivity(intent1);
                finish();
                break;
            case R.id.tet_jump:
                Intent intent = new Intent(this, MainActivity.class);
                intent.putExtra("strName", strName);
                startActivity(intent);
                finishAffinity();
                break;
            case R.id.text_copy_mnemonic:
                //copy text
                ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                // The text is placed on the system clipboard.
                cm.setText(textAllWord.getText());
                Toast.makeText(MnemonicActivity.this, R.string.copysuccess, Toast.LENGTH_LONG).show();

                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void event(FirstEvent updataHint) {
        strSeed = updataHint.getMsg();
        textCopyMnemonic.setVisibility(View.VISIBLE);
        textAllWord.setText(strSeed);
        if (!TextUtils.isEmpty(strSeed)) {
            String[] wordsList = strSeed.split(" ");
            ArrayList<String> strings = new ArrayList<>();
            for (int i = 0; i < wordsList.length; i++) {
                strings.add(wordsList[i]);
            }
            myDialog.dismiss();
            reclHelpWord.setLayoutManager(new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL));
            reclHelpWord.setAdapter(new HelpWordAdapter(strings));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

}
