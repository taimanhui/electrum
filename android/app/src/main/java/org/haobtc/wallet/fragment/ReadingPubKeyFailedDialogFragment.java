package org.haobtc.wallet.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.DialogFragment;

import org.haobtc.wallet.R;

import java.util.Objects;

public class ReadingPubKeyFailedDialogFragment extends DialogFragment {
    private Runnable runnable;
    private Activity activity;
    private int errorsId;

    public void setRunnable(Runnable runnable) {
        this.runnable = runnable;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }
    public ReadingPubKeyFailedDialogFragment(@StringRes int errors) {
        this.errorsId = errors;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_reading_exception, container, false);
        view.findViewById(R.id.cancel_sign_fail).setOnClickListener((v) -> dismiss());
        view.findViewById(R.id.retry).setOnClickListener((v) -> {
            activity.runOnUiThread(runnable);
            dismiss();
        });
        TextView errorMessage = view.findViewById(R.id.error_message);
        errorMessage.setText(errorsId);
        Window window = Objects.requireNonNull(getDialog()).getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(R.color.transparent);
            window.getDecorView().setPadding(0, 0, 0, 0);
            WindowManager.LayoutParams wlp = window.getAttributes();
            wlp.gravity = Gravity.BOTTOM;
            wlp.width = WindowManager.LayoutParams.MATCH_PARENT;
            wlp.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(wlp);
        }
        return view;
    }
}