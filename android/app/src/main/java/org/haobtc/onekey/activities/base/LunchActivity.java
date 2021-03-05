package org.haobtc.onekey.activities.base;

import android.content.Intent;
import com.google.common.base.Strings;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.util.Optional;
import org.haobtc.onekey.R;
import org.haobtc.onekey.bean.PyResponse;
import org.haobtc.onekey.business.wallet.TokenManager;
import org.haobtc.onekey.constant.Constant;
import org.haobtc.onekey.manager.PreferencesManager;
import org.haobtc.onekey.manager.PyEnv;
import org.haobtc.onekey.onekeys.GuidanceActivity;
import org.haobtc.onekey.onekeys.HomeOneKeyActivity;
import org.haobtc.onekey.utils.NfcUtils;

/** @author liyan */
public class LunchActivity extends BaseActivity {

    @Override
    public int getLayoutId() {
        return R.layout.activity_lunch;
    }

    @Override
    public void initView() {
        if (!Optional.ofNullable(NfcUtils.nfcCheck(this, false)).isPresent()) {
            PreferencesManager.put(this, "Preferences", Constant.WAY, Constant.WAY_MODE_BLE);
            PreferencesManager.put(this, "Preferences", Constant.NFC_SUPPORT, false);
        }
    }

    private void init() {
        boolean firstRun =
                (boolean) PreferencesManager.get(this, "Preferences", Constant.FIRST_RUN, false);
        if (firstRun) {
            Intent intent = new Intent(LunchActivity.this, HomeOneKeyActivity.class);
            startActivity(intent);
            finish();
        } else {
            initGuide();
        }
    }

    private void initGuide() {
        Intent intent = new Intent(LunchActivity.this, GuidanceActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void initData() {
        PyEnv.init(MyApplication.getInstance());
        init();
        requestTokenList();
    }

    private void requestTokenList() {
        Observable.create(
                        (ObservableOnSubscribe<PyResponse<String>>)
                                emitter -> {
                                    PyResponse<String> response = PyEnv.getAllTokenInfo();
                                    emitter.onNext(response);
                                    emitter.onComplete();
                                })
                .subscribeOn(Schedulers.io())
                .subscribe(
                        response -> {
                            if (Strings.isNullOrEmpty(response.getErrors())) {
                                new TokenManager().uploadLocalTokenList(response.getResult());
                            }
                        });
    }
}
