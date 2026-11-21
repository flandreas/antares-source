package io.antarescircuit.antares.web

import io.antarescircuit.antares.model.DigitalGraph
import io.antarescircuit.antares.model.module.AntaresModelModule
import io.antarescircuit.antares.model.testcase.CombinedTestRunResult
import io.antarescircuit.antares.model.testcase.MatchedValue
import io.antarescircuit.antares.model.testcase.TestRunResult
import io.antarescircuit.antares.model.testcase.TestVector
import io.antarescircuit.antares.model.testcase.Testcase
import io.antarescircuit.antares.model.testcase.TestcaseService
import io.antarescircuit.antares.model.testcase.Value
import io.antarescircuit.jabbah.app.ApplicationDataHolder
import io.antarescircuit.jabbah.base.Bean
import io.antarescircuit.jabbah.base.dsl.DslError
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.parser.Parser
import io.antarescircuit.jabbah.base.richtext.RichText
import io.antarescircuit.jabbah.edit.AbstractPropertyCommand
import io.antarescircuit.jabbah.edit.BeanProvider
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.model.ComponentMessage
import io.antarescircuit.jabbah.edit.model.ComponentMessageType
import io.antarescircuit.jabbah.edit.model.text.ScriptProperty
import io.antarescircuit.jabbah.edit.model.text.description.Description
import io.antarescircuit.jabbah.edit.model.text.description.Name
import io.antarescircuit.jabbah.graph.MetaGraph
import io.antarescircuit.jabbah.graph.library.LibraryModule
import io.antarescircuit.jabbah.graph.view.GraphView

/**
 * Test cases of digital circuits (desktop: `TestcaseViewSwing`, `TestcaseActions`, `TestRunResultsPanel`).
 * Changes are executed as undoable commands of the editor's command manager, like on the desktop.
 * Scenarios and use cases are provided by [scenarios].
 */
@JsExport
class WebTestcases internal constructor(private val ctx: WebEditorContext) {

	/** Scenarios and use cases of the open circuit. */
	val scenarios: WebScenarios = WebScenarios(ctx)

	private val service get() = AntaresModelModule.testcaseAppService

	private val dataHolder: ApplicationDataHolder
		get() = ctx.frame().graphPanelViewController.editViewController.scenarioViewController.applicationDataHolder

	private val metaGraph: MetaGraph? get() = dataHolder.data?.content as? MetaGraph

	private val circuit: DigitalGraph? get() = metaGraph?.graph?.graphView?.graph as? DigitalGraph

	/** Like the desktop's `AbstractTestcaseAction`: an editable digital circuit in edit mode. */
	private val editable: Boolean
		get() = circuit != null && dataHolder.data?.savable?.editable == true && ctx.frame().applicationModeHolder.currentMode.isEdit()

	private fun testcase(id: Int): Testcase? = circuit?.testcases?.testcases?.firstOrNull { it.id == id }

	/** Returns the test cases of the open circuit as JSON. */
	fun list(): String = ctx.guarded("null") {
		val circuit = circuit
		val editable = editable
		val simulating = ctx.frame().applicationModeHolder.currentMode.isExecute()
		val items = circuit?.testcases?.testcases?.map { t ->
			obj {
				this.id = t.id
				this.name = t.name.value
				this.description = t.description.value ?: ""
				this.script = t.testVectors.script ?: ""
				this.ignored = t.ignored
				this.skipPropDelayCheck = t.skipPropDelayConsistenceCheck
				this.iterations = t.numberOfIterations
			}
		} ?: emptyList()
		toJson(obj {
			this.digital = circuit != null
			this.editable = editable
			this.simulating = simulating
			this.items = items.toJsArray()
		})
	}

	/** Adds a new test case and returns its ID, or -1. */
	fun add(name: String): Int = ctx.guarded(-1) {
		if (!editable) return@guarded -1
		service.addTestcase(dataHolder, Testcase(name)).also { ctx.notifyChanged() }
	}

	/** Duplicates a test case and returns the ID of the copy, or -1. */
	fun duplicate(id: Int, name: String): Int = ctx.guarded(-1) {
		if (!editable || testcase(id) == null) return@guarded -1
		service.duplicateTestcase(dataHolder, id, name).also { ctx.notifyChanged() }
	}

	fun delete(id: Int) = ctx.guarded(Unit) {
		if (!editable || testcase(id) == null) return@guarded
		service.deleteTestcase(dataHolder, id)
		ctx.notifyChanged()
	}

	/** Moves a test case up (`delta` < 0) or down. */
	fun move(id: Int, delta: Int) = ctx.guarded(Unit) {
		val testcases = circuit?.testcases ?: return@guarded
		if (!editable) return@guarded
		val index = testcases.indexOfTestcase(id)
		val target = index + delta
		if (index < 0 || target < 0 || target >= testcases.testcases.size) return@guarded
		// Desktop semantics (drag & drop): the index to insert before, counted before removal
		service.moveTestcase(dataHolder, id, if (delta > 0) target + 1 else target)
		ctx.notifyChanged()
	}

	/**
	 * Changes a property of a test case as undoable command (desktop: `TestcaseBeanInfo`).
	 * Keys: name, description, script, ignored, skipPropDelayCheck (values "true"/"false"), iterations.
	 */
	fun setProperty(id: Int, key: String, value: String) = ctx.guarded(Unit) {
		val testcase = testcase(id) ?: return@guarded
		if (!editable) return@guarded
		val provider: BeanProvider = { e, ids -> listOf(((e.drawing as GraphView).graph as DigitalGraph).testcases.get(ids.first().toInt())) }
		fun <V> run(baseKey: String, newValue: V, get: (Testcase) -> V, set: (Testcase, V) -> Unit) =
			runBeanCommand(ctx.editor, baseKey, provider, id.toString(), newValue, { get(it as Testcase) }, { b, v -> set(b as Testcase, v) })
		when (key) {
			"name" -> run("edit.property.name", Name(testcase.name.withText(value)), { it.name }, { t, v -> t.name = v })
			"description" -> run("edit.property.description", Description(testcase.description.withText(value)), { it.description }, { t, v -> t.description = v })
			"script" -> run("antares.testcase.testVectors", value, { it.testVectors.script ?: "" }, { t, v -> t.testVectors = ScriptProperty(v) })
			"ignored" -> run("antares.testcase.ignored", value == "true", { it.ignored }, { t, v -> t.ignored = v })
			"skipPropDelayCheck" -> run("antares.testcase.skipPropDelayConsistencyTest", value == "true", { it.skipPropDelayConsistenceCheck }, { t, v -> t.skipPropDelayConsistenceCheck = v })
			"iterations" -> value.toIntOrNull()?.takeIf { it > 0 }?.let { n ->
				run("antares.testcase.numberOfIterations", n, { it.numberOfIterations }, { t, v -> t.numberOfIterations = v })
			}
		}
		ctx.notifyChanged()
	}

	/** Parses [script] like the test case's script editor; returns the first error as JSON, or "null". */
	fun check(id: Int, script: String): String = ctx.guarded("null") {
		val testcase = testcase(id) ?: return@guarded "null"
		checkScript(script) { testcase.createParser(it, null) }
	}

	/** Runs the test cases with [ids] of the open circuit and returns the results as JSON. */
	fun run(ids: Array<Int>): String = ctx.guarded("[]") {
		val metaGraph = metaGraph ?: return@guarded "[]"
		val testcases = circuit?.testcases?.testcases?.filter { it.id in ids } ?: return@guarded "[]"
		report(TestcaseService.run(metaGraph, testcases))
	}

	/** Runs all test cases of the open circuit (desktop: `RunCircuitTestcasesAction`). */
	fun runAll(): String = ctx.guarded("[]") {
		val metaGraph = metaGraph ?: return@guarded "[]"
		report(TestcaseService.run(metaGraph, circuit?.testcases?.testcases ?: emptyList()))
	}

	/** Runs all test cases of all circuits of the open project (desktop: `RunLibraryTestcasesAction`). */
	fun runProject(): String = ctx.guarded("[]") {
		val library = LibraryModule.libraryHolder.l ?: return@guarded "[]"
		// Tests run on the stored circuits, so store the open one first
		ctx.saveNow()
		report(TestcaseService.runAllLibraryTests(library))
	}

	/** Posts the desktop's "test run done" message and converts [results] to JSON. */
	private fun report(results: List<CombinedTestRunResult>): String {
		if (results.isNotEmpty()) {
			val type = if (results.any { it.failed }) ComponentMessageType.Error else ComponentMessageType.Info
			BaseModule.eventBus.post(ComponentMessage(type, source = null, messageKey = "antares.testcase.results.done.text"))
		}
		return toJson(results.map { resultJs(it) }.toJsArray())
	}

	private fun resultJs(r: CombinedTestRunResult): dynamic {
		val runs = listOfNotNull(r.circuitResults, r.scriptResults).map { runJs(it) }
		val general = r.generalTestResults.map { g -> obj { this.error = g.error; this.description = g.description } }
		return obj {
			this.circuit = RichText.stripToPlainText(r.source.name.value)
			this.circuitUuid = r.source.uuid.id
			this.testcaseId = r.testcase.id
			this.testcase = r.testcase.name.value
			this.ignored = r.ignored
			this.error = r.error
			this.failedCount = r.totalFailedCount
			this.general = general.toJsArray()
			this.runs = runs.toJsArray()
		}
	}

	private fun runJs(r: TestRunResult): dynamic {
		val type = if (r.type == TestRunResult.Type.Circuit) "circuit" else "script"
		val label = r.type.toString()
		val tooltip = r.type.tooltip()
		val columns = r.names.mapIndexed { i, name -> obj { this.name = name; this.output = r.isOutput[i] } }
		val rows = r.collector.testVectors.map { rowJs(it) }
		return obj {
			this.type = type
			this.label = label
			this.tooltip = tooltip
			this.error = r.errorMessage
			this.failedCount = r.failedCount
			this.duration = r.duration.toDouble()
			this.columns = columns.toJsArray()
			this.rows = rows.toJsArray()
		}
	}

	private fun rowJs(v: TestVector): dynamic {
		val kind = when (v.type) {
			TestVector.Type.Top -> "top"
			TestVector.Type.RunFirst -> "first"
			TestVector.Type.RunLine -> "line"
			TestVector.Type.RunLast -> "last"
		}
		val cells = v.values.map { value ->
			val state = when (value.state) {
				Value.State.PASSED -> "passed"
				Value.State.FAILED -> "failed"
				Value.State.NORMAL -> "normal"
			}
			val expected = (value as? MatchedValue)?.expected
			val dontCare = expected?.type == Value.Type.DONT_CARE
			val text = value.toString()
			obj {
				this.text = text
				this.state = state
				this.expected = expected?.toString()
				this.dontCare = dontCare
			}
		}
		return obj {
			this.kind = kind
			this.description = v.description
			this.cells = cells.toJsArray()
		}
	}
}

/** Parses [script] with the parser created by [createParser]; returns the first error as JSON, or "null". */
internal fun checkScript(script: String, createParser: (String) -> Parser): String {
	if (script.isBlank()) return "null"
	return try {
		createParser(script).parse()
		"null"
	} catch (e: DslError) {
		val message = e.message ?: ""
		toJson(obj {
			this.message = message
			this.row = e.location.row
			this.column = e.location.column
		})
	} catch (t: Throwable) {
		val message = t.message ?: t.toString()
		toJson(obj { this.message = message; this.row = 0; this.column = 0 })
	}
}

/**
 * Executes an undoable property change of the single bean with ID [beanId] provided by [beanProvider],
 * like the desktop's `CommandPropertySwing` does for beans that aren't components (test cases, scenarios, …).
 */
internal fun <V> runBeanCommand(
	editor: Editor,
	baseKey: String,
	beanProvider: BeanProvider,
	beanId: String,
	newValue: V,
	getter: (Any) -> V?,
	setter: (Any, V) -> Unit,
) {
	val command = object : AbstractPropertyCommand<V>(editor, baseKey, emptyArray(), beanProvider, listOf(beanId), newValue) {
		override fun getValue(bean: Bean): V? = getter(bean)
		// Old values were read from the bean, so they have the bean's (possibly non-null) type
		@Suppress("UNCHECKED_CAST")
		override fun setValue(bean: Bean, value: V?) = setter(bean, value as V)
	}
	command.establishOldValues()
	if (command.valueChanged) editor.commandManager.execute(command)
}
