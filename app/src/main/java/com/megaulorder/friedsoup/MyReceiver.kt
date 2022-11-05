package com.megaulorder.friedsoup

import android.content.*
import android.os.IBinder

class MyReceiver: BroadcastReceiver() {

	var emoji: String = "\uD83D\uDC7B"

	// обновляем текствю в нотификашке и передаем выбранную емодзи в калбек
	override fun onReceive(context: Context?, intent: Intent?) {
		emoji = intent?.action.toString()

		val binder: IBinder = peekService(context, Intent(context, MyService::class.java)) ?: return
		val service: MyService = (binder as MyBinder).getService()
		service.updateEmoji(emoji)
		service.setOnClickCallback(emoji)
	}
}