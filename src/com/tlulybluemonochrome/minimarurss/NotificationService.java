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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Xml;
import android.widget.Toast;

/**
 * 更新確認して通知するサービス
 * 
 * @author k9000
 * 
 */
public class NotificationService extends IntentService {

	static ArrayList<RssItem> oldlist;

	int count;

	static final int maxSize = 10 * 1024 * 1024;

	public NotificationService(final String name) {
		super(name);
		// TODO 自動生成されたコンストラクター・スタブ
	}

	// アラームマネージャ用コンストラクタ
	public NotificationService() {
		super("NotificationService");
	}

	final static String TAG = "test";

	@Override
	protected void onHandleIntent(final Intent intent) {
		// TODO 自動生成されたメソッド・スタブ

		RssMessageNotification.cancel(getApplicationContext(), -1);
		RssMessageNotification.titlenotify(getApplicationContext(),
				"minimaruRSS", "更新中", "更新中", -1);

		final SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(this);
		count = sharedPreferences.getInt("COUNT", 0);
		final boolean picChecked = sharedPreferences.getBoolean("pic_switch",
				true);

		final ArrayList<RssItem> arraylist = new ArrayList<RssItem>();

		ArrayList<RssFeed> urilist = new ArrayList<RssFeed>();

		try {// URIセーブデータオープン
			FileInputStream fis = openFileInput("SaveData.txt");
			ObjectInputStream ois = new ObjectInputStream(fis);
			urilist = (ArrayList<RssFeed>) ois.readObject();
			ois.close();
		} catch (Exception e) {
			Toast.makeText(this, "error1", Toast.LENGTH_SHORT).show();
		}

		try {// 既読セーブデータオープン
			FileInputStream fis = openFileInput("SaveData.dat");
			ObjectInputStream ois = new ObjectInputStream(fis);
			oldlist = (ArrayList<RssItem>) ois.readObject();
			ois.close();
		} catch (Exception e) {
			oldlist = null;
		}

		// 全URLチェック
		for (int i = 0; i < urilist.size(); i++) {

			if (urilist.get(i).getNoti()) {// Notifications設定確認
				try {
					final InputStream is = new URL(urilist.get(i).getUrl())
							.openConnection().getInputStream();
					arraylist.addAll(parseXml(is, urilist.get(i).getTag(),
							urilist.get(i).getTitle()));

				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}

		// 未読記事通知
		for (int i = 0; i < arraylist.size(); i++) {
			if (arraylist.get(i).getTag() != 0) {
				RssMessageNotification.notify(
						getApplicationContext(),
						arraylist.get(i).getTitle(),
						arraylist.get(i).getTitle() + "\n"
								+ arraylist.get(i).getText(),
						arraylist.get(i).getUrl(),
						count++,
						makeImage(
								arraylist.get(i).getImage(),
								Picuture(arraylist.get(i).getTag(),
										R.drawable.ic_launcher), picChecked),
						arraylist.get(i).getPage(), false);
				try {// 通知の間を置く
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			}
		}

		if (!arraylist.isEmpty()) {
			try {// 既読判定書き込み
				FileOutputStream fos = openFileOutput("SaveData.dat",
						MODE_PRIVATE);
				ObjectOutputStream oos = new ObjectOutputStream(fos);
				oos.writeObject(arraylist);
				oos.close();
			} catch (Exception e) {
				Log.d(TAG, "Error");
			}
		}

		final Editor editor = sharedPreferences.edit();
		editor.putInt("COUNT", count);
		editor.commit();

	}

	// XMLをパースする
	public static ArrayList<RssItem> parseXml(final InputStream is,
			final int color, final String page) throws IOException,
			XmlPullParserException {
		final XmlPullParser parser = Xml.newPullParser();
		final ArrayList<RssItem> list = new ArrayList<RssItem>();
		try {
			parser.setInput(is, null);
			int eventType = parser.getEventType();
			RssItem currentItem = null;
			int i = 0;
			while (eventType != XmlPullParser.END_DOCUMENT && i < 5) {// 通知数制限
				String tag = null;
				switch (eventType) {
				case XmlPullParser.START_TAG:
					tag = parser.getName();
					if (tag.equals("item") || tag.equals("entry")) {
						currentItem = new RssItem();
						currentItem.setTag(color);
					} else if (currentItem != null) {
						if (tag.equals("title")) {
							currentItem.setTitle(parser.nextText().replaceAll(
									"(&#....;|&....;|&...;)", ""));// タグ除去;
						} else if (tag.equals("link")) {
							final String link = parser.nextText();
							if (link != "") {
								currentItem.setUrl(link);
							} else {
								final String rel = parser.getAttributeValue(
										null, "rel");
								final String herf = parser.getAttributeValue(
										null, "href");
								if (rel.equals("alternate")) {
									currentItem.setUrl(herf);
								}
							}
						} else if (tag.equals("description")
								|| tag.equals("summary")) {
							String buf = parser.nextText();
							currentItem.setImage(StripImageTags(buf));
							currentItem
									.setText(buf
											.replaceAll(
													"(<.+?>|\r\n|\n\r|\n|\r|&#....;|&....;|&...;|&..;)",
													""));// タグと改行除去
						} else if (tag.equals("encoded")) {
							currentItem.setImage(StripImageTags(parser
									.nextText()));
						}
					}
					break;
				case XmlPullParser.END_TAG:
					tag = parser.getName();
					if ((tag.equals("item") || tag.equals("entry"))
							&& removePR(currentItem)) {
						if (Serch(currentItem)) {
							currentItem.setTag(0);
						}
						currentItem.setPage(page);
						list.add(currentItem);
						i++;
					}
					break;
				}
				eventType = parser.next();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;

	}

	private static String StripImageTags(String str) {
		final Pattern o = Pattern.compile("<img.*?(jpg|png).*?>");
		final Pattern p = Pattern.compile("http.*?(jpg|png)");
		final Pattern q = Pattern.compile("//.*?(jpg|png)");

		String matchstr = null;

		Matcher x = o.matcher(str);
		if (x.find()) {
			str = x.group();

			x = p.matcher(str);
			Matcher y = q.matcher(str);

			if (x.find()) {
				matchstr = x.group();
			} else if (y.find()) {
				matchstr = "http:" + y.group();
			} else {
				matchstr = null;
			}
			return matchstr;
		}

		return null;

	}

	// PR削除
	private static boolean removePR(RssItem currentItem) {
		final String title = currentItem.getTitle();
		final String regex = "^PR";
		final Pattern p = Pattern.compile(regex);
		final Matcher m = p.matcher(title);

		return !m.find();
	}

	// 未読チェック
	public static boolean Serch(RssItem item) {
		if (oldlist == null)
			return false;

		for (int i = 0; i < oldlist.size(); i++) {
			if (item.getUrl().equals(oldlist.get(i).getUrl()))
				return true;
		}

		return false;
	}

	// アイコン生成
	public Bitmap Picuture(final int color, final int resource) {

		final Bitmap picture = BitmapFactory.decodeResource(getResources(),
				resource);
		final Bitmap mBmp = picture.copy(picture.getConfig(), true);

		final int width = mBmp.getWidth();
		final int height = mBmp.getHeight();
		final int[] pixels = new int[width * height];
		mBmp.getPixels(pixels, 0, width, 0, 0, width, height);
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				if (pixels[x + y * width] > 0xFFFFFFFF) {
					pixels[x + y * width] = 0;
				} else if (pixels[x + y * width] >= 0xFF33B5E5) {
					pixels[x + y * width] = color;
				} else if (pixels[x + y * width] >= 0xCC33B5E5) {
					pixels[x + y * width] = color - 0x33000000;
				} else if (pixels[x + y * width] >= 0x9933B5E5) {
					pixels[x + y * width] = color - 0x66000000;
				} else if (pixels[x + y * width] >= 0x6633B5E5) {
					pixels[x + y * width] = color - 0x99000000;
				} else if (pixels[x + y * width] >= 0x3333B5E5) {
					pixels[x + y * width] = color - 0xCC000000;
				} else {
					pixels[x + y * width] = 0;
				}
			}
		}
		mBmp.setPixels(pixels, 0, width, 0, 0, width, height);
		return mBmp;
	}

	private Bitmap makeImage(final String image, final Bitmap base,
			boolean picChecked) {
		if (picChecked) {
			Bitmap bitmap;
			try {
				final URL image_url = new URL(image);
				final InputStream is = (InputStream) image_url.getContent();
				bitmap = BitmapFactory.decodeStream(is);
				is.close();
				final Resources res = getBaseContext().getResources();
				bitmap = Bitmap
						.createScaledBitmap(
								bitmap,
								(int) res
										.getDimension(android.R.dimen.notification_large_icon_width),
								(int) res
										.getDimension(android.R.dimen.notification_large_icon_height),
								false);
			} catch (IOException e) {
				e.printStackTrace();
				return base;
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
				return base;
			}
			return bitmap;
		}
		return base;

	}

	// Service終了
	@Override
	public void onDestroy() {
		super.onDestroy();
		RssMessageNotification.cancel(getApplicationContext(), -1);
		RssMessageNotification.titlenotify(getApplicationContext(),
				"minimaruRSS", "タップして更新", "更新完了", -1);
	}

}
