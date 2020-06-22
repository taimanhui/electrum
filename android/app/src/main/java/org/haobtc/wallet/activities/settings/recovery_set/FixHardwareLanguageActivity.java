package org.haobtc.wallet.activities.settings.recovery_set;

import android.content.Intent;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.TextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import org.haobtc.wallet.activities.service.CommunicationModeSelector;
import org.haobtc.wallet.event.SetKeyLanguageEvent;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FixHardwareLanguageActivity extends BaseActivity {

    public static final String TAG = FixHardwareLanguageActivity.class.getSimpleName();
    @BindView(R.id.chinese_easy)
    TextView chineseEasy;
    @BindView(R.id.test_english)
    TextView testEnglish;
    private String keyLanguage = "Chinese";
    private SharedPreferences preferences;

    @Override
    public int getLayoutId() {
        return R.layout.activity_fix_hardware_language;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        preferences = getSharedPreferences("Preferences", MODE_PRIVATE);

    }

    @Override
    public void initData() {
        if (preferences.getString("key_language", "").equals("English")) {
            testEnglish.setTextColor(getColor(R.color.button_bk_disableok));
            chineseEasy.setTextColor(getColor(R.color.text_color1));
        }
    }

    @OnClick({R.id.img_back, R.id.chinese_easy, R.id.test_english})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.chinese_easy:
                keyLanguage = "Chinese";
                testEnglish.setTextColor(getColor(R.color.text_color1));
                chineseEasy.setTextColor(getColor(R.color.button_bk_disableok));
                CommunicationModeSelector.runnables.clear();
                CommunicationModeSelector.runnables.add(null);
                Intent intent = new Intent(this, CommunicationModeSelector.class);
                intent.putExtra("tag", TAG);
                intent.putExtra("set_key_language", "chinese");
                startActivity(intent);
                break;
            case R.id.test_english:
                keyLanguage = "English";
                testEnglish.setTextColor(getColor(R.color.button_bk_disableok));
                chineseEasy.setTextColor(getColor(R.color.text_color1));
                CommunicationModeSelector.runnables.clear();
                CommunicationModeSelector.runnables.add(null);
                Intent intent1 = new Intent(this, CommunicationModeSelector.class);
                intent1.putExtra("tag", TAG);
                intent1.putExtra("set_key_language", "english");
                startActivity(intent1);
                break;
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void showReading(SetKeyLanguageEvent event) {
        String status = event.getStatus();
        if ("1".equals(status)) {
            preferences.edit().putString("key_language", keyLanguage).apply();
            mToast(getString(R.string.set_success));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
