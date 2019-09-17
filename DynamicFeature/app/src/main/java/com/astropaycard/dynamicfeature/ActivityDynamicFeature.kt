package com.astropaycard.dynamicfeature

import android.content.Context
import android.content.Intent
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.play.core.splitcompat.SplitCompat
import com.google.android.play.core.splitinstall.SplitInstallManager
import com.google.android.play.core.splitinstall.SplitInstallManagerFactory
import com.google.android.play.core.splitinstall.SplitInstallRequest
import com.google.android.play.core.splitinstall.SplitInstallStateUpdatedListener
import com.google.android.play.core.splitinstall.model.SplitInstallSessionStatus
import kotlinx.android.synthetic.main.activity_dynamic_feature.*


/**
 * Created by ignaciodeandreisdenis on 2019-06-26.
 */
class ActivityDynamicFeature : AppCompatActivity() {

    private lateinit var manager: SplitInstallManager
    private var module: String? = null
    lateinit var rootViewSnackBar: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dynamic_feature)
        rootViewSnackBar = window.decorView.findViewById(R.id.activity_dynamic_feature_linear_layout)

        activity_dynamic_feature_progress_bar.indeterminateDrawable.colorFilter = PorterDuffColorFilter(ContextCompat.getColor(this, R.color.colorSecondary), PorterDuff.Mode.SRC_IN)

        module = intent.getStringExtra(DYNAMIC_MODULE)

        manager = SplitInstallManagerFactory.create(this)

        activity_dynamic_feature_button_launch.setOnClickListener {
            launchActivity(getString(R.string.jumio_class))
        }

        when (module) {
            DYNAMIC_MODULE_JUMIO -> loadAndLaunchModule(getString(R.string.feature_jumio))
        }
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        // Emulates installation of future on demand modules using SplitCompat.
        SplitCompat.install(this)
    }

    private val listenerDownload = SplitInstallStateUpdatedListener { state ->
        val multiInstall = state.moduleNames().size > 1

        state.moduleNames().joinToString(" - ")

        when (state.status()) {
            SplitInstallSessionStatus.DOWNLOADING -> {
                //  In order to see this, the application has to be uploaded to the Play Store.
                activity_dynamic_feature_progress_bar.visibility = View.VISIBLE
            }
            SplitInstallSessionStatus.REQUIRES_USER_CONFIRMATION -> {
                /*
                  This may occur when attempting to download a sufficiently large module.

                  In order to see this, the application has to be uploaded to the Play Store.
                  Then features can be requested until the confirmation path is triggered.
                 */
                manager.startConfirmationDialogForResult(state, this, DYNAMIC_FEATURE_CONFIRMATION_REQUEST_CODE)
            }
            SplitInstallSessionStatus.INSTALLED -> {
                onSuccessfulLoad(getString(R.string.feature_jumio), launch = false) //!multiInstall
            }

            SplitInstallSessionStatus.INSTALLING -> activity_dynamic_feature_progress_bar.visibility = View.VISIBLE


            SplitInstallSessionStatus.FAILED -> {
                Toast.makeText(this, getString(R.string.two_values, getString(R.string.mobile_error), state.errorCode().toString() + state.moduleNames()), Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * Load a feature by module name.
     * @param name The name of the feature module to load.
     */
    private fun loadAndLaunchModule(name: String) {
        activity_dynamic_feature_progress_bar.visibility = View.VISIBLE

        // Skip loading if the module already is installed. Perform success action directly.
        if (manager.installedModules.contains(name)) {
            activity_dynamic_feature_progress_bar.visibility = View.GONE
            onSuccessfulLoad(name, launch = false)
            return
        }

        // Create request to install a feature module by name.
        val request = SplitInstallRequest.newBuilder()
                .addModule(name)
                .build()

        // Load and install the requested feature module.
        manager.startInstall(request)
    }

    /**
     * Define what to do once a feature module is loaded successfully.
     * @param moduleName The name of the successfully loaded module.
     * @param launch `true` if the feature module should be launched, else `false`.
     */
    private fun onSuccessfulLoad(moduleName: String, launch: Boolean) {
        if (launch) {
            when (moduleName) {
                getString(R.string.feature_jumio) -> launchActivity(getString(R.string.jumio_class))
            }
        } else {
            when (moduleName) {
                getString(R.string.feature_jumio) -> {
                    activity_dynamic_feature_button_launch.visibility = View.VISIBLE
                    activity_dynamic_feature_progress_bar.visibility = View.GONE
                }
            }
        }
    }

    /** Launch an activity by its class name. */
    private fun launchActivity(className: String) {
        Intent().setClassName(BuildConfig.APPLICATION_ID, className)
                .also {
                    startActivityForResult(it, GO_TO_JUMIO)
                }
    }

    override fun onResume() {
        // Listener can be registered even without directly triggering a download.
        manager.registerListener(listenerDownload)
        super.onResume()
    }

    override fun onPause() {
        // Make sure to dispose of the listener once it's no longer needed.
        manager.unregisterListener(listenerDownload)
        super.onPause()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == GO_TO_JUMIO) {
            val intent = Intent()
            intent.putExtra(MESSAGE, data?.getStringExtra(MESSAGE))
            setResult(resultCode, intent)
            finish()
        }
    }
}
