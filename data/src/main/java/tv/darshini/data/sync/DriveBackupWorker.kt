package tv.darshini.data.sync

import android.content.Context
import android.util.Log
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import tv.darshini.domain.manager.DriveAuthState
import tv.darshini.domain.manager.DriveBackupSyncManager
import tv.darshini.domain.model.DriveSyncCadence
import tv.darshini.domain.sync.PlaybackActivitySignal
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.flow.first

/**
 * Uploads the backup to Google Drive on the cadence chosen in Settings.
 *
 * Push only — see [DriveSyncCadence] for why a scheduled pull is not offered. Signed out is a
 * no-op, not a failure: the user simply has not connected Drive, and retrying would burn battery
 * on a schedule forever.
 */
class DriveBackupWorker(
    appContext: Context,
    params: WorkerParameters,
) : CoroutineWorker(appContext, params) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface DriveBackupWorkerEntryPoint {
        fun driveBackupSyncManager(): DriveBackupSyncManager
    }

    override suspend fun doWork(): Result {
        if (PlaybackActivitySignal.isActive) {
            Log.d(TAG, "Deferring Drive backup: playback active")
            return Result.retry()
        }
        return try {
            val driveManager = EntryPointAccessors
                .fromApplication(applicationContext, DriveBackupWorkerEntryPoint::class.java)
                .driveBackupSyncManager()

            if (driveManager.authState.first() !is DriveAuthState.SignedIn) {
                Log.d(TAG, "Skipping Drive backup: not signed in")
                return Result.success()
            }

            when (val result = driveManager.pushAll()) {
                is tv.darshini.domain.model.Result.Success -> Result.success()
                is tv.darshini.domain.model.Result.Error -> {
                    Log.w(TAG, "Drive backup failed: ${result.message}")
                    Result.retry()
                }
                tv.darshini.domain.model.Result.Loading -> Result.success()
            }
        } catch (t: Throwable) {
            Log.e(TAG, "Drive backup worker crashed", t)
            Result.retry()
        }
    }

    companion object {
        private const val TAG = "DriveBackupWorker"
        private const val UNIQUE_WORK_NAME = "DriveBackupWorker"

        fun enqueuePeriodic(context: Context, cadence: DriveSyncCadence) {
            if (cadence == DriveSyncCadence.MANUAL) {
                cancelPeriodic(context)
                return
            }
            val request = PeriodicWorkRequestBuilder<DriveBackupWorker>(
                cadence.intervalHours,
                TimeUnit.HOURS
            )
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .setRequiresBatteryNotLow(true)
                        .build()
                )
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.MINUTES)
                .build()

            // UPDATE, not KEEP: changing the cadence in Settings must re-schedule the existing work.
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        fun cancelPeriodic(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME)
        }
    }
}
