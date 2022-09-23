package com.megaulorder.friedsoup

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class ResultActivity : AppCompatActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.activity_result)
		supportActionBar?.title = resources.getString(R.string.soup_title)
	}
}