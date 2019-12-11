package org.haobtc.wallet.activities.base;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.Toast;
import com.gyf.immersionbar.ImmersionBar;
import org.haobtc.wallet.R;
import org.haobtc.wallet.utils.MyDialog;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class BaseActivity extends AppCompatActivity {
    private String strUTF8;
    private MyDialog myDialog;
    private Bitmap bitmap;
    private String filed1utf;
    private String nowTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutId());
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); //Disable horizontal screen
        mBinitState();
        initView();
        initData();


    }

    public abstract int getLayoutId();

    public abstract void initView();

    public abstract void initData();

    //activity intent
    public void mIntent(Class mActivity) {
        Intent intent = new Intent(this, mActivity);
        startActivity(intent);
    }


    //toast short
    public void mToast(String str) {
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
    }

    //toast long
    public void mlToast(String str) {
        Toast.makeText(this, str, Toast.LENGTH_LONG).show();
    }

    //UTF-8 to text
    public String mUTFTtoText(String str) {
        try {
            strUTF8 = URLDecoder.decode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return strUTF8;
    }

    //text  to  utf-8
    public String mmTextToutf8(String strFiled) {
        String filed1HangyeUTF = null;
        try {
            filed1HangyeUTF = new String(strFiled.getBytes("UTF-8"));
            filed1utf = URLEncoder.encode(filed1HangyeUTF, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return filed1utf;

    }

    //get now time
    public String mGetNowDatetime() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date curDate = new Date(System.currentTimeMillis());
        nowTime = formatter.format(curDate);
        return nowTime;
    }
    //judge mobile is wrong or right
    public boolean isMobileNO(String mobiles) {
        Pattern p = Pattern
                .compile("^((17[0-9])|(14[0-9])|(13[0-9])|(15[^4,\\D])|(16[0-9])|(18[0,5-9]))\\d{8}$");
        Matcher m = p.matcher(mobiles);

        return m.matches();
    }

    //get versionCode
    public int getLocalVersion(Context ctx) {
        int localVersion = 0;
        try {
            PackageInfo packageInfo = ctx.getApplicationContext()
                    .getPackageManager()
                    .getPackageInfo(ctx.getPackageName(), 0);
            localVersion = packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return localVersion;
    }

    /**
     * get  versionName
     *
     * @param context
     * @return
     */
    public String mGetVersionName(Context context) {
        PackageManager packageManager = context.getPackageManager();
        PackageInfo packageInfo;
        String versionName = "";
        try {
            packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            versionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }

    //图片压缩
    public Bitmap mPicYasuo(String imgPath) {
        /**
         * 压缩图片
         */
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        options.inSampleSize = 2;
        options.inJustDecodeBounds = false;
        bitmap = BitmapFactory.decodeFile(imgPath, options);
        return bitmap;

    }

    /**
     * Set transparent immersion bar
     */
    public void mInitState() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //transparent immersion bar
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            //Transparent navigation bar will cause virtual buttons to disappear (for example, Huawei)
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        }
    }

    /**
     * Set transparent immersion bar:white text
     */
    public void mWhiteinitState() {
//        LinearLayout top_manger=findViewById(R.id.top_manger);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            //透明状态栏
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//            //透明导航栏

        }
    }

    /**
     * Set transparent immersion bar : white backgrand black text
     */
    public void mBinitState() {
        ImmersionBar.with(this).keyboardEnable(false).statusBarDarkFont(true, 0f).navigationBarColor(R.color.button_bk_ddake).init();


        //other one write
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
//            getWindow().setStatusBarColor(Color.WHITE);
//            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
//        }

    }

    //Transformation  price
    public String mNumToPrice(String string) {
        // string type price to double
        Double numDouble = Double.parseDouble(string);
        // Want to convert to the currency format of the specified country 
        NumberFormat format = NumberFormat.getCurrencyInstance(Locale.CHINA);
        // Return the string type of the converted currency 
        String numString = format.format(numDouble);
        return numString;
    }

}
