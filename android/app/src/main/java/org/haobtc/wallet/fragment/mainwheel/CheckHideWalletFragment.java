package org.haobtc.wallet.fragment.mainwheel;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.cardview.widget.CardView;

import com.chaquo.python.Kwarg;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.personalwallet.hidewallet.CheckHideWalletActivity;
import org.haobtc.wallet.activities.service.CommunicationModeSelector;
import org.haobtc.wallet.aop.SingleClick;
import org.haobtc.wallet.utils.Daemon;

import static org.haobtc.wallet.activities.service.CommunicationModeSelector.xpub;

/**
 * .
 */
public class CheckHideWalletFragment extends WheelViewpagerFragment {
    public static final String TAG = CheckHideWalletFragment.class.getSimpleName();

    @SuppressLint("CommitPrefEdits")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_check_hide_wallet, container, false);
        initview(view);
        return view;
    }

    private void initview(View view) {
        CardView cardCheckHidewallet = view.findViewById(R.id.wallet_card_add);
        cardCheckHidewallet.setOnClickListener(new View.OnClickListener() {
            @SingleClick
            @Override
            public void onClick(View v) {
                CommunicationModeSelector.runnables.clear();
                CommunicationModeSelector.runnables.add(null);
                CommunicationModeSelector.runnables.add(runnable2);
                Intent intent = new Intent(getActivity(), CommunicationModeSelector.class);
                intent.putExtra("tag", TAG);
                startActivity(intent);
            }
        });
    }

    private Runnable runnable2 = () -> createHideWallet(xpub);

    private void createHideWallet(String xpub) {
        //set see view
//        View view = View.inflate(context, resource, null);
//        dialogBtoms = new Dialog(context, R.style.dialog);
//        EditText edit_bixinName = view.findViewById(R.id.edit_keyName);
//        TextView tet_Num = view.findViewById(R.id.txt_textNum);
//        TextView textView = view.findViewById(R.id.text_public_key_cosigner_popup);
//        textView.setText(xpub);
//        int defaultKeyNameNum = defaultKeyNum + 1;
//        edit_bixinName.setText(String.format("pub%s", String.valueOf(defaultKeyNameNum)));
//        edit_bixinName.addTextChangedListener(new TextWatcher() {
//            CharSequence input;
//
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                input = s;
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                tet_Num.setText(String.format(Locale.CHINA, "%d/20", input.length()));
//                if (input.length() > 19) {
//                    Toast.makeText(context, getString(R.string.moreinput_text), Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//
//            }
//        });
//        view.findViewById(R.id.btn_confirm).setOnClickListener(v -> {
        String strXpub = "[\"" + xpub + "\"]";
        try {
            Daemon.commands.callAttr("import_create_hw_wallet", "隐藏钱包" , 1, 1, strXpub, new Kwarg("hide_type", true));
        } catch (Exception e) {
            e.printStackTrace();
            String message = e.getMessage();
            if ("BaseException: file already exists at path".equals(message)) {
                Toast.makeText(getActivity(), getString(R.string.changewalletname), Toast.LENGTH_SHORT).show();
            } else if (message.contains("The same xpubs have create wallet")) {
                Toast.makeText(getActivity(), getString(R.string.xpub_have_wallet), Toast.LENGTH_SHORT).show();
            }
            return;
        }
        // todo: 弹窗关闭
        Intent intent = new Intent(getActivity(), CheckHideWalletActivity.class);
        startActivity(intent);
//        });

        //cancel dialog
//        view.findViewById(R.id.img_cancle).setOnClickListener(v -> {
//            dialogBtoms.cancel();
//            // todo: 弹窗关闭
//        });
//
//        dialogBtoms.setContentView(view);
//        Window window = dialogBtoms.getWindow();
//        //set pop_up size
//        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
//        //set locate
//        window.setGravity(Gravity.BOTTOM);
//        //set animal
//        window.setWindowAnimations(R.style.AnimBottom);
//        dialogBtoms.show();
    }

    @Override
    public void setValue(String msgVote) {
    }
}
