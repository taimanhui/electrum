package org.haobtc.wallet.activities.transaction;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.aop.SingleClick;
import org.haobtc.wallet.utils.MyDialog;
import org.haobtc.wallet.utils.internet.NetBroadcastReceiver;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class CheckChainDetailWebActivity extends BaseActivity implements NetBroadcastReceiver.NetStatusMonitor{

    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.web_heckChain)
    WebView webHeckChain;
    @BindView(R.id.lin_Nonet)
    LinearLayout linNonet;
    private String checkTxid;
    private int nets = 2;
    private boolean netStatus;
    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 99) {
                nets = 2;
                webHeckChain.setVisibility(View.VISIBLE);
                linNonet.setVisibility(View.GONE);
            } else {
                mToast(getString(R.string.net_dont_use));
                nets = 4;
                webHeckChain.setVisibility(View.GONE);
                linNonet.setVisibility(View.VISIBLE);
            }
        }
    };
    private MyDialog myDialog;
    private NetBroadcastReceiver netBroadcastReceiver;
    private String blockServerLine;

    @Override
    public int getLayoutId() {
        return R.layout.activity_check_chain_detail_web;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        SharedPreferences preferences = getSharedPreferences("Preferences", MODE_PRIVATE);
        blockServerLine = preferences.getString("blockServerLine", "https://btc.com/");
        myDialog = MyDialog.showDialog(CheckChainDetailWebActivity.this);
        myDialog.show();
        Intent intent = getIntent();
        checkTxid = intent.getStringExtra("checkTxid");

    }

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    public void initData() {
        webHeckChain.getSettings().setJavaScriptEnabled(true);
        webHeckChain.getSettings().setAppCacheEnabled(true);
        webHeckChain.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
        webHeckChain.getSettings().setDomStorageEnabled(true);
        webHeckChain.setWebChromeClient(new WebChromeClient());
        webHeckChain.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                myDialog.show();
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                myDialog.dismiss();
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                super.onReceivedError(view, request, error);
                myDialog.dismiss();
            }
        });

        Log.i("hhhhhhhhhhhh", "initData: "+blockServerLine+checkTxid);
        if (nets == 2) {
            webHeckChain.loadUrl(blockServerLine+checkTxid);
        }
    }


    @SingleClick
    @OnClick({R.id.img_back})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerBroadcastReceiver();

    }

    private void registerBroadcastReceiver() {
        if (netBroadcastReceiver == null) {
            netBroadcastReceiver = new NetBroadcastReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            registerReceiver(netBroadcastReceiver, filter);
            netBroadcastReceiver.setStatusMonitor(this);
        }
    }


    @Override
    public void onNetChange(boolean netStatus) {
        this.netStatus = netStatus;
        isNetConnect();
    }

    private void isNetConnect() {
        Message message = new Message();
        if (netStatus) {
            message.what = 99;
            handler.sendMessage(message);
        } else {
            Toast.makeText(this, getString(R.string.net_dont_use), Toast.LENGTH_SHORT).show();
            message.what = 100;
            handler.sendMessage(message);
        }

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        myDialog.dismiss();
        if (webHeckChain != null) {
            webHeckChain.destroy();
        }
        if (netBroadcastReceiver != null) {
            //注销广播
            unregisterReceiver(netBroadcastReceiver);
        }
    }


}
