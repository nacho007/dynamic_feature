package com.astropaycard.dynamicfeature.feature_jumio

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.astropaycard.android.jumio.ANALYTICS_JUMIO
import com.astropaycard.android.jumio.JUMIO_API_SECRET
import com.astropaycard.android.jumio.JUMIO_API_TOKEN
import com.google.android.play.core.splitcompat.SplitCompat
import com.jumio.core.enums.JumioDataCenter
import com.jumio.core.exceptions.PlatformNotSupportedException
import com.jumio.nv.IsoCountryConverter
import com.jumio.nv.NetverifyInitiateCallback
import com.jumio.nv.NetverifySDK


/**
 * Created by ignaciodeandreisdenis on 2019-06-20.
 */
class ActivityJumio: AppCompatActivity() {

    private val permissionsCamera = arrayOf(android.Manifest.permission.CAMERA)
    private val PERMISSIONS_CAMERA = 105
    lateinit var rootViewSnackBar: View
    var screenName = ""

    private lateinit var netVerifySDK: NetverifySDK

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_jumio)
        screenName = ANALYTICS_JUMIO
        rootViewSnackBar = window.decorView.findViewById(R.id.activity_jumio_linear_layout)

        try {
            netVerifySDK = NetverifySDK.create(this, JUMIO_API_TOKEN, JUMIO_API_SECRET, JumioDataCenter.US)
            netVerifySDK.setCustomTheme(R.style.CustomNetverifyTheme)
            netVerifySDK.setPreselectedCountry(IsoCountryConverter.convertToAlpha3("UYU"))
            netVerifySDK.setUserReference("173")

            netVerifySDK.initiate(object : NetverifyInitiateCallback {
                override fun onNetverifyInitiateSuccess() {
                }

                override fun onNetverifyInitiateError(errorCode: String, errorMessage: String, retryPossible: Boolean) {
                }
            })

            checkJumioConditions()

        } catch (e: PlatformNotSupportedException) {
        } catch (e1: NullPointerException) {
        } catch (e2: Exception) {
            e2.printStackTrace()
        }
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        // Emulates installation of future on demand modules using SplitCompat.
        SplitCompat.install(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == NetverifySDK.REQUEST_CODE) {
            var message = ""

            if (resultCode == RESULT_OK) {
                message = "Result Ok"
            } else if (resultCode == Activity.RESULT_CANCELED) {
                message = "Result Canceled"
            }

            netVerifySDK.destroy()

            val intent = Intent()
            finish()
        }
    }

    private fun checkJumioConditions() {
        val netVerifySDKIsRooted = NetverifySDK.isRooted(this)

        val isSupportedPlatform = NetverifySDK.isSupportedPlatform(this)

        if (isSupportedPlatform) {
            checkPermissions()
        } else {

        }
    }

    private fun startJumio() {
        if (::netVerifySDK.isInitialized) {
            netVerifySDK.start()
        }
    }

    private fun checkPermissions() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (!hasPermissions(this, permissionsCamera[0])) {
                ActivityCompat.requestPermissions(this, permissionsCamera, PERMISSIONS_CAMERA)
            } else {
                startJumio()
            }
        } else {
            startJumio()
        }
    }

    private fun hasPermissions(context: Context?, vararg permissions: String): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && context != null) {
            permissions.forEach {
                if (ActivityCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED) {
                    return false
                }
            }
        }
        return true
    }
}
