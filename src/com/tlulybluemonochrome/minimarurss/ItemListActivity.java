package com.tlulybluemonochrome.minimarurss;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Menu;
import android.view.MenuItem;

/**
 * An activity representing a list of Items. This activity has different
 * presentations for handset and tablet-size devices. On handsets, the activity
 * presents a list of items, which when touched, lead to a
 * {@link ItemDetailActivity} representing item details. On tablets, the
 * activity presents the list of items and item details side-by-side using two
 * vertical panes.
 * <p>
 * The activity makes heavy use of fragments. The list of items is a
 * {@link ItemListFragment} and the item details (if present) is a
 * {@link ItemDetailFragment}.
 * <p>
 * This activity also implements the required {@link ItemListFragment.Callbacks}
 * interface to listen for item selections.
 */
public class ItemListActivity extends Activity implements
		ItemListFragment.Callbacks {

	/**
	 * Whether or not the activity is in two-pane mode, i.e. running on a tablet
	 * device.
	 */
	private boolean mTwoPane;

	SharedPreferences sharedPreferences;

	SectionsPagerAdapter mSectionsPagerAdapter;

	ViewPager mViewPager;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		/* Preferencesからテーマ設定 */
		sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		String thme_preference = sharedPreferences.getString("theme_preference",
				"Light");
		int theme = android.R.style.Theme_Holo_Light;
		if (thme_preference.equals("Light"))
			theme = android.R.style.Theme_Holo_Light;
		else if (thme_preference.equals("Dark"))
			theme = android.R.style.Theme_Holo;
		else if (thme_preference.equals("Transparent"))
			theme = android.R.style.Theme_DeviceDefault_Wallpaper;
		setTheme(theme);
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_item_list);

		if (findViewById(R.id.item_detail_container) != null) {
			// The detail container view will be present only in the
			// large-screen layouts (res/values-large and
			// res/values-sw600dp). If this view is present, then the
			// activity should be in two-pane mode.
			mTwoPane = true;

			// In two-pane mode, list items should be given the
			// 'activated' state when touched.
			((ItemListFragment) getFragmentManager().findFragmentById(
					R.id.item_list)).setActivateOnItemClick(true);
		}

		//sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the app.
		mSectionsPagerAdapter = new SectionsPagerAdapter(getFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				// positionは、表示されているページインデックスです。
				// ここにページ変更後の処理を書きます。
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
				if (mTwoPane)
					((ItemListFragment) getFragmentManager().findFragmentById(
							R.id.item_list)).setActivatedPosition(arg0 - 1);
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {

			}
		});
		mViewPager.setAdapter(mSectionsPagerAdapter);
		mViewPager.setCurrentItem(getIntent().getIntExtra(
				ItemDetailFragment.ARG_ITEM_ID, 0));

		// TODO: If exposing deep links into your app, handle intents here.
	}

	/**
	 * Callback method from {@link ItemListFragment.Callbacks} indicating that
	 * the item with the given ID was selected.
	 */
	@Override
	public void onItemSelected(String tag, String url, int position) {
		mViewPager.setCurrentItem(position + 1);

	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.rss, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		boolean ret = true;
		switch (item.getItemId()) {
		default:
			ret = super.onOptionsItemSelected(item);
			break;
		case R.id.action_settings:
			/* 設定画面呼び出し */
			ret = true;
			Intent intent = new Intent(this, (Class<?>) SettingActivity.class);
			startActivity(intent);
			break;
			
		case R.id.start:
			ret = true;
			startService(new Intent(this,
	                NotificationService.class));
			break;
			
		case R.id.stop:
			ret = true;
			stopService(new Intent(this,
	                NotificationService.class));
			break;
		}
		return ret;
	}

	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */

	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);

		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a DummySectionFragment (defined as a static inner class
			// below) with the page number as its lone argument.

			Bundle arguments = new Bundle();
			arguments.putString(ItemDetailFragment.ARG_ITEM_ID,
					sharedPreferences.getString("URL" + (position - 1), ""));
			// getIntent().getStringExtra(ItemDetailFragment.ARG_ITEM_ID));
			Fragment fragment;

			if (position == 0)
				fragment = new ItemListFragment();
			else
				fragment = new ItemDetailFragment();

			fragment.setArguments(arguments);

			return fragment;
		}

		@Override
		public int getCount() {
			int count = sharedPreferences.getInt("COUNT", 1) + 1;
			return count;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			if (position == 0)
				return "LIST";
			else
				return sharedPreferences.getString("TITLE" + (position - 1),
						"null");

		}

	}
}
