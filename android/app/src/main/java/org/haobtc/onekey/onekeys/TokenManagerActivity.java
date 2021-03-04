package org.haobtc.onekey.onekeys;

import android.content.Context;
import android.content.Intent;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.alibaba.fastjson.JSON;
import com.google.common.base.Strings;
import com.zy.multistatepage.MultiStateContainer;
import com.zy.multistatepage.MultiStatePage;
import com.zy.multistatepage.state.SuccessState;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.haobtc.onekey.R;
import org.haobtc.onekey.activities.base.MyApplication;
import org.haobtc.onekey.adapter.HotTokenAdapter;
import org.haobtc.onekey.adapter.MoreTokenAdapter;
import org.haobtc.onekey.bean.PyResponse;
import org.haobtc.onekey.bean.TokenList;
import org.haobtc.onekey.card.utils.JsonParseUtils;
import org.haobtc.onekey.databinding.ActivityTokenManagerBinding;
import org.haobtc.onekey.manager.PyEnv;
import org.haobtc.onekey.onekeys.homepage.mindmenu.AddNewTokenActivity;
import org.haobtc.onekey.ui.base.BaseActivity;
import org.haobtc.onekey.ui.status.NoSearchState;
import org.haobtc.onekey.viewmodel.AppWalletViewModel;

public class TokenManagerActivity extends BaseActivity
        implements HotTokenAdapter.onHotSwitchClick, MoreTokenAdapter.onMoreSwitchClick {

    private ActivityTokenManagerBinding mBinding;
    private HotTokenAdapter mHotTokenAdapter;
    private MoreTokenAdapter mMoreTokenAdapter;
    private View mHeaderView;
    private TextView mTokenNum;
    private RecyclerView mHotRecyclerView;
    private List<TokenList.ERCToken> mAllTokens;
    private List<TokenList.ERCToken> moreTokens;
    private List<TokenList.ERCToken> mHotTokens;
    private List<TokenList.ERCToken> mSearchTokens;
    private HotTokenAdapter mSearchAdapter;
    private LinearLayout headerLayout;
    private MultiStateContainer mMultiStateContainer;
    private AppWalletViewModel mAppWalletViewModel;

    public static void start(Context context) {
        context.startActivity(new Intent(context, TokenManagerActivity.class));
    }

    @Override
    public void init() {
        setLeftTitle(R.string.token_manager);
        mAppWalletViewModel =
                new ViewModelProvider(MyApplication.getInstance()).get(AppWalletViewModel.class);
        mAllTokens = new ArrayList<>();
        moreTokens = new ArrayList<>();
        mHotTokens = new ArrayList<>();
        mSearchTokens = new ArrayList<>();
        mMultiStateContainer = MultiStatePage.bindMultiState(mBinding.searchRecyclerview);
        String json = JsonParseUtils.getJsonStr(mContext, "eth_token_list.json");
        TokenList tokenList = JSON.parseObject(json, TokenList.class);
        mAllTokens.addAll(tokenList.tokens);
        Collections.sort(mAllTokens, (o1, o2) -> o1.rank > o2.rank ? -1 : 1);
        getAllAddToken(mAllTokens);
        mHotTokens.addAll(mAllTokens.subList(0, 10));
        moreTokens.addAll(mAllTokens.subList(0, 50));
        moreTokens.sort(
                (o1, o2) -> {
                    String start = o1.symbol;
                    String end = o2.symbol;
                    return start.compareToIgnoreCase(end);
                });

        mMoreTokenAdapter = new MoreTokenAdapter(moreTokens, this);
        mSearchAdapter = new HotTokenAdapter(mSearchTokens, this);
        mBinding.searchRecyclerview.setAdapter(mSearchAdapter);
        mBinding.searchRecyclerview.setLayoutManager(new LinearLayoutManager(mContext));
        mBinding.moreRecyclerview.setLayoutManager(new LinearLayoutManager(mContext));
        mBinding.moreRecyclerview.setAdapter(mMoreTokenAdapter);
        mBinding.waveSlideBar.setOnTouchLetterChangeListener(
                letter -> {
                    int letterIndex = getLetterIndex(moreTokens, letter);
                    if (letterIndex > 0) {
                        mBinding.moreRecyclerview.smoothScrollToPosition(letterIndex + 1);
                    }
                });
        initHeaderView(mHotTokens);
    }

    private void getAllAddToken(List<TokenList.ERCToken> tokenList) {
        Disposable disposable =
                Observable.create(
                                (ObservableOnSubscribe<PyResponse<String>>)
                                        emitter -> {
                                            PyResponse<String> response = PyEnv.getAllTokenInfo();
                                            emitter.onNext(response);
                                            emitter.onComplete();
                                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(response -> doSelectToken(response, tokenList));
        mCompositeDisposable.add(disposable);
    }

    private void doSelectToken(PyResponse<String> response, List<TokenList.ERCToken> tokenList) {
        if (Strings.isNullOrEmpty(response.getErrors())) {
            try {
                List<String> list = JSON.parseArray(response.getResult(), String.class);
                if (list.size() > 0) {
                    for (TokenList.ERCToken token : tokenList) {
                        for (String s : list) {
                            if (token.address.equals(s)) {
                                token.isAdd = true;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.toString();
            }
        } else {
            mToast(response.getErrors());
        }
    }

    private int getLetterIndex(List<TokenList.ERCToken> moreTokens, String letter) {
        for (int i = 0; i < moreTokens.size(); i++) {
            if (moreTokens.get(i).symbol.startsWith(letter)
                    || moreTokens.get(i).symbol.toUpperCase().startsWith(letter)) {
                return i;
            }
        }
        return -1;
    }

    private void initHeaderView(List<TokenList.ERCToken> hotTokens) {
        mHeaderView = getLayoutInflater().inflate(R.layout.ac_token_list_headerview, null);
        headerLayout = mHeaderView.findViewById(R.id.layout_header);
        mTokenNum = mHeaderView.findViewById(R.id.token_num);
        mHotRecyclerView = mHeaderView.findViewById(R.id.hot_recyclerView);
        mHotTokenAdapter = new HotTokenAdapter(hotTokens, this);
        mHotRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mHotRecyclerView.setAdapter(mHotTokenAdapter);
        mMoreTokenAdapter.setHeaderView(mHeaderView);
        mBinding.searchEt.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(
                            CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {}

                    @Override
                    public void afterTextChanged(Editable s) {
                        String search = s.toString();
                        if (Strings.isNullOrEmpty(search)) {
                            mBinding.slideBarLayout.setVisibility(View.VISIBLE);
                            mHotTokenAdapter.setNewData(mHotTokens);
                            mHotRecyclerView.setAdapter(mHotTokenAdapter);
                            mBinding.searchRecyclerview.setVisibility(View.GONE);
                            mBinding.moreRecyclerview.setVisibility(View.VISIBLE);
                        } else {
                            mBinding.slideBarLayout.setVisibility(View.GONE);
                            mBinding.searchRecyclerview.setVisibility(View.VISIBLE);
                            mBinding.moreRecyclerview.setVisibility(View.GONE);
                            packageSearchData(search);
                        }
                    }
                });
    }

    private void packageSearchData(String search) {
        mSearchTokens.clear();
        List<TokenList.ERCToken> tempData = new ArrayList<>();
        for (TokenList.ERCToken allToken : mAllTokens) {
            if (search.startsWith("0x")) {
                if (allToken.address.equalsIgnoreCase(search)) {
                    tempData.add(allToken);
                }
            } else {
                if (allToken.symbol.contains(search.toLowerCase())
                        || allToken.symbol.contains(search.toUpperCase())
                        || allToken.name.contains(search.toLowerCase())
                        || allToken.name.contains(search.toUpperCase())) {
                    tempData.add(allToken);
                }
            }
        }
        if (tempData.size() == 0) {
            mMultiStateContainer.show(
                    NoSearchState.class,
                    noSearch -> {
                        noSearch.setOnAddTokenListener(
                                v -> {
                                    AddNewTokenActivity.start(mContext);
                                });
                    });
        } else {
            mMultiStateContainer.show(SuccessState.class);
            mSearchTokens.addAll(tempData);
        }
        mSearchAdapter.notifyDataSetChanged();
    }

    @Override
    public int getContentViewId() {
        return 0;
    }

    @Override
    public boolean enableViewBinding() {
        return true;
    }

    @Nullable
    @Override
    public View getLayoutView() {
        mBinding = ActivityTokenManagerBinding.inflate(getLayoutInflater());
        return mBinding.getRoot();
    }

    @Override
    protected boolean showToolBar() {
        return true;
    }

    @Override
    public void onHotCheckedListener(TokenList.ERCToken item, boolean isChecked, int position) {
        if (isChecked) {
            addToken(item.symbol, item.address);
            if (!moreTokens.contains(item)) {
                if (!mBinding.moreRecyclerview.isComputingLayout()
                        && RecyclerView.SCROLL_STATE_IDLE
                                == mBinding.moreRecyclerview.getScrollState()) {
                    moreTokens.add(item);
                    moreTokens.sort(
                            (o1, o2) -> {
                                String start = o1.symbol;
                                String end = o2.symbol;
                                return start.compareToIgnoreCase(end);
                            });
                    mMoreTokenAdapter.notifyDataSetChanged();
                }
            }
        } else {
            deleteToken(item.address);
        }
    }

    @Override
    public void onMoreCheckedListener(TokenList.ERCToken item, boolean isChecked) {
        List<TokenList.ERCToken> data = mHotTokenAdapter.getData();
        for (TokenList.ERCToken datum : data) {
            if (item.address.equals(datum.address)) {
                datum.isAdd = isChecked;
                if (!mHotRecyclerView.isComputingLayout()
                        && RecyclerView.SCROLL_STATE_IDLE == mHotRecyclerView.getScrollState()) {
                    mHotTokenAdapter.notifyDataSetChanged();
                }
                break;
            }
        }
        if (isChecked) {
            addToken(item.symbol, item.address);
        } else {
            deleteToken(item.address);
        }
    }

    /**
     * 添加token
     *
     * @param symbol
     * @param address
     */
    private void addToken(String symbol, String address) {
        Disposable disposable =
                Observable.create(
                                (ObservableOnSubscribe<PyResponse<String>>)
                                        emitter -> {
                                            PyResponse<String> response =
                                                    PyEnv.addToken(symbol, address);
                                            emitter.onNext(response);
                                            emitter.onComplete();
                                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                result -> {
                                    mAppWalletViewModel.refresh();
                                    if (!Strings.isNullOrEmpty(result.getErrors())) {
                                        mToast(result.getErrors());
                                    }
                                });
        mCompositeDisposable.add(disposable);
    }

    /**
     * 删除token
     *
     * @param address
     */
    private void deleteToken(String address) {
        Disposable disposable =
                Observable.create(
                                (ObservableOnSubscribe<PyResponse<String>>)
                                        emitter -> {
                                            PyResponse<String> response =
                                                    PyEnv.deleteToken(address);
                                            emitter.onNext(response);
                                            emitter.onComplete();
                                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                result -> {
                                    mAppWalletViewModel.refresh();
                                    if (!Strings.isNullOrEmpty(result.getErrors())) {
                                        mToast(result.getErrors());
                                    }
                                });
        mCompositeDisposable.add(disposable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCompositeDisposable.dispose();
    }
}
