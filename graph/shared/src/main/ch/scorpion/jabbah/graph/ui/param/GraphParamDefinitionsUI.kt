package ch.scorpion.jabbah.graph.ui.param

import ch.scorpion.jabbah.base.AbstractAction
import ch.scorpion.jabbah.base.Action
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.ui.AbstractUIController
import ch.scorpion.jabbah.base.ui.UIView
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.param.GraphParamDefinition
import ch.scorpion.jabbah.graph.model.param.GraphParamDefinitions

interface GraphParamDefinitionsView : UIView {
	fun valueChanged()
	fun startAdding(name: String)
	fun <T: Any> getEditedDefinition(): GraphParamDefinition<T>
	fun errorMessage(msg: String?)
	fun close()
}

/**
 * Controls editing the [GraphParamDefinitions] of a [Graph].
 */
class GraphParamDefinitionsController(
	val graph: Graph
) : AbstractUIController<GraphParamDefinitionsView>() {

	var value: GraphParamDefinitions = graph.parameterDefinitions
		set(value) {
			field = value
			view.valueChanged()
		}

	var selectedDefinition: GraphParamDefinition<*>? = null
		set(value) {
			field = value
			isFormDirty = false
			updateActions()
			view.errorMessage(null)
		}

	val cancelAction: Action = CancelAction()
	val saveAction: Action = SaveAction()
	val addAction: Action = AddAction()
	val removeAction: Action = RemoveAction()
	val applyAction: Action = ApplyAction()

	private var isFormDirty = false
	private var isAdding = false

	val isFormEnabled: Boolean get() = isAdding || selectedDefinition != null

	var definitionsToReturn: GraphParamDefinitions? = null
		private set

	init {
		updateActions()
	}

	/** Called by [GraphParamDefinitionsView] when the user has changed data in the form.*/
	fun formChanged() {
		isFormDirty = true
		updateActions()
	}

	private fun updateActions() {
		addAction.enabled = true
		removeAction.enabled = selectedDefinition != null
		applyAction.enabled = isFormDirty
	}

	private fun validate(def: GraphParamDefinition<*>): Boolean {
		return if (value.contains(def.name)) {
			view.errorMessage(Translations.getString("graph.paramDefs.dialog.error.unique"))
			false
		} else if (graph.getGraphPort<Any>(def.name) != null) {
			view.errorMessage(Translations.getString("graph.paramDefs.dialog.error.usedForPort"))
			false
		} else {
			view.errorMessage(null)
			true
		}
	}

	private inner class CancelAction : AbstractAction("graph.paramDefs.dialog.cancel") {
		override fun execute(event: ActionEvent) {
			definitionsToReturn = null
			view.close()
		}
	}

	private inner class SaveAction : AbstractAction("graph.paramDefs.dialog.save") {
		override fun execute(event: ActionEvent) {
			definitionsToReturn = value
			view.close()
		}
	}

	private inner class AddAction : AbstractAction("graph.paramDefs.dialog.add") {
		override fun execute(event: ActionEvent) {
			isAdding = true
			view.startAdding("<New>")
		}
	}

	private inner class RemoveAction : AbstractAction("graph.paramDefs.dialog.remove") {
		override fun execute(event: ActionEvent) {
			isAdding = false
			value = value.withoutDefinition(selectedDefinition!!.name)
		}
	}

	private inner class ApplyAction : AbstractAction("graph.paramDefs.dialog.apply") {
		override fun execute(event: ActionEvent) {
			val editedDef = view.getEditedDefinition<Any>()
			if (validate(editedDef)) {
				isAdding = false
				value = if (selectedDefinition != null) {
					value.withReplacedDefinition(selectedDefinition!!.name, editedDef)
				} else {
					value.withDefinition(view.getEditedDefinition<Any>())
				}
			}
		}
	}
}