package org.haobtc.wallet.fragment.mainwheel;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.chaquo.python.PyObject;
import com.google.gson.Gson;

import org.greenrobot.eventbus.EventBus;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.ReceivedPageActivity;
import org.haobtc.wallet.activities.SendOne2OneMainPageActivity;
import org.haobtc.wallet.activities.personalwallet.WalletDetailsActivity;
import org.haobtc.wallet.activities.sign.SignActivity;
import org.haobtc.wallet.aop.SingleClick;
import org.haobtc.wallet.bean.MainNewWalletBean;
import org.haobtc.wallet.event.FirstEvent;
import org.haobtc.wallet.utils.Daemon;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * A simple {@link Fragment} subclass.
 */
public class WheelViewpagerFragment extends Fragment implements View.OnClickListener {

    private TextView wallet_card_name;
    private TextView walletpersonce;
    private TextView walletBlance;
    private String name;
    private String personce;
    private Button btnLeft;
    private Button btncenetr;
    private PyObject select_wallet;
    private TextView tetCny;
    private TextView btn_appWallet;
    private boolean isFirst = false;
    private SharedPreferences.Editor edit;
    private SharedPreferences preferences;
    private TextView tetFiat;
    private ConstraintLayout conlayBback;
    private Button btnRight;
    private String strCNY;
    private String substring;
    private TextView walletCard;

    public WheelViewpagerFragment() {

    }

    public WheelViewpagerFragment(String name, String personce) {
        this.name = name;
        this.personce = personce;
    }

    public WheelViewpagerFragment(String name, String personce, boolean isFirst) {
        this.isFirst = isFirst;
        this.name = name;
        this.personce = personce;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wheel_viewpager, container, false);
        preferences = getActivity().getSharedPreferences("Preferences", Context.MODE_PRIVATE);
        edit = preferences.edit();
        wallet_card_name = view.findViewById(R.id.wallet_card_name);
        walletpersonce = view.findViewById(R.id.wallet_card_tv2);
        walletBlance = view.findViewById(R.id.wallet_card_tv4);
        tetFiat = view.findViewById(R.id.tet_fiat);
        btnLeft = view.findViewById(R.id.wallet_card_bn1);
        btncenetr = view.findViewById(R.id.wallet_card_bn2);
        btnRight = view.findViewById(R.id.wallet_card_bn3);
        btn_appWallet = view.findViewById(R.id.app_wallet);
        tetCny = view.findViewById(R.id.tet_Cny);
        conlayBback = view.findViewById(R.id.conlay_back);
        walletCard = view.findViewById(R.id.wallet_card_tv3);
        init();
        initdata();

        return view;
    }

    private void init() {
        btnLeft.setOnClickListener(this);
        btncenetr.setOnClickListener(this);
        btnRight.setOnClickListener(this);
        conlayBback.setOnClickListener(this);
        wallet_card_name.setText(name);
        if (getActivity() != null) {
            if (!TextUtils.isEmpty(personce)) {
                if (personce.equals("standard")) {
                    btn_appWallet.setVisibility(View.VISIBLE);
                    walletpersonce.setVisibility(View.GONE);
                    conlayBback.setBackground(getActivity().getDrawable(R.drawable.home_gray_bg));
                    btnLeft.setBackground(getActivity().getDrawable(R.drawable.text_tou_back_blue));
                    btncenetr.setBackground(getActivity().getDrawable(R.drawable.text_tou_back_blue));
                    btnRight.setBackground(getActivity().getDrawable(R.drawable.text_tou_back_blue));
                } else {
                    walletpersonce.setText(personce);
                    conlayBback.setBackground(getActivity().getDrawable(R.drawable.home_bg));
                    btnLeft.setBackground(getActivity().getDrawable(R.drawable.button_bk_small));
                    btncenetr.setBackground(getActivity().getDrawable(R.drawable.button_bk_small));
                    btnRight.setBackground(getActivity().getDrawable(R.drawable.button_bk_small));
                }
            }
        }
    }

    private void initdata() {
        // first wallet
        if (isFirst) refreshList();
        //get wallet unit
        String base_unit = preferences.getString("base_unit", "");
        walletCard.setText(String.format("%s（%s）", getString(R.string.balance), base_unit));

    }

    public void refreshList() {
        try {
            Daemon.commands.callAttr("load_wallet", name);
            getWalletMsg();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //get wallet message
    public void getWalletMsg() {
        try {
            select_wallet = Daemon.commands.callAttr("select_wallet", name);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (select_wallet != null) {
//            EventBus.getDefault().post(new FirstEvent("22"));
            String toString = select_wallet.toString();
            Log.i("select_wallet", "select_wallet+++: " + toString);
            Gson gson = new Gson();
            MainNewWalletBean mainWheelBean = gson.fromJson(toString, MainNewWalletBean.class);
            String walletType = mainWheelBean.getWalletType();
            String balanceC = mainWheelBean.getBalance();
            if (!TextUtils.isEmpty(walletType)) {
                String streplaceC = walletType.replaceAll("of", "/");
                walletpersonce.setText(streplaceC);
            }
            if (!TextUtils.isEmpty(balanceC)) {
                if (balanceC.contains("(")) {
                    substring = balanceC.substring(0, balanceC.indexOf("("));
                   // Log.e("substring", "substring: " + substring);
                   // Log.e("substring", "balanceC: " + balanceC);

                   // Log.i("getWalletMsgJXM", "substring:::" + substring);
                    walletBlance.setText(substring);

                    strCNY = balanceC.substring(balanceC.indexOf("(") + 1, balanceC.indexOf(")"));
                    if (!TextUtils.isEmpty(strCNY)) {
                        tetCny.setText(String.format("≈ %s", strCNY));
                    }
                } else {
                  //  Log.i("getWalletMsgJXM", "balanceC:::" + balanceC);
                    walletBlance.setText(balanceC);
                }
            }
        }
    }

    public void setValue(String msgVote) {
        try {
            JSONObject jsonObject = new JSONObject(msgVote);
            if (msgVote.contains("balance")) {
                String balance = jsonObject.getString("balance");
               // Log.i("getWalletMsgJXM", "event+substring:::" + balance);
                walletBlance.setText(balance);
            }
            if (msgVote.contains("fiat")) {
                String fiat = jsonObject.getString("fiat");
                if (!TextUtils.isEmpty(fiat)) {
                    tetCny.setText(String.format("≈ %s", fiat));
                }
            }
            if (msgVote.contains("unconfirmed")) {
                String unconfirmed = jsonObject.getString("unconfirmed");
                tetFiat.setText(String.format("%s%s", unconfirmed, getString(R.string.unconfirm)));
            } else {
                tetFiat.setText("");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @SingleClick
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.conlay_back:
                Intent intent = new Intent(getActivity(), WalletDetailsActivity.class);
                intent.putExtra("wallet_name", name);
                startActivity(intent);
                break;
            case R.id.wallet_card_bn1:
                edit.putString("wallet_type_to_sign", personce);
                edit.apply();
                Intent intent1 = new Intent(getActivity(), SendOne2OneMainPageActivity.class);
                intent1.putExtra("wallet_name", name);
                intent1.putExtra("wallet_type", personce);
                intent1.putExtra("strNowBtc", substring);
                intent1.putExtra("strNowCny", strCNY);
                startActivity(intent1);
                break;
            case R.id.wallet_card_bn2:
                Intent intent2 = new Intent(getActivity(), ReceivedPageActivity.class);
                startActivity(intent2);
                break;
            case R.id.wallet_card_bn3:
                Intent intent3 = new Intent(getActivity(), SignActivity.class);
                intent3.putExtra("personceType", personce);
                startActivity(intent3);
                break;

        }
    }
}