package com.megaulorder.friedsoup

import android.content.*
import android.os.IBinder

class MyReceiver: BroadcastReceiver() {

	var emoji: String = "\uD83D\uDC7B"

	override fun onReceive(context: Context?, intent: Intent?) {
		emoji = intent?.action.toString()

		val binder: IBinder = peekService(context, Intent(context, MyService::class.java)) ?: return
		binder as MyBinder
		binder.updateEmoji(emoji)
	}
}