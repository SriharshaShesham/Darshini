package tv.darshini.app.update

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.os.Build
import android.util.Log
import android.widget.Toast

/**
 * Receives the status of a [PackageInstaller] session committed by [AppUpdateInstaller]. The
 * system hands back a confirmation activity (STATUS_PENDING_USER_ACTION) that must be started for
 * the user to approve the update; every other status is terminal.
 */
class AppUpdateInstallResultReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val status = intent.getIntExtra(PackageInstaller.EXTRA_STATUS, PackageInstaller.STATUS_FAILURE)
        when (status) {
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                val confirmIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    intent.getParcelableExtra(Intent.EXTRA_INTENT, Intent::class.java)
                } else {
                    @Suppress("DEPRECATION")
                    intent.getParcelableExtra(Intent.EXTRA_INTENT)
                }
                if (confirmIntent == null) {
                    Log.e(TAG, "Pending user action without a confirmation intent")
                    return
                }
                confirmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                runCatching { context.startActivity(confirmIntent) }
                    .onFailure { Log.e(TAG, "Could not show the install confirmation", it) }
            }
            PackageInstaller.STATUS_SUCCESS -> Log.i(TAG, "Update installed")
            else -> {
                val message = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)
                Log.w(TAG, "Update install failed status=$status message=$message")
                // STATUS_FAILURE_ABORTED is the user backing out of the confirmation; not an error.
                if (status != PackageInstaller.STATUS_FAILURE_ABORTED) {
                    Toast.makeText(context, "Update could not be installed", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private companion object {
        const val TAG = "AppUpdateInstaller"
    }
}
