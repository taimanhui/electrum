package com.haobtc.coldwallet.adapter;

import android.view.View;
import android.view.ViewGroup;

import androidx.viewpager.widget.PagerAdapter;

import java.util.List;


public class MyPagerAdapter extends PagerAdapter{

	private List<View> viewList;

	public MyPagerAdapter(List<View> viewList){
		this.viewList = viewList;
	}
	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView((View)object);
	}

	@Override
	public int getCount() {
		return viewList.size();
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		container.addView(viewList.get(position));
		return viewList.get(position);
	}
	@Override
	public float getPageWidth(int position) {
		return 0.85f;
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {

		return view == object;
	}

	@Override
	public int getItemPosition(Object object) {
		return POSITION_NONE;
	}

}
