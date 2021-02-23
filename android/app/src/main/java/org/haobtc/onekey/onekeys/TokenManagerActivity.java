package org.haobtc.onekey.onekeys;

import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.alibaba.fastjson.JSON;
import com.orhanobut.logger.Logger;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.haobtc.onekey.R;
import org.haobtc.onekey.adapter.HotTokenAdapter;
import org.haobtc.onekey.adapter.MoreTokenAdapter;
import org.haobtc.onekey.bean.TokenList;
import org.haobtc.onekey.business.wallet.TokenManager;
import org.haobtc.onekey.card.utils.JsonParseUtils;
import org.haobtc.onekey.databinding.ActivityTokenManagerBinding;
import org.haobtc.onekey.ui.base.BaseActivity;
import org.haobtc.onekey.ui.widget.EditTextSearch;
import org.haobtc.onekey.ui.widget.WaveSideBarView;

public class TokenManagerActivity extends BaseActivity
        implements HotTokenAdapter.onHotSwitchClick, MoreTokenAdapter.onMoreSwitchClick {

    private ActivityTokenManagerBinding mBinding;
    private HotTokenAdapter mHotTokenAdapter;
    private MoreTokenAdapter mMoreTokenAdapter;
    private View mHeaderView;
    private EditTextSearch mSearch;
    private TextView mTokenNum;
    private RecyclerView mHotRecyclerView;
    private TokenManager mTokenManager;

    public static void start(Context context) {
        context.startActivity(new Intent(context, TokenManagerActivity.class));
    }

    @Override
    public void init() {
        setLeftTitle(R.string.token_manager);
        mTokenManager = new TokenManager();
        Logger.d("当前时间戳：" + System.currentTimeMillis());
        String json = JsonParseUtils.getJsonStr(mContext, "eth_token_list.json");
        TokenList tokenList = JSON.parseObject(json, TokenList.class);
        Collections.sort(tokenList.tokens, (o1, o2) -> o1.rank > o2.rank ? -1 : 1);
        long currentTime = System.currentTimeMillis() / 1000;
        if (tokenList.timestamp > currentTime) {
            uploadLocal(json);
        }
        List<TokenList.ERCToken> hotTokens = tokenList.tokens.subList(0, 10);

        List<TokenList.ERCToken> moreTokens = new ArrayList<>();
        moreTokens.addAll(tokenList.tokens.subList(0, 50));
        moreTokens.sort(
                (o1, o2) -> {
                    String start = o1.symbol;
                    String end = o2.symbol;
                    return start.compareToIgnoreCase(end);
                });

        mMoreTokenAdapter = new MoreTokenAdapter(moreTokens, this);
        mBinding.moreRecyclerview.setLayoutManager(new LinearLayoutManager(mContext));
        mBinding.moreRecyclerview.setAdapter(mMoreTokenAdapter);
        mBinding.waveSlideBar.setOnTouchLetterChangeListener(
                new WaveSideBarView.OnTouchLetterChangeListener() {
                    @Override
                    public void onLetterChange(String letter) {
                        int letterIndex = getLetterIndex(moreTokens, letter);
                        Logger.d("索引字母点击" + letter + "  定位的位置：" + letterIndex);
                        if (letterIndex > 0) {
                            mBinding.moreRecyclerview.smoothScrollToPosition(letterIndex);
                        }
                    }
                });
        initHeaderView(hotTokens);
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

    private void uploadLocal(String json) {
        Disposable disposable =
                Observable.create(
                                (ObservableOnSubscribe<Boolean>)
                                        emitter -> {
                                            boolean b = mTokenManager.uploadLocalTokenList(json);
                                            emitter.onNext(b);
                                            emitter.onComplete();
                                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                result -> {
                                    Logger.d("File upload Success:  " + result);
                                    String localTokenList = mTokenManager.getLocalTokenList();
                                    Logger.d("读取文件：" + localTokenList);
                                });
        mCompositeDisposable.add(disposable);
    }

    private void initHeaderView(List<TokenList.ERCToken> hotTokens) {
        mHeaderView = getLayoutInflater().inflate(R.layout.ac_token_list_headerview, null);
        mSearch = mHeaderView.findViewById(R.id.search_et);
        mTokenNum = mHeaderView.findViewById(R.id.token_num);
        mHotRecyclerView = mHeaderView.findViewById(R.id.hot_recyclerView);
        mHotTokenAdapter = new HotTokenAdapter(hotTokens, this);
        mHotRecyclerView.setLayoutManager(new LinearLayoutManager(mContext));
        mHotRecyclerView.setAdapter(mHotTokenAdapter);
        mMoreTokenAdapter.setHeaderView(mHeaderView);
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
        Logger.d("添加token" + item.isAdd);
        if (isChecked) {
            //            PyResponse<String> response = PyEnv.addToken(item.symbol, item.address);
            //            if (Strings.isNullOrEmpty(response.getResult())) {
            //                Logger.d("添加token" + "成功");
            //            } else {
            //                Logger.d("添加token" + "失败");
            //            }
        } else {

        }
    }

    @Override
    public void onMoreCheckedListener(TokenList.ERCToken item, boolean isChecked, int position) {}
}
