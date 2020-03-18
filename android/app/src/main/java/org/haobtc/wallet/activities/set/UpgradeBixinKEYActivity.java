package org.haobtc.wallet.activities.set;

import android.os.AsyncTask;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import org.haobtc.wallet.R;
import org.haobtc.wallet.activities.base.BaseActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class UpgradeBixinKEYActivity extends BaseActivity {

    @BindView(R.id.img_back)
    ImageView imgBack;
    @BindView(R.id.tet_test)
    TextView tetTest;
    @BindView(R.id.progressUpgrade)
    ProgressBar progressUpgrade;
    @BindView(R.id.tetUpgradeTest)
    TextView tetUpgradeTest;
    @BindView(R.id.tetUpgradeNum)
    TextView tetUpgradeNum;
    @BindView(R.id.imgdhksjks)
    ImageView imgdhksjks;
    private MyTask mTask;
    private boolean ifOnclick = false;

    private class MyTask extends AsyncTask<String, Integer, String> {
        @Override
        protected void onPreExecute() {
            tetUpgradeTest.setText(getString(R.string.upgradeing));

        }

        @Override
        protected String doInBackground(String... params) {
            try {
                int count = 0;
                int length = 1;
                while (count < 99) {
                    count += length;
                    // 可调用publishProgress（）显示进度, 之后将执行onProgressUpdate（）
                    publishProgress(count);
                    // 模拟耗时任务
                    Thread.sleep(50);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... progresses) {
            progressUpgrade.setProgress(progresses[0]);
            tetUpgradeNum.setText(String.format("%s%%", String.valueOf(progresses[0])));

        }

        @Override
        protected void onPostExecute(String result) {
            mIntent(UpgradeFinishedActivity.class);
            finish();
        }

        @Override
        protected void onCancelled() {
            tetUpgradeTest.setText(getString(R.string.cancled));
            progressUpgrade.setProgress(0);

        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_upgrade_bixin_k_e_y;
    }

    @Override
    public void initView() {
        ButterKnife.bind(this);
        mTask = new MyTask();

    }

    @Override
    public void initData() {

    }

    @OnClick({R.id.img_back, R.id.imgdhksjks, R.id.tet_test})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.img_back:
                finish();
                break;
            case R.id.imgdhksjks:
                if (!ifOnclick){
                    mTask.execute();
                    tetTest.setText(getString(R.string.fit_key_warning));
                    ifOnclick = true;
                }
                break;
            case R.id.tet_test:
                if (ifOnclick){
                    mTask.cancel(true);
                    ifOnclick = false;
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mTask.cancel(true);
    }
}
