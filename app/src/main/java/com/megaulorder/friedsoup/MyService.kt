package com.megaulorder.friedsoup

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.widget.RemoteViews
import android.widget.Toast
import androidx.annotation.IdRes
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat

private const val NORMAL_CHANNEL_ID = "CHANNEL_NORMAL"
private const val SERVICE_NOTIFICATION_ID: Int = 5

class MyService : Service() {

	private val binder = MyBinder(this)

	var currentEmoji: String = "\uD83D\uDC7B"

	// листенер нажатия кнопки в нотификашке
	var onClickListener: ((emoji: String) -> Unit)? = null

	private var layout: RemoteViews? = null

	private var notificationManager: NotificationManager? = null
	private var pendingIntent: PendingIntent? = null

	private fun buildNotification(emoji: String): Notification {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			createNotificationChannel()
		}
		layout = RemoteViews(packageName, R.layout.notification)
		layout!!.setTextViewText(R.id.textview, emoji)
		currentEmoji = emoji

		val resultIntent = Intent(this, MainActivity::class.java)
		pendingIntent =
			PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_IMMUTABLE)

		setOnClickPendingIntents()

		return NotificationCompat.Builder(this, NORMAL_CHANNEL_ID)
			.setSmallIcon(R.drawable.ic_emoji)
			.setPriority(NotificationCompat.PRIORITY_DEFAULT)
			.setContentIntent(pendingIntent)
			.setCategory(NotificationCompat.CATEGORY_MESSAGE)
			.setStyle(NotificationCompat.DecoratedCustomViewStyle())
			.setCustomContentView(layout)
			.setAutoCancel(false)
			.setOnlyAlertOnce(true)
			.build()
	}

	// дергается когда нажимаем на кнопку в нотификашке
	fun setOnClickCallback(emoji: String) {
		onClickListener?.invoke(emoji)
	}

	// обновляем нотифик с новой емоздей
	fun updateEmoji(emoji: String) {
		notificationManager?.notify(SERVICE_NOTIFICATION_ID, buildNotification(emoji))
	}

	@RequiresApi(Build.VERSION_CODES.O)
	private fun createNotificationChannel() {
		val name = getString(R.string.normal_channel_name)
		val descriptionText = getString(R.string.normal_channel_description)
		val channel = NotificationChannel(
			NORMAL_CHANNEL_ID,
			name,
			NotificationManager.IMPORTANCE_DEFAULT
		).apply {
			description = descriptionText
			setShowBadge(true)
		}
		notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
		notificationManager!!.createNotificationChannel(channel)
	}

	// задаем кнопкам пендинг интенты на клик
	private fun setOnClickPendingIntents(lambda: ((emoji: String) -> Unit)? = null) {
		for (emoji in Emoji.values()) {
			val buttonIntent = Intent(this, MyReceiver::class.java)
			buttonIntent.action = emoji.text
			val pendingIntent =
				PendingIntent.getBroadcast(this, 0, buttonIntent, PendingIntent.FLAG_IMMUTABLE)
			layout?.setOnClickPendingIntent(emoji.id, pendingIntent)
			val buttonEmoji: String = emoji.text
			currentEmoji = buttonEmoji
			lambda?.invoke(buttonEmoji)
		}
	}

	override fun onBind(intent: Intent): IBinder {
		val emoji: String? = intent.getStringExtra(MainActivity.CURRENT_EMOJI_EXTRA)
		startForeground(SERVICE_NOTIFICATION_ID, buildNotification(emoji ?: currentEmoji))
		Toast.makeText(this, "Service bound", Toast.LENGTH_SHORT).show()
		return binder
	}

	override fun onUnbind(intent: Intent?): Boolean {
		Toast.makeText(this, "Service unbound", Toast.LENGTH_SHORT).show()
		return super.onUnbind(intent)
	}
}

class MyBinder(private val service: MyService) : Binder() {

	fun getService(): MyService = service
}

enum class Emoji(
	@IdRes val id: Int,
	val text: String,
) {
	PUMPKIN(R.id.notification_pumpkin, "\uD83C\uDF83"),
	FLOWER(R.id.notification_flower, "\uD83C\uDF38"),
	TREE(R.id.notification_tree, "\uD83C\uDF84")
}