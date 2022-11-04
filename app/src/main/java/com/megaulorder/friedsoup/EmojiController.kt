package com.megaulorder.friedsoup

import android.widget.Button

class EmojiController(
	private val widget: EmojiWidget,
	private val buttons: List<Button>,
) {
	var currentEmoji: (() -> String) = { "\uD83D\uDC7B" }

	init {
		setClickListeners()
	}

	fun setClickListeners(lambda: ((emoji: String) -> Unit)? = null) {
		for (button in buttons) {
			button.setOnClickListener {
				val buttonEmoji = button.text.toString()
				widget.setEmoji(buttonEmoji)
				currentEmoji = { buttonEmoji }
				lambda?.invoke(buttonEmoji)
			}
		}
	}
}