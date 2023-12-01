package trnqilo.telecomando.automation

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.flow.first
import trnqilo.telecomando.data.AppRepo
import trnqilo.telecomando.data.DatabaseBuilder.appDatabase

class AutomationRunWorker(
  appContext: Context,
  workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {
  override suspend fun doWork(): Result {
    val ruleId = inputData.getInt(KEY_RULE_ID, -1)
    if (ruleId < 0) return Result.failure()

    val repo = AppRepo(appDatabase(applicationContext).commandDao())
    val rule = repo.getAutomation(ruleId) ?: return Result.success()
    val commandRunner = AutomationChainRunner()
    commandRunner.execute(rule) { commandId ->
      repo.getCommandWithServers(commandId)
    }

    return Result.success()
  }

  companion object {
    const val KEY_RULE_ID = "rule_id"
  }
}
