package com.haobtc.coldwallet.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.haobtc.coldwallet.R;

import java.util.ArrayList;
import java.util.List;
/***
 * 引导页面
 * */
public class GuideActivity extends Activity implements ViewPager.OnPageChangeListener {
    private ViewPager viewPager;
    private List<View>  viewList = new ArrayList<>();
    private ImageView[] dots;
    private int[] ids = {R.id.iv1, R.id.iv2};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.boot_page);
        initViews();
        initDots();
        initCallback();

    }
    private void initViews() {
        viewPager = findViewById(R.id.vp);
        LayoutInflater inflater = LayoutInflater.from(this);
        viewList.add(inflater.inflate(R.layout.boot_page_item1, null));
        viewList.add(inflater.inflate(R.layout.boot_page_item2, null));
        viewPager.setAdapter(new PagerAdapter() {
            @Override
            public int getCount() {
                return viewList.size();
            }

            @Override
            public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
                return view == object;
            }

            @NonNull
            @Override
            public Object instantiateItem(@NonNull ViewGroup container, int position) {
                container.addView(viewList.get(position));
                return viewList.get(position);
            }

            @Override
            public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
                container.removeView(viewList.get(position));
            }
        });
        viewPager.addOnPageChangeListener(this);
    }
    private void initDots() {
        dots = new ImageView[viewList.size()];
        for (int i = 0; i < viewList.size(); i++) {
            dots[i] = findViewById(ids[i]);
        }
    }
    private void initCallback() {
    //处理接收到的消息的方法
        new Handler(arg0 -> {
        //实现页面跳转
        startNewPage();
        return false;
    }).sendEmptyMessageDelayed(0, 3000);

}

    private void startNewPage() {
        Intent intent = new Intent(this, CreateWalletActivity.class);
        startActivity(intent);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        for (int i = 0; i < viewList.size(); i++) {
            if (position == i) {
                // 当前滑动到的页面设置点为蓝色的点
                dots[i].setImageResource(R.drawable.boot_page_bluedot);
            } else {
                // 没被选中时灰色的点
                dots[i].setImageResource(R.drawable.boot_page_greydot);
            }
        }

    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
