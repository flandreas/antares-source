package ch.scorpion.jabbah.graph.dsl

import ch.scorpion.jabbah.base.IssueImpl
import ch.scorpion.jabbah.base.IssueSeverity
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.dsl.ScopedSymbolTable
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.graph.model.GraphInput
import ch.scorpion.jabbah.graph.script.ScriptErrorHandler
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphPortView
import ch.scorpion.jabbah.graph.view.GraphView
import kotlin.reflect.KClass

/**
 * Base class with helper methods for implementing external DSL functions
 * that access a [GraphView] and its [GraphElementView]s.
 *
 * Can be used as delegate by other [DslExternalFunctions] implementations
 * that enhance the DSL with more specific functions
 */
open class GraphViewExternalFunctions : AbstractExternalFunctions(), ScriptErrorHandler {

	companion object {
		private val LOG by logger(GraphViewExternalFunctions::class)
	}

	private lateinit var graphView: GraphView
	private lateinit var origin: String
	private lateinit var context: String
	private lateinit var eventBus: EventBus

	private var _errorHandled = false

	override val errorHandled: Boolean get() = _errorHandled

	override fun defineIn(symbolTable: ScopedSymbolTable) {
		// Empty, no particular functions so far
	}

	fun bind(
		graphView: GraphView,
		origin: String,
		context: String,
		eventBus: EventBus = BaseModule.eventBus
	) {
		this.graphView = graphView
		this.origin = origin
		this.context = context
		this.eventBus = eventBus
		_errorHandled = false
	}

	// TODO Generalize and move resources to graph module
	fun getInputGraphPortView(inputName: String): GraphPortView<GraphInput<Any>>? {
		val input = graphView.getGraphPortView(inputName)
		if (input == null) {
			postNotFoundIssue(inputName)
			return null
		}
		if (!input.model.portType.isInput) {
			postTypeIssue(inputName, Translations.getString("library.element.CircuitInOut.name"), Translations.getString("graph.property.portType.output"))
			return null
		}
		return input as GraphPortView<GraphInput<Any>>
	}

	fun getComponent(id: Int): Component? {
		val component = graphView.getWithId(id)
		if (component == null) {
			postNotFoundIssue(id.toString())
		}
		return component
	}

	fun <T : Component> getComponent(id: Int, clazz: KClass<*>, translatedClassName: String): T? {
		getComponent(id)?.let { component ->
			if (component::class != clazz) {
				LOG.trace("expecting ${clazz.simpleName}, but component with ID $id is of type ${component::class.simpleName}")
				postTypeIssue(id.toString(), translatedClassName, component.type)
				return null
			}
			return component as T?
		} ?: return null
	}

	fun postTypeIssue(id: String, expected: String, actual: String) =
		postIssue(
			Translations.getString("antares.usecaseDSL.typeError.name"),
			Translations.getString("antares.usecaseDSL.typeError.text", id, expected, actual))

	private fun postNotFoundIssue(id: String) {
		LOG.trace("Component '$id' not found")
		return postIssue(
			Translations.getString("antares.usecaseDSL.compNotFound.name"),
			Translations.getString("antares.usecaseDSL.compNotFound.text", id)
		)
	}

	open fun postIssue(name: String, description: String) {
		_errorHandled = true
		eventBus.post(IssueImpl(
			severity = IssueSeverity.Error,
			name = name,
			description = description,
			origin = origin,
			context = context
		))
	}
}