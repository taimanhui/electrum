package org.haobtc.wallet.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.text.Editable;
import android.util.Log;
import android.view.View;

import org.haobtc.wallet.R;

public class NumKeyboardUtil {
    private KeyboardView keyboardView;
    private Keyboard k;// num keyboard
    private PasswordInputView ed;

    @SuppressLint("ResourceAsColor")
    public NumKeyboardUtil(Activity act, Context ctx, PasswordInputView edit) {
        this.ed = edit;
        k = new Keyboard(ctx, R.xml.number);
        keyboardView = (KeyboardView) act.findViewById(R.id.keyboard_view);
        keyboardView.setKeyboard(k);
        keyboardView.setEnabled(true);
        keyboardView.setPreviewEnabled(false);
        keyboardView.setOnKeyboardActionListener(listener);
//        keyboardView.setBackgroundColor(Color.parseColor("#f1f1f1"));

    }

    private KeyboardView.OnKeyboardActionListener listener = new KeyboardView.OnKeyboardActionListener() {
        @Override
        public void swipeUp() {
        }

        @Override
        public void swipeRight() {
        }

        @Override
        public void swipeLeft() {
        }

        @Override
        public void swipeDown() {
        }

        @Override
        public void onText(CharSequence text) {
        }

        @Override
        public void onRelease(int primaryCode) {
        }

        @Override
        public void onPress(int primaryCode) {
        }

        //The codes of some special operation keys are fixed, such as completion, fallback, etc
        @Override
        public void onKey(int primaryCode, int[] keyCodes) {
            Editable editable = ed.getEditableText();
            int start = ed.getSelectionStart();
            if (primaryCode == Keyboard.KEYCODE_DELETE) {// exit
                if (editable != null && editable.length() > 0) {
                    if (start > 0) {
                        editable.delete(start - 1, start);
                    }
                }
            } else if (primaryCode == Keyboard.KEYCODE_CANCEL) {// finish
                hideKeyboard();
            } else if (primaryCode == 48) {
                Log.i("Disable", "zero is useless" );
            } else { //The number to be entered is now in the edit box
                editable.insert(start, Character.toString((char) primaryCode));
            }
        }
    };

    public void showKeyboard() {
        keyboardView.setVisibility(View.VISIBLE);
    }

    public void hideKeyboard() {
        keyboardView.setVisibility(View.GONE);
    }

    public int getKeyboardVisible() {
        return keyboardView.getVisibility();
    }
}