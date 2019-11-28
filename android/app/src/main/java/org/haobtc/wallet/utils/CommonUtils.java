package org.haobtc.wallet.utils;

import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import org.haobtc.wallet.R;

import java.lang.reflect.Field;

public class CommonUtils {

    public static  void enableToolBar(AppCompatActivity appCompatActivity,  int resid) {
        TextView textView = appCompatActivity.findViewById(R.id.title_template);
        if (resid == 0) {
            textView.setText("");
        } else {
            textView.setText(resid);
        }
        // my_child_toolbar is defined in the layout file
        appCompatActivity.setSupportActionBar(appCompatActivity.findViewById(R.id.tb));

        // Get a support ActionBar corresponding to this toolbar
        ActionBar ab = appCompatActivity.getSupportActionBar();
        // Enable the Up button
        if (ab != null) {
            ab.setDisplayHomeAsUpEnabled(true);
        }
    }

    public static void setNumberPickerDividerColor(AppCompatActivity appCompatActivity, NumberPicker numberPicker) {
        Field[] pickerFields = NumberPicker.class.getDeclaredFields();
        for (Field pf : pickerFields) {
            if (pf.getName().equals("mSelectionDivider")) {
                pf.setAccessible(true);
                try {
                    //设置分割线的颜色值 透明
                    pf.set(numberPicker, new ColorDrawable(appCompatActivity.getResources().getColor(R.color.separation_line)));
                } catch (IllegalArgumentException | Resources.NotFoundException | IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
        // 分割线高度
        for (Field pf2 : pickerFields) {
            if (pf2.getName().equals("mSelectionDividerHeight")) {
                pf2.setAccessible(true);
                try {
                    int result = 3;
                    pf2.set(numberPicker, result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            }
        }

    }
}
