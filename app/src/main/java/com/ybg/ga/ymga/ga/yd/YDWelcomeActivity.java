/**
 * 
 */
package com.ybg.ga.ymga.ga.yd;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.LocalActivityManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.Window;
import android.widget.RadioGroup;

import com.ybg.ga.ymga.R;
import com.ybg.ga.ymga.ga.activity.AboutActivity;
import com.ybg.ga.ymga.ga.activity.OnResultAvailableListener;

/**
 * @author 杨拔纲
 * 
 */
@SuppressWarnings("deprecation")
public class YDWelcomeActivity extends Activity {

	private RadioGroup radioGroup;

	// 页卡内容
	private ViewPager mPager;
	// Tab页面列表
	private List<View> listViews;
	// 当前页卡编号
	private LocalActivityManager manager = null;

	private MyPagerAdapter mpAdapter = null;
	private int index;

	private View view0 = null;
	private View view1 = null;
	private View view2 = null;

	// 更新intent传过来的值
	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {

	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		manager.removeAllActivities();
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (getIntent() != null) {
			index = getIntent().getIntExtra("index", 0);
			mPager.setCurrentItem(index);
			setIntent(null);
		} else {
			if (index < 2) {
				index = index + 1;
				mPager.setCurrentItem(index);
				index = index - 1;
				mPager.setCurrentItem(index);

			} else if (index == 2) {
				index = index - 1;
				mPager.setCurrentItem(index);
				index = index + 1;
				mPager.setCurrentItem(index);
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.yd_container);

		mPager = (ViewPager) findViewById(R.id.vPager);
		manager = new LocalActivityManager(this, true);
		manager.dispatchCreate(savedInstanceState);

		view0 = getIntentView("A", new Intent(YDWelcomeActivity.this,
				YDMainActivity.class));
		view1 = getIntentView("B", new Intent(YDWelcomeActivity.this,
				YdHistoryActivity.class));
		view2 = getIntentView("C", new Intent(YDWelcomeActivity.this,
				AboutActivity.class));

		InitViewPager();
		radioGroup = (RadioGroup) this.findViewById(R.id.yd_tab_btns);
		radioGroup
				.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
					public void onCheckedChanged(RadioGroup group, int checkedId) {
						switch (checkedId) {

						case R.id.ydMainTab:
							index = 0;
							listViews.set(0, view0);
							mpAdapter.notifyDataSetChanged();
							mPager.setCurrentItem(0);
							break;

						case R.id.ydHistoryTab:
							index = 1;
							listViews.set(1, view1);
							mpAdapter.notifyDataSetChanged();
							mPager.setCurrentItem(1);
							break;

						case R.id.ydDiscoverTab:
							index = 2;
							listViews.set(2, view2);
							mpAdapter.notifyDataSetChanged();
							mPager.setCurrentItem(2);
							break;

						default:
							break;
						}
					}
				});
	}

	/**
	 * 初始化ViewPager
	 */
	private void InitViewPager() {
		listViews = new ArrayList<View>();
		mpAdapter = new MyPagerAdapter(listViews);
		listViews.add(view0);
		listViews.add(view1);
		listViews.add(view2);
		mPager.setOffscreenPageLimit(1);
		mPager.setAdapter(mpAdapter);
		mPager.setCurrentItem(0);
		mPager.setOnPageChangeListener(new MyOnPageChangeListener());
	}

	/**
	 * ViewPager适配器
	 */
	public class MyPagerAdapter extends PagerAdapter {
		public List<View> mListViews;

		public MyPagerAdapter(List<View> mListViews) {
			this.mListViews = mListViews;
		}

		@Override
		public void destroyItem(View arg0, int arg1, Object arg2) {
			((ViewPager) arg0).removeView(mListViews.get(arg1));
		}

		@Override
		public void finishUpdate(View arg0) {
		}

		@Override
		public int getCount() {
			return mListViews.size();
		}

		@Override
		public Object instantiateItem(View arg0, int arg1) {
			((ViewPager) arg0).addView(mListViews.get(arg1), 0);
			return mListViews.get(arg1);
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == (arg1);
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
	}

	/**
	 * 页卡切换监听，ViewPager改变同样改变TabHost内容
	 */
	public class MyOnPageChangeListener implements OnPageChangeListener {

		public void onPageSelected(int arg0) {
			manager.dispatchResume();
			switch (arg0) {
			case 0:
				index = 0;
				radioGroup.check(R.id.ydMainTab);
				listViews.set(0, view0);
				mpAdapter.notifyDataSetChanged();
				break;
			case 1:
				index = 1;
				radioGroup.check(R.id.ydHistoryTab);
				listViews.set(1, view1);
				mpAdapter.notifyDataSetChanged();
				break;
			case 2:
				index = 2;
				radioGroup.check(R.id.ydDiscoverTab);
				listViews.set(2, view2);
				mpAdapter.notifyDataSetChanged();
				break;
			}
		}

		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		public void onPageScrollStateChanged(int arg0) {
		}
	}

	private View getIntentView(String id, Intent intent) {
		return manager.startActivity(id, intent).getDecorView();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		String[] ids = { "A", "B", "C" };
		OnResultAvailableListener currentActivity = (OnResultAvailableListener) manager
				.getActivity(ids[index]);
		currentActivity.setActivityResult(requestCode, resultCode, data);
	}

}
