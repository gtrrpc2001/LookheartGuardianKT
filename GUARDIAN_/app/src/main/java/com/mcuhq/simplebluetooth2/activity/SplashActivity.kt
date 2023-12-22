package com.mcuhq.simplebluetooth2.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.mcuhq.simplebluetooth2.R

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
class SplashActivity : AppCompatActivity() {
    var handler = Handler()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        handler.postDelayed({ /* Create an Intent that will start the Menu-Activity. */
            val mainIntent = Intent(applicationContext, Activity_Login::class.java)
            startActivity(mainIntent)
            finish()
        }, 2000)
    }
}