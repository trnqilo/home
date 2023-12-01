package trnqilo.telecomando.automation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.flow.first
import trnqilo.telecomando.data.AppRepo
import trnqilo.telecomando.data.DatabaseBuilder.appDatabase

class AutomationAlarmReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    val ruleId = automationRuleId(intent)
    if (ruleId >= 0) enqueueAutomationRun(context, ruleId)
  }
}

class AutomationSmsReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    if (intent.action != SMS_RECEIVED_ACTION) return
    runBlocking {
      val repo = AppRepo(appDatabase(context.applicationContext).commandDao())
      repo.getAutomations().first()
        .asSequence()
        .filter { it.rule.enabled && it.initiators.any { initiator -> initiator.type == AutomationInitiatorType.SmsReceived.name } }
        .forEach { rule -> enqueueAutomationRun(context, rule.rule.ruleId) }
    }
  }
}

class AutomationBootReceiver : BroadcastReceiver() {
  override fun onReceive(context: Context, intent: Intent) {
    if (intent.action != Intent.ACTION_BOOT_COMPLETED) return
    runBlocking {
      AlarmAutomationScheduler(
        context.applicationContext,
        AppRepo(appDatabase(context.applicationContext).commandDao()),
      ).syncAll()
    }
  }
}

private fun enqueueAutomationRun(context: Context, ruleId: Int) {
  WorkManager.getInstance(context).enqueue(
    OneTimeWorkRequestBuilder<AutomationRunWorker>()
      .setInputData(Data.Builder().putInt(AutomationRunWorker.KEY_RULE_ID, ruleId).build())
      .build()
  )
}

private const val SMS_RECEIVED_ACTION = "android.provider.Telephony.SMS_RECEIVED"
