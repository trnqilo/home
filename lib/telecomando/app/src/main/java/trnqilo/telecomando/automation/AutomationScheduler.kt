package trnqilo.telecomando.automation

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import kotlinx.coroutines.flow.first
import trnqilo.telecomando.data.AppRepo

interface AutomationScheduler {
  suspend fun sync(ruleId: Int)

  suspend fun syncAll()

  suspend fun cancel(ruleId: Int)
}

class AlarmAutomationScheduler(
  private val context: Context,
  private val repo: AppRepo,
) : AutomationScheduler {
  private val alarmManager = context.getSystemService(AlarmManager::class.java)

  override suspend fun sync(ruleId: Int) {
    val rule = repo.getAutomation(ruleId)
    if (rule == null || !rule.rule.enabled || !rule.hasInitiator(AutomationInitiatorType.Alarm.name)) {
      cancel(ruleId)
      return
    }
    schedule(rule.rule)
  }

  override suspend fun syncAll() {
    repo.getAutomations().first().forEach { rule ->
      if (rule.rule.enabled && rule.hasInitiator(AutomationInitiatorType.Alarm.name)) {
        schedule(rule.rule)
      } else {
        cancel(rule.rule.ruleId)
      }
    }
  }

  override suspend fun cancel(ruleId: Int) {
    alarmManager.cancel(pendingIntent(ruleId))
  }

  private fun schedule(rule: trnqilo.telecomando.data.AutomationRuleEntity) {
    val triggerAt = System.currentTimeMillis() + (rule.initiatorDelayMinutes.coerceAtLeast(1) * 60_000L)
    alarmManager.setAndAllowWhileIdle(
      AlarmManager.RTC_WAKEUP,
      triggerAt,
      pendingIntent(rule.ruleId),
    )
  }

  private fun pendingIntent(ruleId: Int): PendingIntent {
    val intent = Intent(context, AutomationAlarmReceiver::class.java)
      .setAction(ACTION_RUN_AUTOMATION)
      .putExtra(EXTRA_RULE_ID, ruleId)
    return PendingIntent.getBroadcast(
      context,
      ruleId,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
    )
  }

}

private fun trnqilo.telecomando.data.AutomationRuleWithSteps.hasInitiator(type: String): Boolean =
  initiators.any { it.type == type }

private const val ACTION_RUN_AUTOMATION = "trnqilo.telecomando.action.RUN_AUTOMATION"
private const val EXTRA_RULE_ID = "automation_rule_id"

internal fun automationRuleId(intent: Intent): Int =
  intent.getIntExtra(EXTRA_RULE_ID, -1)
