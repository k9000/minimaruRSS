package com.tlulybluemonochrome.minimarurss;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import com.tlulybluemonochrome.minimarurss.dummy.DummyContent;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import android.util.Xml;

public class NotificationService extends IntentService {

	public NotificationService(String name) {
		super(name);
		// TODO 自動生成されたコンストラクター・スタブ
	}

	public NotificationService() {
		super("NotificationService");
	}

	final static String TAG = "test";
	ArrayList<DummyContent.DummyItem> arraylist;

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO 自動生成されたメソッド・スタブ
		arraylist = null;
		
		RssMessageNotification.notify(getApplicationContext(), "更新中","更新中","", 0);

		try {
			URL url = new URL(
					"http://news.google.com/news?hl=ja&ned=us&ie=UTF-8&oe=UTF-8&output=rss");
			InputStream is = url.openConnection().getInputStream();
			arraylist = parseXml(is);
		} catch (Exception e) {
			e.printStackTrace();
		}

		for (int i = 0; i < arraylist.size(); i++) {

			RssMessageNotification.notify(getApplicationContext(), arraylist
					.get(i).getTitle(), arraylist.get(i).getTag(), arraylist
					.get(i).getUrl(), i);
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
			}
		}
	}

	// XMLをパースする
	public ArrayList<DummyContent.DummyItem> parseXml(InputStream is)
			throws IOException, XmlPullParserException {
		XmlPullParser parser = Xml.newPullParser();
		ArrayList<DummyContent.DummyItem> mAdapter = new ArrayList<DummyContent.DummyItem>();
		try {
			parser.setInput(is, null);
			int eventType = parser.getEventType();
			DummyContent.DummyItem currentItem = null;
			while (eventType != XmlPullParser.END_DOCUMENT) {
				String tag = null;
				switch (eventType) {
				case XmlPullParser.START_TAG:
					tag = parser.getName();
					if (tag.equals("item")) {
						currentItem = new DummyContent.DummyItem();
						// currentItem.setTag("URL");
					} else if (currentItem != null) {
						if (tag.equals("title")) {
							currentItem.setTitle(parser.nextText());
						} else if (tag.equals("link")) {
							currentItem.setLink(parser.nextText());
						} else if (tag.equals("description")) {
							currentItem.setTag(parser.nextText().replaceAll(
									"<.+?>", ""));
						}
					}
					break;
				case XmlPullParser.END_TAG:
					tag = parser.getName();
					if (tag.equals("item")) {
						mAdapter.add(currentItem);
					}
					break;
				}
				eventType = parser.next();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return mAdapter;

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "onDestroy");
	}

}
