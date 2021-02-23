package org.haobtc.onekey.onekeys.homepage.mindmenu;

import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import com.google.common.base.Strings;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import org.haobtc.onekey.R;
import org.haobtc.onekey.activities.base.BaseActivity;
import org.haobtc.onekey.activities.base.MyApplication;
import org.haobtc.onekey.adapter.HdWalletAssetAdapter;
import org.haobtc.onekey.bean.AllWalletBalanceBean;
import org.haobtc.onekey.bean.PyResponse;
import org.haobtc.onekey.business.wallet.BalanceManager;
import org.haobtc.onekey.business.wallet.SystemConfigManager;
import org.haobtc.onekey.business.wallet.bean.WalletBalanceBean;
import org.haobtc.onekey.exception.HardWareExceptions;
import org.haobtc.onekey.viewmodel.AppWalletViewModel;

public class AllAssetsActivity extends BaseActivity implements TextWatcher {

    @BindView(R.id.test_all_assets)
    TextView testAllAssets;

    @BindView(R.id.edit_search)
    EditText editSearch;

    @BindView(R.id.recl_assets)
    RecyclerView reclAssets;

    @BindView(R.id.tet_None)
    TextView tetNone;

    private List<WalletBalanceBean> walletInfo;
    private ArrayList<WalletBalanceBean> mAdapterDatas;
    private SystemConfigManager mSystemConfigManager;
    private BalanceManager mBalanceManager;
    private Disposable mLoadDisposable;
    private AppWalletViewModel mAppWalletViewModel;
    private HdWalletAssetAdapter hdWalletAssetAdapter;
    private ArrayList<WalletBalanceBean> mTempList;

    @Override
    public int getLayoutId() {
        return R.layout.activity_all_assets;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        mSystemConfigManager = new SystemConfigManager(this);
        mBalanceManager = new BalanceManager();

        editSearch.addTextChangedListener(this);
    }

    @Override
    public void initData() {
        mAppWalletViewModel =
                new ViewModelProvider(MyApplication.getInstance()).get(AppWalletViewModel.class);
        mAdapterDatas = new ArrayList<>();
        mTempList = new ArrayList<>();
        walletInfo = new ArrayList<>();
        hdWalletAssetAdapter =
                new HdWalletAssetAdapter(mContext, mAppWalletViewModel, mAdapterDatas);
        reclAssets.setAdapter(hdWalletAssetAdapter);
        reclAssets.setNestedScrollingEnabled(false);
        // get all wallet
        getAllWalletList();
    }

    private void getAllWalletList() {
        if (mLoadDisposable != null && !mLoadDisposable.isDisposed()) {
            mLoadDisposable.dispose();
        }
        mLoadDisposable =
                Observable.create(
                                (ObservableOnSubscribe<PyResponse<AllWalletBalanceBean>>)
                                        emitter -> {
                                            PyResponse<AllWalletBalanceBean> allWalletBalance =
                                                    mBalanceManager.getAllWalletBalances();
                                            emitter.onNext(allWalletBalance);
                                            emitter.onComplete();
                                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .doOnSubscribe(disposable -> showProgress())
                        .doFinally(this::dismissProgress)
                        .subscribe(
                                allWalletBalanceString -> {
                                    if (Strings.isNullOrEmpty(allWalletBalanceString.getErrors())) {
                                        AllWalletBalanceBean allWalletBalance =
                                                allWalletBalanceString.getResult();
                                        if (allWalletBalance != null
                                                && allWalletBalance.getWalletInfo().size() > 0) {
                                            tetNone.setVisibility(View.GONE);

                                            String allBalance = allWalletBalance.getAllBalance();
                                            String fiat =
                                                    allBalance.substring(
                                                            0, allBalance.indexOf(" "));
                                            float f = Float.parseFloat(fiat);
                                            DecimalFormat decimalFormat =
                                                    new DecimalFormat(
                                                            "0.00"); // 构造方法的字符格式这里如果小数不足2位,会以0补足.
                                            String money = decimalFormat.format(f);
                                            String currencySymbol =
                                                    mSystemConfigManager.getCurrentFiatSymbol();
                                            testAllAssets.setText(
                                                    String.format("%s %s", currencySymbol, money));
                                            walletInfo.clear();
                                            walletInfo.addAll(allWalletBalance.getWalletInfo());
                                            mAdapterDatas.addAll(walletInfo);
                                            hdWalletAssetAdapter.notifyDataSetChanged();
                                        }
                                    } else {
                                        tetNone.setVisibility(View.VISIBLE);
                                        mToast(allWalletBalanceString.getErrors());
                                    }
                                },
                                e -> {
                                    tetNone.setVisibility(View.VISIBLE);
                                    mToast(HardWareExceptions.getThrowString(e));
                                    e.printStackTrace();
                                });
    }

    @OnClick({R.id.img_back})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {}

    @Override
    public void afterTextChanged(Editable s) {
        mAdapterDatas.clear();
        mTempList.clear();
        if (!TextUtils.isEmpty(s.toString()) && walletInfo != null && walletInfo.size() > 0) {
            for (int i = 0; i < walletInfo.size(); i++) {
                if (walletInfo.get(i).getName().startsWith(s.toString())) {
                    mTempList.add(walletInfo.get(i));
                }
            }
            mAdapterDatas.addAll(mTempList);
        } else {
            mAdapterDatas.addAll(walletInfo);
        }
        hdWalletAssetAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLoadDisposable != null && !mLoadDisposable.isDisposed()) {
            mLoadDisposable.dispose();
        }
    }
}
