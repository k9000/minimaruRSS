/*
 * Copyright (C) 2013 k9000
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tlulybluemonochrome.minimarurss;

import java.util.ArrayList;

import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher;
import uk.co.senab.actionbarpulltorefresh.library.viewdelegates.AbsListViewDelegate;
import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Loader;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ListView;

/**
 * 更新ページ表示用フラグメント
 * 
 * @author k9000
 * 
 */
public class ItemDetailFragment extends Fragment
		implements
		LoaderCallbacks<ArrayList<RssItem>>,
		uk.co.senab.actionbarpulltorefresh.library.PullToRefreshAttacher.OnRefreshListener {

	boolean mFlag = false;

	private ListView mListView;

	/**
	 * The fragment argument representing the item ID that this fragment
	 * represents.
	 */
	public static final String ARG_ITEM_ID = "item_id";

	/**
	 * The fragment's current callback object, which is notified of list item
	 * clicks.
	 */
	private Callbacks mCallbacks = sDummyCallbacks;

	/**
	 * The current activated item position. Only used on tablets.
	 */
	private int mActivatedPosition = ListView.INVALID_POSITION;

	private ArrayList<RssItem> list;

	private PullToRefreshAttacher mPullToRefreshAttacher;

	/**
	 * A callback interface that all activities containing this fragment must
	 * implement. This mechanism allows activities to be notified of item
	 * selections.
	 */
	public interface Callbacks {
		/**
		 * Callback for when an item has been selected.
		 * 
		 * @param position
		 */
		public void onAdapterSelected(final int tag, final String url,
				final int position);

		public void onRefreshList(String string, ArrayList<RssItem> arg1);

	}

	/**
	 * A dummy implementation of the {@link Callbacks} interface that does
	 * nothing. Used only when this fragment is not attached to an activity.
	 */
	private static Callbacks sDummyCallbacks = new Callbacks() {
		@Override
		public void onAdapterSelected(final int tag, final String url,
				final int position) {
		}

		@Override
		public void onRefreshList(String string, ArrayList<RssItem> arg1) {
			// TODO 自動生成されたメソッド・スタブ

		}

	};

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(final LayoutInflater inflater,
			final ViewGroup container, final Bundle savedInstanceState) {
		final View rootView = inflater.inflate(R.layout.main, container, false);

		mListView = (ListView) rootView.findViewById(R.id.listview);

		mListView.setAdapter(null);

		list = (ArrayList<RssItem>) getArguments().getSerializable("LIST");

		if (getArguments().getSerializable("LIST") != null) {
			mListView
					.setAdapter(new CustomDetailAdapter(getActivity(), 0, list));

			// 引っ張って更新
			/*mPullToRefreshAttacher = ((ItemListActivity) getActivity())
					.getPullToRefreshAttacher();
			mPullToRefreshAttacher.addRefreshableView(mListView, this);*/
			mPullToRefreshAttacher = ((ItemListActivity) getActivity())
					.getPullToRefreshAttacher();
			PullToRefreshAttacher.ViewDelegate handler = new AbsListViewDelegate();
	        mPullToRefreshAttacher.addRefreshableView(mListView, handler, this);

		}

		mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(final AdapterView<?> arg0, final View arg1,
					final int position, final long id) {
				// TODO 自動生成されたメソッド・スタブ
				mCallbacks.onAdapterSelected(position, list.get(position)
						.getUrl(), position);

			}

		});

		// getLoaderManager().initLoader(0, getArguments(), this);

		return rootView;
	}

	@Override
	public void onAttach(final Activity activity) {
		super.onAttach(activity);

		// Activities containing this fragment must implement its callbacks.
		if (!(activity instanceof Callbacks)) {
			throw new IllegalStateException(
					"Activity must implement fragment's callbacks.");
		}

		mCallbacks = (Callbacks) activity;
	}

	@Override
	public void onDetach() {
		super.onDetach();

		// Reset the active callbacks interface to the dummy implementation.
		mCallbacks = sDummyCallbacks;
	}

	// ASyncTaskLoader始動
	@Override
	public Loader<ArrayList<RssItem>> onCreateLoader(final int wait,
			final Bundle args) {
		final RssParserTaskLoader appLoader = new RssParserTaskLoader(
				getActivity(), args.getString(ItemDetailFragment.ARG_ITEM_ID),
				wait, args.getInt("COLOR"), args.getString("TITLE"));
		appLoader.forceLoad();
		return appLoader;
	}

	@Override
	public void onLoadFinished(final Loader<ArrayList<RssItem>> arg0,
			final ArrayList<RssItem> arg1) {
		if (arg1.isEmpty()) {// 失敗時
			// Toast.makeText(getActivity(), "error",
			// Toast.LENGTH_SHORT).show();
		} else {
			// リスト更新
			mListView
					.setAdapter(new CustomDetailAdapter(getActivity(), 0, arg1));
			mCallbacks.onRefreshList(
					this.getArguments().getString(
							ItemDetailFragment.ARG_ITEM_ID), arg1);
			list = arg1;
		}
		if (mFlag) {// 引っ張って更新したとき
			// mListView.completeRefreshing();
			mPullToRefreshAttacher.setRefreshComplete();
			LayoutAnimationController anim = getListCascadeAnimation();
			mListView.setLayoutAnimation(anim);
			mFlag = false;
		}

	}

	@Override
	public void onLoaderReset(final Loader<ArrayList<RssItem>> arg0) {

	}

	private static LayoutAnimationController getListCascadeAnimation() {
		final AnimationSet set = new AnimationSet(true);

		Animation animation = new AlphaAnimation(0.0f, 1.0f);
		animation.setDuration(50);
		set.addAnimation(animation);

		animation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 1.0f,
				Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
				-1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
		animation.setDuration(100);
		set.addAnimation(animation);

		final LayoutAnimationController controller = new LayoutAnimationController(
				set, 0.5f);
		return controller;
	}

	@Override
	public void onDestroyView() {
		getLoaderManager().destroyLoader(0);
		mListView.setAdapter(null);
		// mListView.setOnRefreshListener(null);
		mListView = null;
		list = null;
		super.onDestroyView();

	}

	@Override
	public void onRefreshStarted(View view) {
		mFlag = true;
		getLoaderManager()
				.restartLoader(500, ItemDetailFragment.this.getArguments(),
						ItemDetailFragment.this);
	}
}
