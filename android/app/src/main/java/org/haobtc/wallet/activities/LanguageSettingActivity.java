package org.haobtc.wallet.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import org.haobtc.wallet.MainActivity;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.utils.CommonUtils;

import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LanguageSettingActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener {

    @BindView(R.id.radio_chineseasy)
    RadioButton radioChineseasy;
    @BindView(R.id.radio_character)
    RadioButton radioCharacter;
    @BindView(R.id.radio_english)
    RadioButton radioEnglish;
    @BindView(R.id.radio_Korean)
    RadioButton radioKorean;
    @BindView(R.id.radio_Japanese)
    RadioButton radioJapanese;
    @BindView(R.id.radio_group)
    RadioGroup radioGroup;
    private SharedPreferences preferences;
    private SharedPreferences.Editor edit;

    public int getLayoutId() {
        return R.layout.language_setting;
    }

    public void initView() {
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
        preferences = getSharedPreferences("preferences", MODE_PRIVATE);
        edit = preferences.edit();
        CommonUtils.enableToolBar(this, R.string.language);
        radioGroup.setOnCheckedChangeListener(this);

    }

    @Override
    public void initData() {

    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        switch (checkedId) {
            case R.id.radio_chineseasy:
                mTextChinese();
                refreshSelf();
                edit.putString("language","Chinese");
                edit.apply();
                break;
            case R.id.radio_character:

                break;
            case R.id.radio_english:
                mTextEnglish();
                refreshSelf();
                edit.putString("language","English");
                edit.apply();
                break;
            case R.id.radio_Korean:

                break;
            case R.id.radio_Japanese:

                break;
        }
    }

    public void refreshSelf() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
