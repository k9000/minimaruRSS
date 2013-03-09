package com.tlulybluemonochrome.minimarurss;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URL;
import java.util.ArrayList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;
import android.util.Xml;
import android.widget.Toast;

public class NotificationService extends IntentService {
	
	ArrayList<RssItem> oldlist;
	

	public NotificationService(String name) {
		super(name);
		// TODO 自動生成されたコンストラクター・スタブ
	}

	public NotificationService() {
		super("NotificationService");
	}

	final static String TAG = "test";

	@Override
	protected void onHandleIntent(Intent intent) {
		// TODO 自動生成されたメソッド・スタブ

		RssMessageNotification.titlenotify(getApplicationContext(),
				"minimaruRSS", "更新中", "更新中", 99);

		ArrayList<RssItem> arraylist = new ArrayList<RssItem>();
		oldlist = new ArrayList<RssItem>();

		ArrayList<RssFeed> urilist = new ArrayList<RssFeed>();

		try {
			FileInputStream fis = openFileInput("SaveData.txt");
			ObjectInputStream ois = new ObjectInputStream(fis);
			urilist = (ArrayList<RssFeed>) ois.readObject();
			ois.close();
		} catch (Exception e) {
			Toast.makeText(this, "error1", Toast.LENGTH_SHORT).show();
		}

		try {
			FileInputStream fis = openFileInput("SaveData.dat");
			ObjectInputStream ois = new ObjectInputStream(fis);
			oldlist = (ArrayList<RssItem>) ois.readObject();
			ois.close();
		} catch (Exception e) {
			Log.d(TAG, "Error");
		}

		for (int i = 0; i < urilist.size(); i++) {

			if (urilist.get(i).getNoti()) {
				try {
					URL url = new URL(urilist.get(i).getUrl());
					InputStream is = url.openConnection().getInputStream();
					arraylist.addAll(parseXml(is,urilist.get(i).getTag()));
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}

		for (int i = 0; i < arraylist.size(); i++) {
			if (arraylist.get(i).getTag() != 0) {
				RssMessageNotification.notify(getApplicationContext(),
						arraylist.get(i).getTitle(),
						arraylist.get(i).getText(), arraylist.get(i).getUrl(),
						i,arraylist.get(i).getTag());
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
				}
			}
		}

		try {
			FileOutputStream fos = openFileOutput("SaveData.dat", MODE_PRIVATE);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(arraylist);
			oos.close();
		} catch (Exception e) {
			Log.d(TAG, "Error");
		}

		RssMessageNotification.cancel(getApplicationContext(), 99);
		RssMessageNotification.titlenotify(getApplicationContext(),
				"minimaruRSS", "タップして更新", "更新完了", 100);
	}

	// XMLをパースする
	public ArrayList<RssItem> parseXml(InputStream is, int color)
			throws IOException,
			XmlPullParserException {
		XmlPullParser parser = Xml.newPullParser();
		ArrayList<RssItem> list = new ArrayList<RssItem>();
		try {
			parser.setInput(is, null);
			int eventType = parser.getEventType();
			RssItem currentItem = null;
			while (eventType != XmlPullParser.END_DOCUMENT) {
				String tag = null;
				switch (eventType) {
				case XmlPullParser.START_TAG:
					tag = parser.getName();
					if (tag.equals("item")) {
						currentItem = new RssItem();
						currentItem.setTag(color);
					} else if (currentItem != null) {
						if (tag.equals("title")) {
							currentItem.setTitle(parser.nextText());
						} else if (tag.equals("link")) {
							currentItem.setUrl(parser.nextText());
						} else if (tag.equals("description")) {
							currentItem.setText(parser.nextText().replaceAll(
									"<.+?>", ""));
						}
					}
					break;
				case XmlPullParser.END_TAG:
					tag = parser.getName();
					if (tag.equals("item")) {
						if (Serch(currentItem)){
							currentItem.setTag(0);
						}
						list.add(currentItem);
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

	public boolean Serch(RssItem item) {

		for (int i = 0; i < oldlist.size(); i++) {
			if (item.getUrl().equals(oldlist.get(i).getUrl()))
				return true;
		}

		return false;
	}

}
