package io.antarescircuit.jabbah.edit

import io.antarescircuit.jabbah.base.event.PropertyChangeEvent
import io.antarescircuit.jabbah.draw.*
import io.antarescircuit.jabbah.draw.style.StyleType
import io.antarescircuit.jabbah.draw.container.UnzoomableContainer
import io.antarescircuit.jabbah.draw.container.UnzoomableContainerIF
import io.antarescircuit.jabbah.draw.drawable.DrawableDrawer
import io.antarescircuit.jabbah.draw.drawable.Unzoomable
import io.antarescircuit.jabbah.edit.module.EditModule
import io.antarescircuit.jabbah.edit.select.UnzoomableSelectionModel

interface DrawingViewFactory {
	fun <C : Component, T : Drawing<C>> create(
		drawing: T,
		contextHolder: ApplicationContextHolder?,
		displayGlobalMessages: Boolean,
		name: String
	): DrawingView<C,T>
}

/**
 * Enhances [View] with functionality needed for editing [Drawing]s.
 *
 * Clients should not instantiate implementations of [DrawingView] by themselves.
 * They should rather use [EditModule.drawingViewFactory] for creating [DrawingView] implementations.
 */
interface DrawingView<C: Component, T : Drawing<C>> : View<EditInputEventContext> {

	companion object {
		/** The name of the [Drawing] property in [PropertyChangeEvent]s.*/
		const val PROP_DRAWING = "PROP_DRAWING"

		/** The name of the [Boolean] property in [PropertyChangeEvent]s.*/
		const val PROP_SHOW_GRID = "PROP_SHOW_GRID"

		/** The name of the [Boolean] property in [PropertyChangeEvent]s.*/
		const val PROP_EDITABLE = "PROP_EDITABLE"
	}

	/**
	 * Determines whether this [DrawingView] is editable.
	 * Sends a [PropertyChangeEvent] for [DrawingView.PROP_EDITABLE] when changed.
	 */
	var editable: Boolean

	/**
	 * Contains the [DrawingViewContent] that is currently being displayed by this [DrawingView].
	 * Set a new [DrawingViewContent] if the [Drawing] and its current selection and highlights should be restored,
	 * otherwise use [setDrawing] to set a new [Drawing] and to create a new [DrawingViewContent].
	 * Sends a [PropertyChangeEvent] for [DrawingView.PROP_DRAWING]
	 */
	var content: DrawingViewContent<C, T>

	/**
	 * Holds the [Drawing] as the main [ComponentContainer] in this [DrawingView].
	 */
	val drawing: T


	/** Allows selecting [Component]s in this [DrawingView] for editing them.*/
	val selectionManager: SelectionManager

	/** Allows temporary highlighting of [Component]s in this [DrawingView].*/
	val highlighter: Highlighter

	/** Controls whether the user wants this [DrawingView] to display its [Grid]. Even if this property is set,
	 * the [Grid] is not shown if this [DrawingView] is not [editable].
	 */
	var showGrid: Boolean

	/** The [Grid] displayed in the background of this [DrawingView].*/
	val grid: Grid

	/** Holds the default [SelectionDrawingStrategy] to be used when selecting [Component].*/
	var defaultSelectionDrawingStrategy: SelectionDrawingStrategy

	/** Holds the [DrawableContainer] for temporarily displaying [Unzoomable] graphical objects like ghosts.*/
	val ghostContainer: UnzoomableContainerIF<Unzoomable>

	/** Holds the [DrawableContainer] for rendering zoomed animations.*/
	val animationContainer: DrawableContainer<Drawable>

	/** Holds the [DrawableContainer] that contains the [Drawable]s that highlight [Component]s.*/
	val highlightContainer: DrawableContainer<Drawable>

	/**
	 * Draws non-[Component] zoomed [Drawables][Drawable] beneath everything else, e.g. A4 page borders to signal
	 * available space for [drawing].
	 */
	val backgroundContainer: DrawableContainer<Drawable>

	/**
	 * Sets the current [Drawing] by creating a new [DrawingViewContent] for the new [Drawing] and sending
	 * a [PropertyChangeEvent] for [DrawingView.PROP_DRAWING]
	 * @param drawing the new [Drawing] to set
	 * @param applyDefaultZoomStrategy `true` if [View.defaultZoomStrategy] is to be applied. Specify `false`
	 * if the [Drawing] is reset for technical reasons, e.g. if the [Drawing] comes from an undo/redo snapshot.
	 */
	fun setDrawing(drawing: T, applyDefaultZoomStrategy: Boolean = true)

	/** Creates a new [DrawingViewContent] for the specified [Drawing]*/
	fun createContent(drawing: T): DrawingViewContent<C, T>

	/**
	 * Adds the specified [DrawableDrawer] at the head of the chain of [DrawableDrawer] responsible for drawing
	 * the main [Drawing].
	 */
	fun addDrawableDrawer(drawableDrawer: DrawableDrawer<C>)

	/**
	 * Determines the [SelectionDrawingStrategy] of a [Component] in this [DrawingView].
	 *
	 * If the [Component] has a preferred [SelectionDrawingStrategy], that one should be returned.
	 * Otherwise, the default [SelectionDrawingStrategy] of this [DrawingView] will be returned.
	 */
	fun getComponentSelectionDrawingStrategy(component: Component): SelectionDrawingStrategy
}

interface DrawingViewContent<C: Component, T : Drawing<C>> {

	/** The [DrawingView] that owns this [DrawingViewContent].*/
	val drawingView: DrawingView<C, T>

	/** Holds the [Drawing] as the main [ComponentContainer] of this [DrawingViewContent].*/
	val drawing: T

	/** Stores the [ViewTransformation] of a [DrawingView] in order to re-establish it when going back to this [DrawingViewContent].*/
	var transformation: ViewTransformation

	/** Allows selecting [Component]s in [drawing] for editing them.*/
	val selectionManager: SelectionManager

	/** Allows temporary highlighting of [Component]s in [drawing].*/
	val highlighter: Highlighter

	/** Holds the [DrawableContainer] for temporarily displaying [Unzoomable] graphical objects like ghosts.*/
	val ghostContainer: UnzoomableContainer<Unzoomable>

	/** Holds the [DrawableContainer] for rendering zoomed animations.*/
	val animationContainer: DrawableContainer<Drawable>

	/** Holds the [DrawableContainer] that contains the [Drawable]s that highlight [Component]s.*/
	val highlightContainer: DrawableContainer<Drawable>

	/** Draws all [Components][Component] in [drawing] with [StyleType.isBackdrop] is `true`.*/
	val backdropDrawer: Drawable

	/**
	 * Draws non-[Component] zoomed [Drawables][Drawable] beneath everything else, e.g. A4 page borders to signal
	 * available space for [drawing].
	 */
	val backgroundContainer: DrawableContainer<Drawable>

	/** Frees this [DrawingViewContent] from usage and disposes all inner objects, including the [Drawing].*/
	fun dispose()

	/** Adds the specified [SelectionModel] to the [DrawableContainer] related with the given [SelectionDrawingStrategy]. */
	fun addSelectionModel(selectionModel: SelectionModel<Component>, strategy: SelectionDrawingStrategy)

	/** Removes the specified [SelectionModel] from this [DrawingView].*/
	fun removeSelectionModel(selectionModel: SelectionModel<Component>)

	/** Removes all [SelectionModel]s from this [DrawingViewContent].*/
	fun removeAllSelectionModels()

	/** Determines whether this [DrawingView] shows a [SelectionModel] for the specified [Component].*/
	fun hasSelectionModelFor(component: Component): Boolean

	fun getReplacingSelectionModel(component: Component): SelectionModel<Component>?

	/** Returns the [DrawableContainer] that contains the zoomable [SelectionModel]s of the specified [SelectionDrawingStrategy].*/
	fun zoomableSelectionContainerFor(strategy: SelectionDrawingStrategy): DrawableContainer<SelectionModel<Component>>?

	/** Returns the [UnzoomableContainer] that contains the [UnzoomableSelectionModel] for the specified [SelectionDrawingStrategy].*/
	fun unzoomableSelectionContainerFor(strategy: SelectionDrawingStrategy): UnzoomableContainer<UnzoomableSelectionModel<Component>>?
}