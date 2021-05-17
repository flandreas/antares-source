package ch.scorpion.antares.script.dsl

import ch.scorpion.antares.view.inout.CircuitInOutView
import ch.scorpion.antares.view.input.SwitchView
import ch.scorpion.antares.view.output.LEDView
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.execution.issue.IssueImpl
import ch.scorpion.jabbah.execution.issue.IssueSeverity
import ch.scorpion.jabbah.graph.script.ScriptErrorHandler
import ch.scorpion.jabbah.graph.view.GraphView
import kotlin.reflect.KClass

abstract class AbstractDrawingViewBridge(
	protected val graphView: GraphView,
	private val eventBus: EventBus = BaseModule.eventBus,
	private val origin: String,
	private val context: String?
) : ScriptErrorHandler {

	companion object {
		private val LOG by logger(AbstractDrawingViewBridge::class)
	}

	/** ---- [ScriptErrorHandler] */

	private var _errorHandled = false

	override val errorHandled: Boolean get() = _errorHandled

	/** ---- [AbstractDrawingViewBridge] */

	protected fun getButton(buttonId: Int): SwitchView? {
		return getComponent(buttonId, SwitchView::class, Translations.getString("library.element.Switch.name"))
	}

	protected fun getLED(ledId: Int): LEDView? {
		return getComponent(ledId, LEDView::class, Translations.getString("library.element.LED.name"))
	}

	protected fun getInput(inputId: Int): CircuitInOutView? {
		getComponent(inputId, CircuitInOutView::class, Translations.getString("library.element.CircuitInOut.name"))?.let { input ->
			if (!input.model.portType.isInput) {
				LOG.trace("\"expecting input CircuitInOutView, but PortType is ${input.model.portType}")
				postTypeIssue(inputId, Translations.getString("library.element.CircuitInOut.name"), Translations.getString("graph.property.portType.output"))
				return null
			}
			return input
		} ?: return null
	}

	protected fun getOutput(outputId: Int): CircuitInOutView? {
		getComponent(outputId, CircuitInOutView::class, Translations.getString("library.element.CircuitInOut.name"))?.let { output ->
			if (!output.model.portType.isOutput) {
				LOG.trace("\"expecting output CircuitInOutView, but PortType is ${output.model.portType}")
				postTypeIssue(outputId, Translations.getString("library.element.CircuitInOut.name"), Translations.getString("graph.property.portType.input"))
				return null
			}
			return output
		} ?: return null
	}

	protected fun <T : Component> getComponent(id: Int, clazz: KClass<T>, translatedClassName: String): T? {
		getComponent(id)?.let { component ->
			if (component::class != clazz) {
				LOG.trace("expecting ${clazz.simpleName}, but component with ID $id is of type ${component::class.simpleName}")
				postTypeIssue(id, translatedClassName, component.type)
				return null
			}
			return component as T?
		} ?: return null
	}

	protected fun getComponent(id: Int): Component? {
		val component = graphView.getWithId(id)
		if (component == null) {
			postNotFoundIssue(id)
		}
		return component
	}

	private fun postTypeIssue(id: Int, expected: String, actual: String) {
		return postIssue(
			Translations.getString("antares.usecaseDSL.typeError.name"),
			Translations.getString("antares.usecaseDSL.typeError.text", id, expected, actual))
	}

	private fun postNotFoundIssue(id: Int) {
		LOG.trace("Component with ID $id not found")
		return postIssue(
			Translations.getString("antares.usecaseDSL.compNotFound.name"),
			Translations.getString("antares.usecaseDSL.compNotFound.text", id)
		)
	}

	protected fun postIssue(name: String, description: String) {
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