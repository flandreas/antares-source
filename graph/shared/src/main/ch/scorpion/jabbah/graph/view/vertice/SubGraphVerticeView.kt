package ch.scorpion.jabbah.graph.view.vertice

import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.edit.model.text.LabelComponent
import ch.scorpion.jabbah.edit.model.text.Translatable
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.container.ContainerDrawing
import ch.scorpion.jabbah.graph.library.Library
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVertice
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.VerticeView

/**
 * A graphical representation of a [SubGraphVertice] that can be added to a [GraphView].
 */
interface SubGraphVerticeView<T : SubGraphVertice> : VerticeView<T> {

	companion object {

		fun getDescribingName(label: Translatable?, graph: Graph): String {
			val graphName = graph.name.value
			return if (label == null) {
				graphName
			} else {
				if (label.getTranslation() != graphName) {
					"${label.getTranslation()}: $graphName"
				} else {
					graphName
				}
			}
		}
	}

	/** Returns the model of this [SubGraphVerticeView].*/
	val subGraphVertice: SubGraphVertice?

	val hasCustomizedContainerDrawing: Boolean

	/**
	 * The text to be used to overwrite the first [LabelComponent], if any. If `null` no overwriting
	 * takes place. Can also be set to an empty [String] in order to hide the predefined label.
	 * Returns the standard value from the [ContainerDrawing] if not overwritten. Used by the UI.
	 */
	var label: Translatable?

	var executionLabel: Translatable?

	var controlViewVisibility: ControlViewVisibility

	/**
	 * Returns the translated name of the referenced [Graph], preceded by the [label] if it differs
	 * from the [Graph]'s name.
	 */
	val describingName: String

	/**
	 * Creates a new [GraphView] of the references sub [Graph].
	 * @param signalHandler the [SignalHandler] required if the [GraphView] is currently being executed
	 */
	fun createSubGraphView(signalHandler: SignalHandler?): GraphView

	/** Adds a [Drawable] to be part of the graphical representation of this [SubGraphVerticeView] */
	fun addDrawable(drawable: Drawable)

	/**
	 * Returns a clone of the [ContainerDrawing] that can be customized by the user, which is either the one of the reference
	 * [Library] [MetaGraph] (if the user hasn't customized it yet), or the previously customized
	 * [ContainerDrawing].
	 */
	fun getEditableContainerDrawing(): ContainerDrawing

	/**
	 * Sets or resets the [ContainerDrawing] that has been customized by the user.
	 * @param containerDrawing the customized [ContainerDrawing], or `null` if the customization is to be
	 * deleted and the [SubGraphVerticeView] should use the standard [ContainerDrawing] from the [Library].
	 */
	fun setEditedContainerDrawing(containerDrawing: ContainerDrawing?)

	/**
	 * Returns the designating [LabelComponent] of this [SubGraphVerticeView]'s [ContainerDrawing].
	 * This is the one (if any) the user can overwrite with a custom text that is more
	 * appropriate (or more precise) in the [GraphView] where this [SubGraphVerticeView] is used.
	 */
	fun getLabelComponent(): LabelComponent?
}