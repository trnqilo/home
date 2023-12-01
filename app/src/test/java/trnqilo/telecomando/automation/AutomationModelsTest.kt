package trnqilo.telecomando.automation

import org.junit.Assert.assertEquals
import org.junit.Test
import trnqilo.telecomando.data.AutomationRuleEntity
import trnqilo.telecomando.data.AutomationRuleWithSteps
import trnqilo.telecomando.data.AutomationStepEntity

class AutomationModelsTest {
  @Test
  fun roundTripsStepChainDraftAndSave() {
    val model = AutomationRuleWithSteps(
      rule = AutomationRuleEntity(
        name = "Chain",
        enabled = true,
        initiatorType = AutomationInitiatorType.Alarm.name,
        initiatorDelayMinutes = 15,
        commandId = 1,
        completionPolicy = "Chain",
        ruleId = 9,
      ),
      initiators = listOf(
        trnqilo.telecomando.data.AutomationInitiatorEntity(
          ruleId = 9,
          orderIndex = 0,
          type = AutomationInitiatorType.Alarm.name,
          configJson = """{"delayMinutes":"15"}""",
          initiatorId = 1,
        ),
      ),
      steps = listOf(
        AutomationStepEntity(ruleId = 9, orderIndex = 0, commandId = 101, successNextStepId = 11, stepId = 10),
        AutomationStepEntity(ruleId = 9, orderIndex = 1, commandId = 102, stepId = 11),
      ),
    )

    val draft = model.toDraft()
    assertEquals(listOf(10L, 11L), draft.steps.map { it.key })
    assertEquals(11L, draft.steps.first().successNextKey)

    val save = draft.toSave()
    assertEquals("Chain", save.rule.name)
    assertEquals(101, save.rule.commandId)
    assertEquals("Chain", save.rule.completionPolicy)
    assertEquals(1, save.initiators.size)
    assertEquals(AutomationInitiatorType.Alarm.name, save.rule.initiatorType)
    assertEquals(listOf(10L, 11L), save.steps.map { it.key })
  }

  @Test
  fun preservesSmsInitiatorType() {
    val draft = AutomationRuleDraft(
      name = "SMS",
      initiators = listOf(
        AutomationInitiatorDraft(type = AutomationInitiatorType.SmsReceived.name),
      ),
      steps = listOf(
        AutomationStepDraft(commandId = 1),
      ),
    )

    val save = draft.toSave()
    assertEquals(AutomationInitiatorType.SmsReceived.name, save.rule.initiatorType)
    assertEquals(AutomationInitiatorType.SmsReceived.name, draft.toExecutionPreview().rule.initiatorType)
  }
}
