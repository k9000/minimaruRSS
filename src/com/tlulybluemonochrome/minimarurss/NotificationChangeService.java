package com.tlulybluemonochrome.minimarurss;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;

public class NotificationChangeService extends IntentService {

	public NotificationChangeService() {
		super("NotificationChangeService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		RssMessageNotification.notify(getApplicationContext(),
				intent.getStringExtra("TITLE"), intent.getStringExtra("TEXT"),
				intent.getStringExtra("URL"), intent.getIntExtra("ID", 0),
				(Bitmap) intent.getParcelableExtra("BITMAP"),
				intent.getStringExtra("PAGE"),
				intent.getBooleanExtra("PIN", false));

	}

}