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

	fun setEmoji(emoji: String) {
		currentEmoji = { emoji }
		widget.setEmoji(emoji)
	}

	fun setClickListeners(lambda: ((emoji: String) -> Unit)? = null) {
		for (button in buttons) {
			button.setOnClickListener {
				val buttonEmoji = button.text.toString()
				setEmoji(buttonEmoji)
				lambda?.invoke(buttonEmoji)
			}
		}
	}
}