package com.astropaycard.dynamicfeature

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.play.core.splitcompat.SplitCompat
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        activity_main_button.setOnClickListener{
            val intent = Intent(this, ActivityDynamicFeature::class.java)
            intent.putExtra(DYNAMIC_MODULE, DYNAMIC_MODULE_JUMIO)
            startActivityForResult(intent, GO_TO_DYNAMIC_FEATURES)
        }
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        // Emulates installation of future on demand modules using SplitCompat.
        SplitCompat.install(this)
    }
}
