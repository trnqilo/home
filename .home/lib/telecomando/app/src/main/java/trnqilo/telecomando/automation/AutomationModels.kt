package trnqilo.telecomando.automation

import java.util.concurrent.atomic.AtomicLong
import trnqilo.telecomando.data.AutomationInitiatorEntity
import trnqilo.telecomando.data.AutomationRuleEntity
import trnqilo.telecomando.data.AutomationRuleWithSteps
import trnqilo.telecomando.data.AutomationStepEntity
import trnqilo.telecomando.data.flatJsonObjectOf
import trnqilo.telecomando.data.parseFlatJsonObject

enum class AutomationInitiatorType {
  Alarm,
  SmsReceived,
}

enum class AutomationStepBranchMode {
  Terminal,
  Success,
  Failure,
  Both,
  Split,
}

data class AutomationInitiatorDraft(
  val key: Long = nextAutomationInitiatorKey(),
  val type: String = AutomationInitiatorType.Alarm.name,
  val alarmDelayMinutes: String = "15",
)

data class AutomationStepDraft(
  val key: Long = nextAutomationStepKey(),
  val commandId: Int? = null,
  val successNextKey: Long? = null,
  val failureNextKey: Long? = null,
)

data class AutomationRuleDraft(
  val ruleId: Int = 0,
  val name: String = "",
  val enabled: Boolean = true,
  val initiators: List<AutomationInitiatorDraft> = listOf(AutomationInitiatorDraft()),
  val steps: List<AutomationStepDraft> = listOf(AutomationStepDraft()),
)

data class AutomationSave(
  val rule: AutomationRuleEntity,
  val initiators: List<AutomationInitiatorDraft>,
  val steps: List<AutomationStepDraft>,
)

internal fun AutomationRuleWithSteps.toDraft(): AutomationRuleDraft =
  AutomationRuleDraft(
    ruleId = rule.ruleId,
    name = rule.name,
    enabled = rule.enabled,
    initiators = initiators.sortedBy(AutomationInitiatorEntity::orderIndex).map { it.toDraft() }
      .ifEmpty { listOf(AutomationInitiatorDraft()) },
    steps = steps.sortedBy(AutomationStepEntity::orderIndex).map { step ->
      AutomationStepDraft(
        key = step.stepId.toLong(),
        commandId = step.commandId.takeIf { it != 0 },
        successNextKey = step.successNextStepId?.let { nextStepId ->
          steps.firstOrNull { it.stepId == nextStepId }?.stepId?.toLong()
        },
        failureNextKey = step.failureNextStepId?.let { nextStepId ->
          steps.firstOrNull { it.stepId == nextStepId }?.stepId?.toLong()
        },
      )
    }.ifEmpty { listOf(AutomationStepDraft()) },
  )

internal fun AutomationRuleDraft.toSave(): AutomationSave {
  val orderedInitiators = initiators.mapIndexed { index, initiator -> index to initiator }
  val orderedSteps = steps.mapIndexed { index, step -> index to step }
  require(orderedInitiators.isNotEmpty()) { "An automation needs at least one initiator." }
  require(orderedInitiators.all { (_, initiator) -> initiator.isValid() }) {
    "All automation initiators need valid settings."
  }
  require(orderedSteps.isNotEmpty()) { "An automation needs at least one step." }
  require(orderedSteps.all { (_, step) -> step.commandId != null }) {
    "All automation steps need a command."
  }
  require(hasForwardOnlyStepLinks()) {
    "Automation step branches must point to later steps or end."
  }

  val primaryInitiator = requireNotNull(orderedInitiators.first().second)
  val primaryCommandId = requireNotNull(orderedSteps.first().second.commandId)
  return AutomationSave(
    rule = AutomationRuleEntity(
      name = name,
      enabled = enabled,
      initiatorType = primaryInitiator.type,
      initiatorDelayMinutes = primaryInitiator.delayMinutesOrDefault(),
      commandId = primaryCommandId,
      completionPolicy = "Chain",
      ruleId = ruleId,
    ),
    initiators = initiators,
    steps = steps,
  )
}

internal fun AutomationRuleDraft.toExecutionPreview(): AutomationRuleWithSteps {
  val previewInitiators = initiators.ifEmpty { listOf(AutomationInitiatorDraft()) }
  val primaryInitiator = previewInitiators.first()
  val delayMinutes = primaryInitiator.alarmDelayMinutes.toIntOrNull()?.coerceAtLeast(1) ?: 1
  val previewSteps = steps.mapIndexed { index, step ->
    AutomationStepEntity(
      ruleId = ruleId,
      orderIndex = index,
      commandId = requireNotNull(step.commandId),
      stepId = index + 1,
    )
  }
  val keyToStepId = steps.zip(previewSteps).associate { (draft, entity) -> draft.key to entity.stepId }
  val resolvedSteps = steps.mapIndexed { index, step ->
    previewSteps[index].copy(
      successNextStepId = step.successNextKey?.let(keyToStepId::get),
      failureNextStepId = step.failureNextKey?.let(keyToStepId::get),
    )
  }
  val primaryCommandId = requireNotNull(steps.firstOrNull()?.commandId)
  return AutomationRuleWithSteps(
    rule = AutomationRuleEntity(
      name = name,
      enabled = enabled,
      initiatorType = primaryInitiator.type,
      initiatorDelayMinutes = delayMinutes,
      commandId = primaryCommandId,
      completionPolicy = "Chain",
      ruleId = ruleId,
    ),
    initiators = previewInitiators.mapIndexed { index, initiator ->
      initiator.toEntity(ruleId = ruleId, orderIndex = index)
    },
    steps = resolvedSteps,
  )
}

internal fun AutomationInitiatorEntity.toDraft(): AutomationInitiatorDraft =
  AutomationInitiatorDraft(
    key = initiatorId.toLong(),
    type = type,
    alarmDelayMinutes = configJson.takeIf { it.isNotBlank() }
      ?.parseFlatJsonObject()
      ?.get("delayMinutes")
      .orEmpty()
      .ifBlank { "15" },
  )

internal fun AutomationInitiatorDraft.toEntity(
  ruleId: Int,
  orderIndex: Int,
): AutomationInitiatorEntity = AutomationInitiatorEntity(
  ruleId = ruleId,
  orderIndex = orderIndex,
  type = type,
  configJson = when (type) {
    AutomationInitiatorType.Alarm.name -> flatJsonObjectOf(
      "delayMinutes" to alarmDelayMinutes.ifBlank { "15" },
    )
    else -> "{}"
  },
)

internal fun AutomationStepDraft.branchMode(): AutomationStepBranchMode = when {
  successNextKey == null && failureNextKey == null -> AutomationStepBranchMode.Terminal
  successNextKey != null && failureNextKey == null -> AutomationStepBranchMode.Success
  successNextKey == null && failureNextKey != null -> AutomationStepBranchMode.Failure
  successNextKey == failureNextKey -> AutomationStepBranchMode.Both
  else -> AutomationStepBranchMode.Split
}

internal fun AutomationStepEntity.branchMode(): AutomationStepBranchMode = when {
  successNextStepId == null && failureNextStepId == null -> AutomationStepBranchMode.Terminal
  successNextStepId != null && failureNextStepId == null -> AutomationStepBranchMode.Success
  successNextStepId == null && failureNextStepId != null -> AutomationStepBranchMode.Failure
  successNextStepId == failureNextStepId -> AutomationStepBranchMode.Both
  else -> AutomationStepBranchMode.Split
}

internal fun AutomationRuleDraft.addStep(): AutomationRuleDraft =
  copy(steps = steps + AutomationStepDraft())

internal fun AutomationRuleDraft.addInitiator(): AutomationRuleDraft =
  copy(initiators = initiators + AutomationInitiatorDraft())

internal fun AutomationRuleDraft.updateStep(
  key: Long,
  update: AutomationStepDraft.() -> AutomationStepDraft,
): AutomationRuleDraft = copy(
  steps = steps.map { step -> if (step.key == key) step.update() else step }
)

internal fun AutomationRuleDraft.updateInitiator(
  key: Long,
  update: AutomationInitiatorDraft.() -> AutomationInitiatorDraft,
): AutomationRuleDraft = copy(
  initiators = initiators.map { initiator -> if (initiator.key == key) initiator.update() else initiator }
)

internal fun AutomationRuleDraft.removeStep(key: Long): AutomationRuleDraft {
  val remaining = steps.filterNot { it.key == key }.map { step ->
    step.copy(
      successNextKey = step.successNextKey.takeUnless { it == key },
      failureNextKey = step.failureNextKey.takeUnless { it == key },
    )
  }
  return copy(steps = remaining.ifEmpty { listOf(AutomationStepDraft()) })
}

internal fun AutomationRuleDraft.removeInitiator(key: Long): AutomationRuleDraft {
  val remaining = initiators.filterNot { it.key == key }
  return copy(initiators = remaining.ifEmpty { listOf(AutomationInitiatorDraft()) })
}

internal fun AutomationRuleDraft.moveStep(key: Long, offset: Int): AutomationRuleDraft {
  val fromIndex = steps.indexOfFirst { it.key == key }
  if (fromIndex < 0) return this
  val toIndex = (fromIndex + offset).coerceIn(0, steps.lastIndex)
  if (fromIndex == toIndex) return this
  val mutableSteps = steps.toMutableList()
  val moved = mutableSteps.removeAt(fromIndex)
  mutableSteps.add(toIndex, moved)
  return copy(steps = mutableSteps)
}

private fun AutomationRuleDraft.hasForwardOnlyStepLinks(): Boolean {
  val stepIndexByKey = steps.withIndex().associate { (index, step) -> step.key to index }
  return steps.withIndex().all { (index, step) ->
    val successOk = step.successNextKey == null || (stepIndexByKey[step.successNextKey] ?: -1) > index
    val failureOk = step.failureNextKey == null || (stepIndexByKey[step.failureNextKey] ?: -1) > index
    successOk && failureOk
  }
}

private fun AutomationInitiatorDraft.isValid(): Boolean = when (type) {
  AutomationInitiatorType.Alarm.name -> alarmDelayMinutes.toIntOrNull() != null
  AutomationInitiatorType.SmsReceived.name -> true
  else -> false
}

private fun AutomationInitiatorDraft.delayMinutesOrDefault(): Int =
  alarmDelayMinutes.toIntOrNull()?.coerceAtLeast(1) ?: 1

private val automationStepKeyGenerator = AtomicLong(0)

internal fun nextAutomationStepKey(): Long = -automationStepKeyGenerator.incrementAndGet()

private val automationInitiatorKeyGenerator = AtomicLong(0)

internal fun nextAutomationInitiatorKey(): Long = -automationInitiatorKeyGenerator.incrementAndGet()
