package io.antarescircuit.antares.web

import io.antarescircuit.antares.model.AntaresGraphTypes
import io.antarescircuit.antares.view.analog.CurrentFlowAnimationSpeed
import io.antarescircuit.jabbah.base.Action
import io.antarescircuit.jabbah.base.Status
import io.antarescircuit.jabbah.base.StatusType
import io.antarescircuit.jabbah.base.System
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.ActionEvent
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.execution.module.ExecutionModule
import io.antarescircuit.jabbah.graph.model.module.GraphModelModule
import io.antarescircuit.jabbah.graph.model.net.SignalConflictBehaviour
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.graph.view.GraphElementView
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.GraphViewAnimationType
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule
import io.antarescircuit.jabbah.graph.view.net.node.NodeView
import io.antarescircuit.jabbah.graph.view.scenario.CurrentScenarioMode
import io.antarescircuit.jabbah.graph.view.scenario.ScenarioMode

/**
 * Simulation options and view toggles of the desktop's "Simulation" and "View" menus
 * (`GraphMenuBarBuilder`, `AntaresMenuBarBuilder`): deep simulation, pause on issue,
 * soft breakpoints, simulation time, interactive propagation delay, animation, noise, signal conflicts,
 * scenario texts, oscilloscope, plus the analog current flow speed and component IDs.
 */
@JsExport
class WebSimulation internal constructor(private val ctx: WebEditorContext) {

	/** A checkable option: its state, whether it can be changed now, and how to toggle it. */
	private class Option(val checked: () -> Boolean, val enabled: () -> Boolean, val toggle: () -> Unit)

	private val panel get() = ctx.frame().graphPanelViewController
	private val contextHolder get() = ctx.frame().applicationContextHolder
	private val scheduler get() = contextHolder.scheduler
	private val animation get() = GraphViewModule.currentGraphViewAnimationType

	/** The circuit's view, if it shows a [GraphView] (the oscilloscope is part of the circuit's drawing). */
	@Suppress("UNCHECKED_CAST")
	private val graphDrawingView: DrawingView<GraphElementView<*>, GraphView>?
		get() = ctx.editor.view.takeIf { it.drawing is GraphView } as? DrawingView<GraphElementView<*>, GraphView>

	/** Shows or hides the oscilloscope, like the desktop's `OscilloscopeVisibilityAction`. */
	private val oscilloscope = Option(
		{ graphDrawingView?.let { oscilloscopes.isOscilloscopeDisplayed(it.drawing) } ?: false },
		{ graphDrawingView?.editable == true && !scheduler.isActive },
	) {
		graphDrawingView?.let { view ->
			if (oscilloscopes.isOscilloscopeDisplayed(view.drawing)) oscilloscopes.hideOscilloscope(view)
			else oscilloscopes.displayOscilloscope(view)
		}
	}

	private val oscilloscopes get() = GraphViewModule.oscilloscopeViewService

	private val noiseGenerators get() = listOf(ExecutionModule.noNoiseGenerator, ExecutionModule.randomNoiseGenerator)

	/** Like a Swing check box menu item: toggles [Action.selected] first, then executes the action. */
	private fun actionOption(action: () -> Action) = Option({ action().selected }, { action().enabled }) {
		val a = action()
		a.selected = !a.selected
		a.execute(ActionEvent(null, this, 0, "toggle", System.currentTimeMillis()))
	}

	private val options: Map<String, Option> by lazy {
		mapOf(
			"deepSimulation" to actionOption { panel.executionDepthAction },
			"pauseOnIssue" to actionOption { panel.stopOnIssueAction },
			"softBreakpoints" to actionOption { panel.enableSoftBreakpointsAction },
			"simulationTime" to actionOption { panel.simulationTimeStatusEnabledAction },
			"interactiveDelay" to actionOption { panel.enableInteractivePropagationDelayAction },
			"oscilloscope" to oscilloscope,
			"animation" to Option({ animation.graphViewAnimationType == GraphViewAnimationType.Animation }, { true }) {
				val on = animation.graphViewAnimationType == GraphViewAnimationType.Animation
				animation.graphViewAnimationType = if (on) GraphViewAnimationType.None else GraphViewAnimationType.Animation
			},
			"scenarioBreakpoints" to Option({ contextHolder.scenarioBreakpoints.enabled }, { true }) {
				contextHolder.scenarioBreakpoints.enabled = !contextHolder.scenarioBreakpoints.enabled
			},
		)
	}

	/**
	 * Returns all options as JSON: `options` (name → `{checked, enabled}`), the selected `noise`, `conflict` and
	 * `scenarioMode` with their choices, the analog `currentFlow` speed, the simulation `time` shown in the
	 * desktop's status bar (or `null`), whether the execution time can be reset and the selected component IDs.
	 */
	fun state(): String = ctx.guarded("null") {
		val optionsJs: dynamic = obj { }
		options.forEach { (name, option) ->
			val checked = option.checked()
			val enabled = option.enabled()
			optionsJs[name] = obj {
				this.checked = checked
				this.enabled = enabled
			}
		}
		val noise = ExecutionModule.noiseGeneratorHolder.current.nameKey
		val noiseChoices = noiseGenerators.map { choice(it.nameKey, Translations.getString("${it.nameKey}.name")) }
		val conflict = GraphModelModule.signalConflictBehaviourHolder.current.customName
		val conflictChoices = SignalConflictBehaviour.entries.map { choice(it.customName, it.toString()) }
		val scenarioMode = CurrentScenarioMode.mode.customName
		val scenarioModes = ScenarioMode.entries.map { choice(it.customName, it.toString()) }
		val analog = (ctx.editor.drawing as? GraphView)?.graph?.type == AntaresGraphTypes.Analog
		val speed = CurrentFlowAnimationSpeed.speed
		val turbo = CurrentFlowAnimationSpeed.turbo
		val minSpeed = CurrentFlowAnimationSpeed.MIN_SPEED
		val maxSpeed = CurrentFlowAnimationSpeed.MAX_SPEED
		val time = if (ctx.frame().applicationModeHolder.currentMode.isExecute()) Status[StatusType.Small] else null
		val canResetTime = scheduler.isActive
		val ids = selectedIds()
		toJson(obj {
			this.options = optionsJs
			this.noise = noise
			this.noiseChoices = noiseChoices.toJsArray()
			this.conflict = conflict
			this.conflictChoices = conflictChoices.toJsArray()
			this.scenarioMode = scenarioMode
			this.scenarioModes = scenarioModes.toJsArray()
			this.analog = analog
			this.currentFlow = obj {
				this.speed = speed
				this.turbo = turbo
				this.min = minSpeed
				this.max = maxSpeed
			}
			this.time = time
			this.canResetTime = canResetTime
			this.selectedIds = ids
		})
	}

	private fun choice(id: String, label: String): dynamic = obj {
		this.id = id
		this.label = label
	}

	/** Toggles option [name], see [state]. */
	fun toggle(name: String) = ctx.guarded(Unit) {
		val option = options[name] ?: return@guarded
		if (option.enabled()) option.toggle()
		ctx.notifyChanged()
	}

	/** Selects the noise generator [id] (its name key). */
	fun setNoise(id: String) = ctx.guarded(Unit) {
		noiseGenerators.firstOrNull { it.nameKey == id }?.let { ExecutionModule.noiseGeneratorHolder.current = it }
		ctx.notifyChanged()
	}

	/** Sets how signal conflicts are reported, like the desktop's `SignalConflictBehaviourAction`. */
	fun setConflictBehaviour(id: String) = ctx.guarded(Unit) {
		val behaviour = SignalConflictBehaviour.withName(id)
		BaseModule.properties.customize(SignalConflictBehaviour.PROP_SIGNAL_CONFLICT_BEHAVIOUR, behaviour.customName)
		GraphModelModule.signalConflictBehaviourHolder.current = behaviour
		ctx.notifyChanged()
	}

	/** Sets when scenario texts are shown during simulation. */
	fun setScenarioMode(id: String) = ctx.guarded(Unit) {
		CurrentScenarioMode.mode = ScenarioMode.withName(id)
		ctx.notifyChanged()
	}

	/** Sets the speed of the current flow animation in analog circuits. */
	fun setCurrentFlow(speed: Int, turbo: Boolean) = ctx.guarded(Unit) {
		CurrentFlowAnimationSpeed.speed = speed.coerceIn(CurrentFlowAnimationSpeed.MIN_SPEED, CurrentFlowAnimationSpeed.MAX_SPEED)
		CurrentFlowAnimationSpeed.turbo = turbo
		ctx.notifyChanged()
	}

	/**
	 * Resets the simulation time to 0, like the desktop's `ResetExecutionTimeAction`.
	 * Returns an error message if that isn't possible (pending events), otherwise an empty string.
	 */
	fun resetExecutionTime(): String = ctx.guarded("") {
		if (!scheduler.isActive) return@guarded ""
		if (!scheduler.isQueueEmpty) return@guarded Translations.getString("execution.action.resetExecutionTime.queueNotEmpty.msg")
		scheduler.resetExecutionTime()
		ctx.notifyChanged()
		""
	}

	/** The IDs of the selected components as shown by the desktop's `DisplayIdsAction` (without nodes). */
	private fun selectedIds(): String =
		ctx.editor.view.selectionManager.selection
			.filter { it !is NodeView<*> }
			.map { (it as Component).id }
			.sorted()
			.joinToString(",")
}
