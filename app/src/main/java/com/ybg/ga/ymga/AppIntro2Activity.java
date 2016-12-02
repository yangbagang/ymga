/**
 * 
 */
package com.ybg.ga.ymga;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.ybg.ga.ymga.R;
import com.ybg.ga.ymga.ga.MainActivity;

import java.util.ArrayList;
import java.util.List;

/**
 * @author 杨拔纲
 *
 */
public class AppIntro2Activity extends Activity {

	private ViewPager viewPager;

	private List<View> pageViews;

	private ImageView imageView;

	private ImageView[] imageViews;

	// 包裹滑动图片LinearLayout
	private ViewGroup main;

	// 包裹小圆点的LinearLayout
	private ViewGroup group;

	@SuppressLint("InflateParams")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 设置无标题窗口
		getWindow().requestFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		LayoutInflater inflater = getLayoutInflater();
		pageViews = new ArrayList<View>();
		pageViews.add(inflater.inflate(R.layout.intro_page_0, null));
		pageViews.add(inflater.inflate(R.layout.intro_page_1, null));
		pageViews.add(inflater.inflate(R.layout.intro_page_2, null));
		pageViews.add(inflater.inflate(R.layout.intro_page_3, null));

		imageViews = new ImageView[pageViews.size()];
		main = (ViewGroup) inflater.inflate(R.layout.intro_page_main, null);

		group = (ViewGroup) main.findViewById(R.id.viewGroup);
		viewPager = (ViewPager) main.findViewById(R.id.guidePages);

		for (int i = 0; i < pageViews.size(); i++) {
			imageView = new ImageView(this);
			imageView.setLayoutParams(new LayoutParams(20, 20));
			imageView.setPadding(20, 0, 20, 0);
			imageViews[i] = imageView;

			if (i == 0) {
				// 默认选中第一张图片
				imageViews[i]
						.setBackgroundResource(R.mipmap.page_indicator_focused);
			} else {
				imageViews[i].setBackgroundResource(R.mipmap.page_indicator);
			}

			group.addView(imageViews[i]);
		}

		setContentView(main);

		viewPager.setAdapter(new GuidePageAdapter());
		viewPager.setOnPageChangeListener(new GuidePageChangeListener());
	}
	
	public void enterMainActivity(View view) {
		Intent intent = new Intent(this, MainActivity.class);
		startActivity(intent);
		finish();
	}

	// 指引页面数据适配器
	class GuidePageAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			return pageViews.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public int getItemPosition(Object object) {
			return super.getItemPosition(object);
		}

		@Override
		public void destroyItem(View arg0, int arg1, Object arg2) {
			((ViewPager) arg0).removeView(pageViews.get(arg1));
		}

		@Override
		public Object instantiateItem(View arg0, int arg1) {
			((ViewPager) arg0).addView(pageViews.get(arg1));
			return pageViews.get(arg1);
		}

		@Override
		public void restoreState(Parcelable arg0, ClassLoader arg1) {

		}

		@Override
		public Parcelable saveState() {
			return null;
		}

		@Override
		public void startUpdate(View arg0) {

		}

		@Override
		public void finishUpdate(View arg0) {

		}
	}

	// 指引页面更改事件监听器
	class GuidePageChangeListener implements OnPageChangeListener {

		@Override
		public void onPageScrollStateChanged(int arg0) {

		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {

		}

		@Override
		public void onPageSelected(int arg0) {
			imageViews[arg0]
					.setBackgroundResource(R.mipmap.page_indicator_focused);
			for (int i = 0; i < imageViews.length; i++) {
				if (arg0 != i) {
					imageViews[i]
							.setBackgroundResource(R.mipmap.page_indicator);
				}
			}
			if(arg0 == pageViews.size() - 1) {
				findViewById(R.id.enterMainButton).setVisibility(View.VISIBLE);
			} else {
				findViewById(R.id.enterMainButton).setVisibility(View.GONE);
			}
		}
	}
	
}
