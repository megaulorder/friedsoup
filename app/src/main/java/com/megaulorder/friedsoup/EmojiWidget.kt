package com.megaulorder.friedsoup

import android.widget.TextView

class EmojiWidget(
	private val view: TextView
) {
	fun setEmoji(emoji: String) {
		view.text = emoji
	}
}