package org.haobtc.wallet.activities.personalwallet;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.LayoutRes;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.activities.service.CommunicationModeSelector;
import org.haobtc.wallet.aop.SingleClick;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static org.haobtc.wallet.activities.service.CommunicationModeSelector.xpub;

public class ImportHistoryWalletActivity extends BaseActivity {

    public static final String TAG = ImportHistoryWalletActivity.class.getSimpleName();
    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.create_trans_one2one)
    Button createTransOne2one;
    private Dialog dialogBtoms;
    private EditText edit_bixinName;
    private TextView textView;


    @Override
    public int getLayoutId() {
        return R.layout.activity_import_history_wallet;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);

    }

    @Override
    public void initData() {

    }

    @SingleClick
    @OnClick({R.id.img_back, R.id.create_trans_one2one})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.create_trans_one2one:
//                mIntent(ChooseHistryWalletActivity.class);
                // new version code
                CommunicationModeSelector.runnables.clear();
                CommunicationModeSelector.runnables.add(null);
                CommunicationModeSelector.runnables.add(runnable2);
                Intent intent = new Intent(this, CommunicationModeSelector.class);
                intent.putExtra("tag", TAG);
                startActivity(intent);
                break;
        }
    }

    private Runnable runnable2 = () -> showConfirmPubDialog(this, R.layout.bixinkey_confirm, xpub);

    private void showConfirmPubDialog(Context context, @LayoutRes int resource, String xpub) {
        //set see view
        View view = View.inflate(context, resource, null);
        dialogBtoms = new Dialog(context, R.style.dialog);

        edit_bixinName = view.findViewById(R.id.edit_keyName);
        TextView tet_Num = view.findViewById(R.id.txt_textNum);
        textView = view.findViewById(R.id.text_public_key_cosigner_popup);
        textView.setText(xpub);
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
                    Toast.makeText(ImportHistoryWalletActivity.this, R.string.moreinput_text, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        view.findViewById(R.id.btn_confirm).setOnClickListener(v -> {
            if (TextUtils.isEmpty(edit_bixinName.getText().toString())){
                mToast(getString(R.string.input_name));
                return;
            }
            Intent intent1 = new Intent(ImportHistoryWalletActivity.this, ChooseHistryWalletActivity.class);
            intent1.putExtra("histry_xpub", xpub);
            startActivity(intent1);
            finish();
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
}
