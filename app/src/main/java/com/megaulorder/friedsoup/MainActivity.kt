package com.megaulorder.friedsoup

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

private const val ALERT_CHANNEL_ID = "CHANNEL_ALERT"
private const val SILENT_CHANNEL_ID = "CHANNEL_SILENT"

private const val SIMPLE_NOTIFICATION_ID: Int = 1
private const val BIG_TEXT_NOTIFICATION_ID: Int = 2
private const val IMAGE_NOTIFICATION_ID: Int = 3
private const val SILENT_NOTIFICATION_ID: Int = 4

class MainActivity : AppCompatActivity() {

	companion object {
		const val CURRENT_EMOJI_EXTRA = "current_emoji"
	}

	private var myService: MyService? = null
	private lateinit var serviceIntent: Intent

	private var isServiceRunning = false
	private var isServiceBound: Boolean = false

	private var controller: EmojiController? = null

	private val connection = object : ServiceConnection {

		override fun onServiceConnected(className: ComponentName, service: IBinder) {
			val binder = service as MyBinder
			myService = binder.getService()
			// подписываемся в контроллере на обновление емодзь из сервиса
			myService!!.onClickListener = { controller!!.setEmoji(it) }
			isServiceBound = true
		}

		override fun onServiceDisconnected(arg0: ComponentName) {
			isServiceBound = false
		}
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_main)

		val monkaImg: Bitmap = BitmapFactory.decodeResource(resources, R.drawable.monka)
		val simpleNotificationTitle: String =
			resources.getString(R.string.simple_notification_title)
		val simpleNotificationDescription: String =
			resources.getString(R.string.simple_notification_description)
		val bigTextNotificationTitle: String =
			resources.getString(R.string.big_text_notification_title)
		val bigTextNotificationDescription: String =
			resources.getString(R.string.big_text_notification_descriptiom)
		val soupText: String = resources.getString(R.string.soup_text)
		val imageNotificationTitle: String =
			resources.getString(R.string.notification_with_image_title)
		val imageNotificationDescription: String =
			resources.getString(R.string.notification_with_image_description)
		val silentNotificationTitle: String =
			resources.getString(R.string.silent_notification_title)
		val silentNotificationDescription: String =
			resources.getString(R.string.silent_notification_description)

		createAlertNotificationChannel()
		createSilentNotificationChannel()

		val resultIntent = Intent(this, ResultActivity::class.java)

		val pendingIntent: PendingIntent =
			PendingIntent.getActivity(this, 0, resultIntent, PendingIntent.FLAG_IMMUTABLE)

		val notificationSimple = NotificationCompat.Builder(this, ALERT_CHANNEL_ID)
			.setSmallIcon(R.drawable.ic_attention)
			.setContentTitle(simpleNotificationTitle)
			.setContentText(simpleNotificationDescription)
			.setPriority(NotificationCompat.PRIORITY_HIGH)
			.setContentIntent(pendingIntent)
			.setCategory(NotificationCompat.CATEGORY_MESSAGE)
			.setAutoCancel(true)

		val bitTextNotification = NotificationCompat.Builder(this, ALERT_CHANNEL_ID)
			.setSmallIcon(R.drawable.ic_food)
			.setContentTitle(bigTextNotificationTitle)
			.setContentText(bigTextNotificationDescription)
			.setPriority(NotificationCompat.PRIORITY_HIGH)
			.setContentIntent(pendingIntent)
			.setCategory(NotificationCompat.CATEGORY_MESSAGE)
			.setStyle(
				NotificationCompat.BigTextStyle()
					.bigText(soupText)
			)
			.setAutoCancel(true)

		val imageNotification = NotificationCompat.Builder(this, ALERT_CHANNEL_ID)
			.setSmallIcon(R.drawable.ic_fire)
			.setContentTitle(imageNotificationTitle)
			.setContentText(imageNotificationDescription)
			.setPriority(NotificationCompat.PRIORITY_HIGH)
			.setContentIntent(pendingIntent)
			.setCategory(NotificationCompat.CATEGORY_MESSAGE)
			.setLargeIcon(monkaImg)
			.setStyle(
				NotificationCompat.BigPictureStyle()
					.bigPicture(monkaImg)
					.bigLargeIcon(null)
			)
			.setAutoCancel(true)

		val silentNotification = NotificationCompat.Builder(this, SILENT_CHANNEL_ID)
			.setSmallIcon(R.drawable.ic_night)
			.setContentTitle(silentNotificationTitle)
			.setContentText(silentNotificationDescription)
			.setPriority(NotificationCompat.PRIORITY_LOW)
			.setContentIntent(pendingIntent)
			.setCategory(NotificationCompat.CATEGORY_MESSAGE)
			.setAutoCancel(true)

		val headsUpButton: Button = findViewById(R.id.heads_up_notification_button)
		headsUpButton.setOnClickListener {
			with(NotificationManagerCompat.from(this)) {
				notify(SIMPLE_NOTIFICATION_ID, notificationSimple.build())
			}
		}

		val bigTextButton: Button = findViewById(R.id.bit_text_notification_button)
		bigTextButton.setOnClickListener {
			with(NotificationManagerCompat.from(this)) {
				notify(BIG_TEXT_NOTIFICATION_ID, bitTextNotification.build())
			}
		}

		val withImageButton: Button = findViewById(R.id.notification_with_image_button)
		withImageButton.setOnClickListener {
			with(NotificationManagerCompat.from(this)) {
				notify(IMAGE_NOTIFICATION_ID, imageNotification.build())
			}
		}

		val silentButton: Button = findViewById(R.id.silent_notification_button)
		silentButton.setOnClickListener {
			with(NotificationManagerCompat.from(this)) {
				notify(SILENT_NOTIFICATION_ID, silentNotification.build())
			}
		}

		controller = EmojiController(
			widget = EmojiWidget(findViewById(R.id.textview)),
			buttons = listOf(
				findViewById(R.id.pumpkin),
				findViewById(R.id.flower),
				findViewById(R.id.tree),
			)
		)

		serviceIntent = Intent(this, MyService::class.java)

		findViewById<Button>(R.id.start).setOnClickListener {
			if (!isServiceRunning) {
				// кладем в интент емодзю чтобы засетать ее во вью нотифика
				// на случай если до запуска сервиса она поменялась в активити
				serviceIntent.putExtra(CURRENT_EMOJI_EXTRA, controller!!.currentEmoji.invoke())
				startService(serviceIntent)
				bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE)
				// подписываемся в сервисе на обновление емодзь из активити
				controller!!.setClickListeners { myService!!.updateEmoji(it) }
				isServiceRunning = true
			}
		}

		findViewById<Button>(R.id.stop).setOnClickListener {
			if (isServiceRunning) {
				unbindService(connection)
				stopService(serviceIntent)
				isServiceRunning = false
			}
		}
	}

	override fun onDestroy() {
		super.onDestroy()

		if (isServiceRunning) {
			stopService(serviceIntent)
			isServiceRunning = false
		}
	}

	private fun createAlertNotificationChannel() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			val name = getString(R.string.alert_channel_name)
			val descriptionText = getString(R.string.alert_channel_description)
			val importance = NotificationManager.IMPORTANCE_HIGH
			val channel = NotificationChannel(ALERT_CHANNEL_ID, name, importance).apply {
				description = descriptionText
			}
			val notificationManager: NotificationManager =
				getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
			notificationManager.createNotificationChannel(channel)
		}
	}

	private fun createSilentNotificationChannel() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			val name = getString(R.string.silent_channel_name)
			val descriptionText = getString(R.string.silent_channel_description)
			val importance = NotificationManager.IMPORTANCE_LOW
			val channel = NotificationChannel(SILENT_CHANNEL_ID, name, importance).apply {
				description = descriptionText
			}
			val notificationManager: NotificationManager =
				getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
			notificationManager.createNotificationChannel(channel)
		}
	}
}