package io.antarescircuit.antares.web

import io.antarescircuit.jabbah.app.ApplicationDataHolder
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.event.EventHandler
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.edit.BeanProvider
import io.antarescircuit.jabbah.edit.model.text.ScriptProperty
import io.antarescircuit.jabbah.edit.model.text.description.Description
import io.antarescircuit.jabbah.edit.model.text.description.Name
import io.antarescircuit.jabbah.graph.MetaGraph
import io.antarescircuit.jabbah.graph.app.ApplicationModeEvent
import io.antarescircuit.jabbah.graph.ui.scenario.ScenarioSelectionEvent
import io.antarescircuit.jabbah.graph.ui.usecase.UsecaseSelectionEvent
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.Scenario
import io.antarescircuit.jabbah.graph.view.ScenarioStep
import io.antarescircuit.jabbah.graph.view.Usecase
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule
import io.antarescircuit.jabbah.graph.view.scenario.CurrentScenarioMode
import io.antarescircuit.jabbah.graph.view.scenario.ScenarioImpl
import io.antarescircuit.jabbah.graph.view.scenario.ScenarioMode
import io.antarescircuit.jabbah.graph.view.scenario.ScenarioStepImpl
import io.antarescircuit.jabbah.graph.view.usecase.UsecaseImpl
import io.antarescircuit.jabbah.graph.view.usecase.UsecaseRecorder
import io.antarescircuit.jabbah.graph.view.usecase.UsecaseRunner
import io.antarescircuit.jabbah.graph.view.usecase.UsecaseTestRunner

/**
 * Scenarios, scenario steps and use cases of the open circuit, driving the shared `ScenarioViewController` and
 * `UsecaseViewController` like the desktop's `ScenarioViewSwing` and `UsecaseViewSwing`.
 * The web application asks for names and confirmations first; the answers are passed to the controllers'
 * views ([WebScenarioView], [WebUsecaseView]) while their actions run.
 */
@JsExport
class WebScenarios internal constructor(private val ctx: WebEditorContext) {

	private val editViewController get() = ctx.frame().graphPanelViewController.editViewController
	private val controller get() = editViewController.scenarioViewController
	private val usecaseController get() = editViewController.usecaseViewController
	private val scenarioView get() = controller.view as WebScenarioView
	private val usecaseView get() = usecaseController.view as WebUsecaseView
	private val modeHolder get() = ctx.frame().applicationModeHolder
	private val contextHolder get() = ctx.frame().applicationContextHolder

	private val dataHolder: ApplicationDataHolder get() = controller.applicationDataHolder

	private val graphView: GraphView?
		get() = controller.graphView ?: (dataHolder.data?.content as? MetaGraph)?.graph?.graphView

	private val editable: Boolean
		get() = dataHolder.data?.content is MetaGraph && dataHolder.data?.savable?.editable == true && modeHolder.currentMode.isEdit()

	private val actionEvent get() = ActionEvent(null, this, 0, "", 0)

	/** The use case recording in progress, if any (desktop: `RecordUsecasePanel`). */
	private var recorder: UsecaseRecorder? = null
	private var recordingUsecaseId = -1
	private val recordingLog = mutableListOf<String>()
	private var recordingStopped = false

	private val applicationModeHandler: EventHandler<ApplicationModeEvent> = {
		// Stopping the simulation ends the recording, like on the desktop
		if (it.applicationMode.isEdit()) stopRecording()
	}

	init {
		BaseModule.eventBus.register(ApplicationModeEvent::class, applicationModeHandler)
	}

	private fun scenario(id: Int): Scenario? = graphView?.scenarios?.getScenarios()?.firstOrNull { it.id == id }

	private fun step(scenario: Scenario?, id: Int): ScenarioStep? = scenario?.getScenarioSteps()?.firstOrNull { it.id == id }

	private fun usecase(id: Int): Usecase? = graphView?.usecases?.getUsecases()?.firstOrNull { it.id == id }

	/** ---- State */

	/** Returns scenarios, use cases and the related settings of the open circuit as JSON. */
	fun state(): String = ctx.guarded("null") {
		val graphView = graphView ?: return@guarded "null"
		val editable = editable
		val simulating = modeHolder.currentMode.isExecute()
		val mode = CurrentScenarioMode.mode.customName
		val modes = ScenarioMode.entries.map { m ->
			val id = m.customName
			val label = m.toString()
			obj { this.id = id; this.label = label }
		}
		val breakpoints = contextHolder.scenarioBreakpoints.enabled
		val scenarios = graphView.scenarios.getScenarios().map { scenarioJs(it) }
		val usecases = graphView.usecases.getUsecases().map { usecaseJs(it) }
		val selectedScenario = controller.scenario?.id
		val selectedStep = controller.scenarioStep?.id
		val activeScenario = graphView.currentScenario?.id
		val activeStep = graphView.currentScenarioStep?.id
		val selectedUsecase = usecaseController.usecase?.id
		val recording = if (recorder != null) obj {
			this.usecaseId = recordingUsecaseId
			this.active = recorder?.isRecording == true
			this.log = recordingLog.joinToString("\n")
		} else null
		toJson(obj {
			this.editable = editable
			this.simulating = simulating
			this.mode = mode
			this.modes = modes.toJsArray()
			this.breakpoints = breakpoints
			this.scenarios = scenarios.toJsArray()
			this.selectedScenario = selectedScenario
			this.selectedStep = selectedStep
			this.activeScenario = activeScenario
			this.activeStep = activeStep
			this.usecases = usecases.toJsArray()
			this.selectedUsecase = selectedUsecase
			this.recording = recording
		})
	}

	private fun scenarioJs(s: Scenario): dynamic {
		val steps = s.getScenarioSteps().map { stepJs(it) }
		val condition = (s as? ScenarioImpl)?.conditionProperty?.script ?: ""
		return obj {
			this.id = s.id
			this.name = s.name.value
			this.description = s.description.value ?: ""
			this.condition = condition
			this.steps = steps.toJsArray()
		}
	}

	private fun stepJs(s: ScenarioStep): dynamic {
		val impl = s as? ScenarioStepImpl
		val condition = impl?.conditionProperty?.script ?: ""
		val onEntry = impl?.onEntryProperty?.script ?: ""
		val onExit = impl?.onExitProperty?.script ?: ""
		return obj {
			this.id = s.id
			this.name = s.name.value
			this.description = s.description.value ?: ""
			this.condition = condition
			this.highlightIds = s.highlightIds ?: ""
			this.onEntry = onEntry
			this.onExit = onExit
		}
	}

	private fun usecaseJs(u: Usecase): dynamic {
		val execScript = u.executionScript.script ?: ""
		val testScript = u.testScript ?: ""
		val hasTest = u.hasTest
		return obj {
			this.id = u.id
			this.name = u.name.value
			this.description = u.description.value ?: ""
			this.execScript = execScript
			this.testScript = testScript
			this.hasTest = hasTest
		}
	}

	/** ---- Scenarios */

	/**
	 * Selects a scenario and optionally one of its steps (-1 for none) like the desktop's scenario tree.
	 * The selected scenario's and step's descriptions and highlights are shown in the circuit.
	 */
	fun select(scenarioId: Int, stepId: Int) = ctx.guarded(Unit) {
		val graphView = graphView ?: return@guarded
		if (modeHolder.currentMode.isExecute()) return@guarded
		val scenario = scenario(scenarioId)
		BaseModule.eventBus.post(ScenarioSelectionEvent(graphView, scenario, step(scenario, stepId)))
		ctx.notifyChanged()
	}

	/** Adds a scenario named [name] (desktop: `AddScenarioAction`) and selects it. Returns its ID or -1. */
	fun addScenario(name: String): Int = ctx.guarded(-1) {
		select(-1, -1)
		val action = controller.addScenarioAction
		if (!action.enabled) return@guarded -1
		scenarioView.answer(name) { action.execute(actionEvent) }
		val id = graphView?.scenarios?.getScenarios()?.maxOfOrNull { it.id } ?: return@guarded -1
		select(id, -1)
		id
	}

	/** Adds a step named [name] to a scenario (desktop: `AddScenarioStepAction`) and selects it. Returns its ID or -1. */
	fun addStep(scenarioId: Int, name: String): Int = ctx.guarded(-1) {
		select(scenarioId, -1)
		val action = controller.addScenarioStepAction
		if (!action.enabled) return@guarded -1
		scenarioView.answer(name) { action.execute(actionEvent) }
		val id = scenario(scenarioId)?.getScenarioSteps()?.maxOfOrNull { it.id } ?: return@guarded -1
		select(scenarioId, id)
		id
	}

	/** Deletes a scenario, or one of its steps if [stepId] >= 0. The web application has asked for confirmation. */
	fun delete(scenarioId: Int, stepId: Int) = ctx.guarded(Unit) {
		select(scenarioId, stepId)
		val action = if (stepId >= 0) controller.deleteScenarioStepAction else controller.deleteScenarioAction
		if (action.enabled) scenarioView.answer(confirmed = true) { action.execute(actionEvent) }
		select(if (stepId >= 0) scenarioId else -1, -1)
	}

	/** Moves a scenario, or one of its steps if [stepId] >= 0, up (`delta` < 0) or down. */
	fun move(scenarioId: Int, stepId: Int, delta: Int) = ctx.guarded(Unit) {
		if (!editable) return@guarded
		val service = GraphViewModule.scenarioAppService
		val scenario = scenario(scenarioId) ?: return@guarded
		if (stepId >= 0) {
			val step = step(scenario, stepId) ?: return@guarded
			val index = scenario.indexOf(step)
			val target = index + delta
			if (target < 0 || target >= scenario.stepCount) return@guarded
			service.moveScenarioStep(dataHolder, scenarioId, stepId, if (delta > 0) target + 1 else target)
		} else {
			val all = graphView!!.scenarios.getScenarios().toList()
			val index = all.indexOf(scenario)
			val target = index + delta
			if (target < 0 || target >= all.size) return@guarded
			service.moveScenario(dataHolder, scenarioId, if (delta > 0) target + 1 else target)
		}
		ctx.notifyChanged()
	}

	/**
	 * Changes a property of a scenario, or of one of its steps if [stepId] >= 0, as undoable command
	 * (desktop: `ScenarioImplBeanInfo`, `ScenarioStepImplBeanInfo`).
	 * Keys: name, description, condition; for steps also highlightIds, onEntry, onExit.
	 */
	fun setProperty(scenarioId: Int, stepId: Int, key: String, value: String) = ctx.guarded(Unit) {
		if (!editable) return@guarded
		val scenario = scenario(scenarioId) ?: return@guarded
		if (stepId >= 0) {
			val step = step(scenario, stepId) as? ScenarioStepImpl ?: return@guarded
			val provider: BeanProvider = { e, ids ->
				val parts = ids.first().split('.').map { it.toInt() }
				listOf((e.drawing as GraphView).scenarios.get(parts[0]).getStep(parts[1]) as ScenarioStepImpl)
			}
			fun <V> run(baseKey: String, newValue: V, get: (ScenarioStepImpl) -> V, set: (ScenarioStepImpl, V) -> Unit) =
				runBeanCommand(ctx.editor, baseKey, provider, "$scenarioId.$stepId", newValue, { get(it as ScenarioStepImpl) }, { b, v -> set(b as ScenarioStepImpl, v) })
			when (key) {
				"name" -> run("graph.property.scenario.name", Name(step.name.withText(value)), { it.name }, { s, v -> s.name = v })
				"description" -> run("graph.property.scenarioStep.description", Description(step.description.withText(value)), { it.description }, { s, v -> s.description = v })
				"condition" -> run("graph.property.scenario.condition", value, { it.conditionProperty.script ?: "" }, { s, v -> s.conditionProperty = ScriptProperty(v) })
				"highlightIds" -> run("graph.property.scenario.highlightIds", value, { it.highlightIds ?: "" }, { s, v -> s.highlightIds = v.ifBlank { null } })
				"onEntry" -> run("graph.property.scenario.onEntry", value, { it.onEntryProperty.script ?: "" }, { s, v -> s.onEntryProperty = ScriptProperty(v) })
				"onExit" -> run("graph.property.scenario.onExit", value, { it.onExitProperty.script ?: "" }, { s, v -> s.onExitProperty = ScriptProperty(v) })
			}
		} else {
			val impl = scenario as? ScenarioImpl ?: return@guarded
			val provider: BeanProvider = { e, ids -> listOf((e.drawing as GraphView).scenarios.get(ids.first().toInt()) as ScenarioImpl) }
			fun <V> run(baseKey: String, newValue: V, get: (ScenarioImpl) -> V, set: (ScenarioImpl, V) -> Unit) =
				runBeanCommand(ctx.editor, baseKey, provider, "$scenarioId", newValue, { get(it as ScenarioImpl) }, { b, v -> set(b as ScenarioImpl, v) })
			when (key) {
				"name" -> run("graph.property.scenario.name", Name(impl.name.withText(value)), { it.name }, { s, v -> s.name = v })
				"description" -> run("graph.property.scenario.description", Description(impl.description.withText(value)), { it.description }, { s, v -> s.description = v })
				"condition" -> run("graph.property.scenario.condition", value, { it.conditionProperty.script ?: "" }, { s, v -> s.conditionProperty = ScriptProperty(v) })
			}
		}
		ctx.editor.view.repaint()
		ctx.notifyChanged()
	}

	/** Parses a script of a scenario or step; returns the first error as JSON, or "null". */
	fun check(scenarioId: Int, stepId: Int, script: String): String = ctx.guarded("null") {
		val scenario = scenario(scenarioId) ?: return@guarded "null"
		if (stepId >= 0) {
			val step = step(scenario, stepId) as? ScenarioStepImpl ?: return@guarded "null"
			checkScript(script) { step.createParser(it, null) }
		} else {
			val impl = scenario as? ScenarioImpl ?: return@guarded "null"
			checkScript(script) { impl.createParser(it, null) }
		}
	}

	/** Sets when scenario texts are shown during simulation (desktop: `ScenarioModeMenu`). */
	fun setMode(mode: String) = ctx.guarded(Unit) {
		CurrentScenarioMode.mode = ScenarioMode.withName(mode)
		ctx.notifyChanged()
	}

	/** Pauses the simulation whenever a scenario step starts (desktop: `ScenarioBreakpointAction`). */
	fun setBreakpoints(enabled: Boolean) = ctx.guarded(Unit) {
		contextHolder.scenarioBreakpoints.enabled = enabled
		ctx.notifyChanged()
	}

	/** ---- Use cases */

	/** Selects a use case (-1 for none). */
	fun selectUsecase(id: Int) = ctx.guarded(Unit) {
		val graphView = graphView ?: return@guarded
		BaseModule.eventBus.post(UsecaseSelectionEvent(graphView, usecase(id)))
		ctx.notifyChanged()
	}

	/** Adds a use case named [name] (desktop: `AddUsecaseAction`) and selects it. Returns its ID or -1. */
	fun addUsecase(name: String): Int = ctx.guarded(-1) {
		selectUsecase(-1)
		val action = usecaseController.addUsecaseAction
		action.updateEnabled()
		if (!action.enabled) return@guarded -1
		usecaseView.answer(name) { action.execute(actionEvent) }
		val id = graphView?.usecases?.getUsecases()?.maxOfOrNull { it.id } ?: return@guarded -1
		selectUsecase(id)
		id
	}

	/** Deletes a use case (desktop: `DeleteUsecaseAction`). The web application has asked for confirmation. */
	fun deleteUsecase(id: Int) = ctx.guarded(Unit) {
		if (!editable || usecase(id) == null) return@guarded
		selectUsecase(-1)
		GraphViewModule.usecaseAppService.deleteUsecase(dataHolder, id)
		ctx.notifyChanged()
	}

	/** Duplicates a use case (desktop: `DuplicateUsecaseAction`) and selects the copy. Returns its ID or -1. */
	fun duplicateUsecase(id: Int, name: String): Int = ctx.guarded(-1) {
		if (!editable || usecase(id) == null) return@guarded -1
		GraphViewModule.usecaseAppService.duplicateUsecase(dataHolder, id, name).also { selectUsecase(it) }
	}

	/** Changes a property of a use case as undoable command (desktop: `UsecaseImplBeanInfo`). Keys: name, description, execScript, testScript. */
	fun setUsecaseProperty(id: Int, key: String, value: String) = ctx.guarded(Unit) {
		if (!editable) return@guarded
		val usecase = usecase(id) as? UsecaseImpl ?: return@guarded
		val provider: BeanProvider = { e, ids -> listOf((e.drawing as GraphView).usecases.get(ids.first().toInt()) as UsecaseImpl) }
		fun <V> run(baseKey: String, newValue: V, get: (UsecaseImpl) -> V, set: (UsecaseImpl, V) -> Unit) =
			runBeanCommand(ctx.editor, baseKey, provider, "$id", newValue, { get(it as UsecaseImpl) }, { b, v -> set(b as UsecaseImpl, v) })
		when (key) {
			"name" -> run("graph.property.usecase.name", Name(usecase.name.withText(value)), { it.name }, { u, v -> u.name = v })
			"description" -> run("edit.property.description", Description(usecase.description.withText(value)), { it.description }, { u, v -> u.description = v })
			"execScript" -> run("graph.property.usecase.execScript", value, { it.executionScript.script ?: "" }, { u, v -> u.executionScript = ScriptProperty(v) })
			"testScript" -> run("graph.property.usecase.testScript", value, { it.testScript ?: "" }, { u, v -> u.testScriptProperty = ScriptProperty(v) })
		}
		ctx.notifyChanged()
	}

	/** Parses a script of a use case; returns the first error as JSON, or "null". */
	fun checkUsecase(id: Int, script: String): String = ctx.guarded("null") {
		val usecase = usecase(id) as? UsecaseImpl ?: return@guarded "null"
		checkScript(script) { usecase.createParser(it, null) }
	}

	/** Starts the simulation and plays a use case (desktop: `RunUsecaseAction`). */
	fun runUsecase(id: Int) = ctx.guarded(Unit) {
		val usecase = usecase(id) ?: return@guarded
		if (!modeHolder.currentMode.isEdit()) return@guarded
		ctx.saveNow()
		UsecaseRunner(usecase, graphView!!, contextHolder.scheduler, modeHolder).run()
		ctx.notifyChanged()
	}

	/** Runs the test of a use case, or of all use cases with tests if [id] is -1 (desktop: `RunSingleUsecaseTestAction`, `RunAllTestsAction`). */
	fun runUsecaseTests(id: Int) = ctx.guarded(Unit) {
		val graphView = graphView ?: return@guarded
		if (!modeHolder.currentMode.isEdit()) return@guarded
		val usecases = if (id >= 0) listOfNotNull(usecase(id)?.takeIf { it.hasTest }) else graphView.usecases.withTests()
		if (usecases.isEmpty()) return@guarded
		ctx.saveNow()
		UsecaseTestRunner(usecases, graphView, contextHolder.scheduler, modeHolder).run()
		ctx.notifyChanged()
	}

	/**
	 * Starts recording clicks and key presses in the circuit for a use case (desktop: `RecordUsecasePanel`).
	 * @param delayMs the time between pressing and releasing
	 * @param timeBetweenMs the time between two actions if not recording in [realtime]
	 */
	fun startRecording(id: Int, realtime: Boolean, delayMs: Int, timeBetweenMs: Int) = ctx.guarded(Unit) {
		if (usecase(id) == null || !editable || recorder?.isRecording == true) return@guarded
		ctx.saveNow()
		recordingUsecaseId = id
		recordingLog.clear()
		recorder = UsecaseRecorder(modeHolder, { recordingLog.add(it); ctx.notifyChanged() }, contextHolder.scheduler).also {
			it.start(realtime, delayMs.coerceAtLeast(1) * 1_000, timeBetweenMs.coerceAtLeast(1) * 1_000)
		}
		ctx.notifyChanged()
	}

	/** Stops recording; the recorded statements are kept until [saveRecording] or [discardRecording]. */
	fun stopRecording() = ctx.guarded(Unit) {
		val recorder = recorder ?: return@guarded
		if (recorder.isRecording && !recordingStopped) {
			recordingStopped = true
			try {
				recorder.stop()
			} finally {
				recordingStopped = false
			}
		}
		ctx.notifyChanged()
	}

	/** Stores the recorded statements as execution script of the use case. */
	fun saveRecording() = ctx.guarded(Unit) {
		stopRecording()
		if (recorder != null && usecase(recordingUsecaseId) != null) {
			GraphViewModule.usecaseAppService.recordUsecase(dataHolder, recordingUsecaseId, recordingLog.joinToString("\n"))
		}
		discardRecording()
	}

	fun discardRecording() = ctx.guarded(Unit) {
		stopRecording()
		recorder = null
		recordingUsecaseId = -1
		recordingLog.clear()
		ctx.notifyChanged()
	}

	/** Default delays of the recorder in ms: [press-release delay, time between actions]. */
	fun recordingDefaults(): Array<Int> = ctx.guarded(arrayOf(2, 500)) {
		arrayOf(
			BaseModule.properties.getInt(UsecaseRecorder.PROP_DEF_DELAY_MS),
			BaseModule.properties.getInt(UsecaseRecorder.PROP_DEF_TIME_BETWEEN_CLICKS_MS),
		)
	}
}
