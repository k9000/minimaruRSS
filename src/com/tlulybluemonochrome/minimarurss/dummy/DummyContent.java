package com.tlulybluemonochrome.minimarurss.dummy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class DummyContent {

	/**
	 * An array of sample (dummy) items.
	 */
	public static List<DummyItem> ITEMS = new ArrayList<DummyItem>();

	/**
	 * A map of sample (dummy) items, by ID.
	 */
	public static Map<String, DummyItem> ITEM_MAP = new HashMap<String, DummyItem>();

	static {
		// Add 3 sample items.
		addItem(new DummyItem("1", "Item 1", "google",
				"http://news.google.com/"));
		addItem(new DummyItem("2", "Item 2", "yahoo", "http://www.yahoo.co.jp/"));
		addItem(new DummyItem("3", "Item 3", "android",
				"http://www.android.com/"));
	}

	private static void addItem(DummyItem item) {
		ITEMS.add(item);
		ITEM_MAP.put(item.id, item);
	}

	/**
	 * A dummy item representing a piece of content.
	 */
	public static class DummyItem {
		public String id;
		public String content;
		public String title;
		public String url;

		public DummyItem(String id, String content, String title, String url) {
			this.id = id;
			this.content = content;
			this.title = title;
			this.url = url;
		}

		@Override
		public String toString() {
			return content;
		}
	}
}
