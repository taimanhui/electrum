package org.haobtc.onekey.ui.dialog.custom;

import android.content.Context;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import com.lxj.xpopup.core.BottomPopupView;
import org.haobtc.onekey.R;

/** @Description: 自定义添加代币弹窗 @Author: peter Qin */
class CustomAddTokenDialog extends BottomPopupView {

    private Unbinder bind;

    @BindView(R.id.image)
    ImageView mImageView;

    @BindView(R.id.token_name)
    TextView tokenNameTV;

    @BindView(R.id.token_address)
    TextView tokenAddressTV;

    @BindView(R.id.cancel_button)
    TextView cancelBtn;

    @BindView(R.id.add_token_btn)
    TextView addTokenBtn;

    public CustomAddTokenDialog(@NonNull Context context) {
        super(context);
    }

    @Override
    protected void onCreate() {
        super.onCreate();
        bind = ButterKnife.bind(this);
    }

    @Override
    protected int getImplLayoutId() {
        return R.layout.dialog_add_token;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        bind.unbind();
    }
}
