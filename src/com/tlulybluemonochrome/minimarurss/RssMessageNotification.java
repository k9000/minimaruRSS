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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;

/**
 * 通知
 * 
 * @author k9000
 * 
 */
public class RssMessageNotification {
	/**
	 * The unique identifier for this type of notification.
	 */
	private static final String NOTIFICATION_TAG = "RssMessage";

	/**
	 * 記事用
	 * 
	 * @param context
	 *            context
	 * @param title
	 *            タイトル
	 * @param text
	 *            本文
	 * @param url
	 *            URL
	 * @param id
	 *            複数通知用ID
	 * @param bitmap
	 *            色分け画像
	 * @param page
	 *            ホームページタイトル
	 * @param pin
	 *            ピン留め有無
	 */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public static void notify(final Context context, final String title,
			final String text, final String url, final int id, Bitmap bitmap,
			String page, boolean pin) {

		// This image is used as the notification's large icon (thumbnail).
		// TODO: Remove this if your notification has no relevant thumbnail.

		final String ticker = title;

		final Notification.Builder builder;

		Notification notification = new Notification();

		builder = new Notification.Builder(context)

				// Set appropriate defaults for the notification light, sound,
				// and vibration.
				// .setDefaults(Notification.DEFAULT_ALL)

				// Set required fields, including the small icon, the
				// notification title, and text.
				.setSmallIcon(R.drawable.ic_stat_rss_message)
				.setContentTitle(title).setContentText(text)

				// All fields below this line are optional.

				// Provide a large icon, shown with the notification in the
				// notification drawer on devices running Android 3.0 or later.
				.setLargeIcon(bitmap)

				// Set ticker text (preview) information for this notification.
				.setTicker(ticker)

				// Show a number. This is useful when stacking notifications of
				// a single type.
				// .setNumber(number)

				// If this notification relates to a past or upcoming event, you
				// should set the relevant time information using the setWhen
				// method below. If this call is omitted, the notification's
				// timestamp will by set to the time at which it was shown.
				// TODO: Call setWhen if this notification relates to a past or
				// upcoming event. The sole argument to this method should be
				// the notification timestamp in milliseconds.
				// .setWhen(...)

				// Set the pending intent to be initiated when the user touches
				// the notification.
				.setContentIntent(
						PendingIntent.getActivity(context, 0, new Intent(
								Intent.ACTION_VIEW, Uri.parse(url)),
								PendingIntent.FLAG_UPDATE_CURRENT))

				// Automatically dismiss the notification when it is touched.
				.setAutoCancel(true);

		if (pin) {
			builder.addAction(android.R.drawable.checkbox_on_background,
					context.getString(R.string.unpinned),

					PendingIntent
							.getService(
									context,
									id,
									new Intent(context,
											NotificationChangeService.class)
											.putExtra("TITLE", title)
											.putExtra("TEXT", text)
											.putExtra("URL", url)
											.putExtra("ID", id)
											.putExtra("BITMAP", bitmap)
											.putExtra("PAGE", page)
											.putExtra("SET", false),
									PendingIntent.FLAG_UPDATE_CURRENT));

		} else {
			builder.addAction(android.R.drawable.checkbox_off_background,
					context.getString(R.string.pinned),

					PendingIntent
							.getService(
									context,
									id,
									new Intent(context,
											NotificationChangeService.class)
											.putExtra("TITLE", title)
											.putExtra("TEXT", text)
											.putExtra("URL", url)
											.putExtra("ID", id)
											.putExtra("BITMAP", bitmap)
											.putExtra("PAGE", page)
											.putExtra("PIN", true),
									PendingIntent.FLAG_UPDATE_CURRENT));
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN && pin) {// Android4.1以降
			builder.setPriority(Notification.PRIORITY_MIN).setStyle(
					new Notification.BigTextStyle().bigText(text)
							.setBigContentTitle(title).setSummaryText(page));

			notification = builder.build();

		} else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN
				&& !pin) {
			builder.setPriority(Notification.PRIORITY_LOW).setStyle(
					new Notification.BigTextStyle().bigText(text)
							.setBigContentTitle(title).setSummaryText(page));

			notification = builder.build();

		} else {

			notification = builder.getNotification();
		}

		if (pin) {
			notification.flags = Notification.FLAG_NO_CLEAR;// 常駐フラグ
		}

		notify(context, notification, id);
	}

	/**
	 * 更新中と更新完了通知
	 * 
	 * @param context
	 *            context
	 * @param title
	 *            タイトル
	 * @param text
	 *            本文
	 * @param ticker
	 *            上に表示されるやつ
	 * @param id
	 *            識別用
	 * @param progress
	 */
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public static void titlenotify(final Context context, final String title,
			final String text, final String ticker, final int id,
			boolean progress) {
		final Resources res = context.getResources();

		// This image is used as the notification's large icon (thumbnail).
		// TODO: Remove this if your notification has no relevant thumbnail.
		final Bitmap picture = BitmapFactory.decodeResource(res,
				R.drawable.ic_launcher);

		final Notification.Builder builder;

		final Notification notification;

		builder = new Notification.Builder(context)

				// Set appropriate defaults for the notification light, sound,
				// and vibration.
				// .setDefaults(Notification.DEFAULT_ALL)

				// Set required fields, including the small icon, the
				// notification title, and text.
				.setSmallIcon(R.drawable.ic_stat_rss_message)
				.setContentTitle(title)
				.setContentText(text)

				// All fields below this line are optional.

				// Provide a large icon, shown with the notification in the
				// notification drawer on devices running Android 3.0 or later.
				.setLargeIcon(picture)

				// Set ticker text (preview) information for this notification.
				.setTicker(ticker)

				// Show a number. This is useful when stacking notifications of
				// a single type.
				// .setNumber(number)

				// If this notification relates to a past or upcoming event, you
				// should set the relevant time information using the setWhen
				// method below. If this call is omitted, the notification's
				// timestamp will by set to the time at which it was shown.
				// TODO: Call setWhen if this notification relates to a past or
				// upcoming event. The sole argument to this method should be
				// the notification timestamp in milliseconds.
				// .setWhen(...)

				// Set the pending intent to be initiated when the user touches
				// the notification.
				.setContentIntent(
						PendingIntent.getService(context, -2, new Intent(
								context, NotificationService.class), 0))

				.setDeleteIntent(
						PendingIntent.getService(context, -1, new Intent(
								context, NotificationChangeService.class)
								.putExtra("TITLE", true),
								PendingIntent.FLAG_UPDATE_CURRENT))

				.addAction(
						android.R.drawable.checkbox_on_background,
						"全て既読にする",
						PendingIntent.getService(context, -3, new Intent(
								context, NotificationChangeService.class)
								.putExtra("CLEAR", true),
								PendingIntent.FLAG_UPDATE_CURRENT));

		// Automatically dismiss the notification when it is touched.
		// .setAutoCancel(true);

		if (progress) {
			builder.setProgress(100, 10, progress);
		}

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			builder.setPriority(Notification.PRIORITY_MIN).setStyle(
					new Notification.BigTextStyle().bigText(text)
							.setBigContentTitle(title)
							.setSummaryText("minimaruRSS"));

			notification = builder.build();

		} else {

			notification = builder.getNotification();
		}

		notify(context, notification, id);
	}

	/**
	 * 通知
	 * 
	 * @param context
	 * @param notification
	 * @param id
	 */
	static void notify(final Context context, final Notification notification,
			final int id) {
		final NotificationManager nm = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.notify(NOTIFICATION_TAG, id, notification);

	}

	/**
	 * 通知削除
	 * 
	 * @param context
	 * @param id
	 */
	public static void cancel(final Context context, int id) {
		final NotificationManager nm = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancel(NOTIFICATION_TAG, id);

	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public static void noti(final Context context,
			final ArrayList<RssItem> arraylist, final int count, final int id,
			final Uri uri) {
		// TODO 自動生成されたメソッド・スタブ

		// This image is used as the notification's large icon (thumbnail).
		// TODO: Remove this if your notification has no relevant thumbnail.

		Bitmap bitmap = null;
		try {
			bitmap = MediaStore.Images.Media.getBitmap(
					context.getContentResolver(), uri);
		} catch (FileNotFoundException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		// bitmap.setHasAlpha(true);

		final String ticker = arraylist.get(0).getTitle();

		final Notification.Builder builder;

		Notification notification = new Notification();

		builder = new Notification.Builder(context)

				// Set appropriate defaults for the notification light, sound,
				// and vibration.
				// .setDefaults(Notification.DEFAULT_ALL)

				// Set required fields, including the small icon, the
				// notification title, and text.
				.setSmallIcon(R.drawable.ic_stat_rss_message)
				.setContentTitle(arraylist.get(0).getTitle())
				.setContentText(arraylist.get(0).getText())

				// All fields below this line are optional.

				// Provide a large icon, shown with the notification in the
				// notification drawer on devices running Android 3.0 or later.
				.setLargeIcon(bitmap)

				// Set ticker text (preview) information for this notification.
				.setTicker(ticker)

				// Show a number. This is useful when stacking notifications of
				// a single type.
				.setNumber(arraylist.size())

				// If this notification relates to a past or upcoming event, you
				// should set the relevant time information using the setWhen
				// method below. If this call is omitted, the notification's
				// timestamp will by set to the time at which it was shown.
				// TODO: Call setWhen if this notification relates to a past or
				// upcoming event. The sole argument to this method should be
				// the notification timestamp in milliseconds.
				// .setWhen(...)

				// Set the pending intent to be initiated when the user touches
				// the notification.
				.setContentIntent(
						PendingIntent.getService(
								context,
								count + 1,
								new Intent(context,
										NotificationChangeService.class)
										.putExtra("BROWSE", true)
										.putExtra("TITLE", false)
										.putExtra("LIST", arraylist)
										.putExtra("COUNT", count)
										.putExtra("ID", id),
								PendingIntent.FLAG_UPDATE_CURRENT))

				.setDeleteIntent(
						PendingIntent.getService(
								context,
								count,
								new Intent(context,
										NotificationChangeService.class)
										.putExtra("BROWSE", false)
										.putExtra("TITLE", false)
										.putExtra("LIST", arraylist)
										.putExtra("COUNT", count)
										.putExtra("ID", id),
								PendingIntent.FLAG_UPDATE_CURRENT))

				// Automatically dismiss the notification when it is touched.
				.setAutoCancel(false);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {// Android4.1以降
			builder.setPriority(Notification.PRIORITY_DEFAULT).setStyle(
					new Notification.BigTextStyle()
							.bigText(arraylist.get(0).getText())
							.setBigContentTitle(arraylist.get(0).getTitle())
							.setSummaryText(arraylist.get(0).getPage()));

			notification = builder.build();

		} else {

			notification = builder.getNotification();
		}

		notify(context, notification, id);

	}

}