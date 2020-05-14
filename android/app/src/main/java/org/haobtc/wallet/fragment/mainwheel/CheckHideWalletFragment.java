package org.haobtc.wallet.fragment.mainwheel;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.LayoutRes;
import androidx.cardview.widget.CardView;

import com.chaquo.python.Kwarg;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.personalwallet.hidewallet.CheckHideWalletActivity;
import org.haobtc.wallet.activities.personalwallet.hidewallet.HideWalletActivity;
import org.haobtc.wallet.activities.service.CommunicationModeSelector;
import org.haobtc.wallet.aop.SingleClick;
import org.haobtc.wallet.utils.Daemon;

import java.util.Locale;
import java.util.Objects;

import static org.haobtc.wallet.activities.service.CommunicationModeSelector.xpub;

/**
 * .
 */
public class CheckHideWalletFragment extends WheelViewpagerFragment {
    public static final String TAG = CheckHideWalletFragment.class.getSimpleName();
    private SharedPreferences.Editor edit;
    private Dialog dialogBtoms;
    private int defaultKeyNum;

    @SuppressLint("CommitPrefEdits")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_check_hide_wallet, container, false);
        SharedPreferences preferences = requireActivity().getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        edit = preferences.edit();
        defaultKeyNum = preferences.getInt("defaultKeyNum", 0);
        initview(view);
        return view;
    }

    private void initview(View view) {

        CardView cardCheckHidewallet = view.findViewById(R.id.wallet_card_add);
        cardCheckHidewallet.setOnClickListener(new View.OnClickListener() {
            @SingleClick
            @Override
            public void onClick(View v) {
//                Intent intent = new Intent(getActivity(), HideWalletActivity.class);
//                startActivity(intent);
                edit.putString("createOrcheck", "check");
                edit.apply();
                CommunicationModeSelector.runnables.clear();
                CommunicationModeSelector.runnables.add(null);
                CommunicationModeSelector.runnables.add(runnable2);
                Intent intent = new Intent(getActivity(), CommunicationModeSelector.class);
                intent.putExtra("tag", TAG);
                startActivity(intent);
            }
        });
    }

    private Runnable runnable2 = () -> showConfirmPubDialog(getActivity(), R.layout.bixinkey_confirm, xpub);

    private void showConfirmPubDialog(Context context, @LayoutRes int resource, String xpub) {
        //set see view
        View view = View.inflate(context, resource, null);
        dialogBtoms = new Dialog(context, R.style.dialog);
        EditText edit_bixinName = view.findViewById(R.id.edit_keyName);
        TextView tet_Num = view.findViewById(R.id.txt_textNum);
        TextView textView = view.findViewById(R.id.text_public_key_cosigner_popup);
        textView.setText(xpub);
        int defaultKeyNameNum = defaultKeyNum + 1;
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
                    Toast.makeText(context, getString(R.string.moreinput_text), Toast.LENGTH_SHORT).show();
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
                Toast.makeText(context, getString(R.string.input_name), Toast.LENGTH_SHORT).show();
                return;
            }
            String strXpub = "[\"" + strSweep + "\"]";
            try {
                Daemon.commands.callAttr("import_create_hw_wallet", strBixinname, 1, 1, strXpub, new Kwarg("hide_type", true));
            } catch (Exception e) {
                e.printStackTrace();
                String message = e.getMessage();
                if ("BaseException: file already exists at path".equals(message)) {
                    Toast.makeText(context, getString(R.string.changewalletname), Toast.LENGTH_SHORT).show();
                } else if (message.contains("The same xpubs have create wallet")) {
                    Toast.makeText(context, getString(R.string.xpub_have_wallet), Toast.LENGTH_SHORT).show();
                }
                return;
            }
            edit.putInt("defaultKeyNum", defaultKeyNameNum);
            edit.apply();
            dialogBtoms.cancel();
            // todo: 弹窗关闭
            Intent intent = new Intent(getActivity(), CheckHideWalletActivity.class);
            intent.putExtra("hideWalletName", strBixinname);
            startActivity(intent);
        });

        //cancel dialog
        view.findViewById(R.id.img_cancle).setOnClickListener(v -> {
            dialogBtoms.cancel();
            // todo: 弹窗关闭
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
    public void setValue(String msgVote) {
    }
}
