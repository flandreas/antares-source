package ch.scorpion.jabbah.edit

import ch.scorpion.jabbah.base.event.PropertyChangeEvent
import ch.scorpion.jabbah.draw.*
import ch.scorpion.jabbah.draw.drawable.DrawableDrawer
import ch.scorpion.jabbah.draw.drawable.Unzoomable
import ch.scorpion.jabbah.draw.container.UnzoomableContainer
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.edit.select.UnzoomableSelectionModel

typealias DrawingViewFactory<T> = (T) -> DrawingView<T>

/**
 * Enhances [View] with functionality needed for editing [Drawing]s.
 *
 * Clients should not instantiate implementations of [DrawingView] by themselves.
 * They should rather use [EditModule.drawingViewFactory] for creating [DrawingView] implementations.
 */
interface DrawingView<T : Drawing<*>> : View<EditInputEventContext> {

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
	 * otherwise use the property [drawing] to set a new [Drawing] and to create a new [DrawingViewContent].
	 * Sends a [PropertyChangeEvent] for [DrawingView.PROP_DRAWING]
	 */
	var content: DrawingViewContent<T>

	/**
	 * Holds the [Drawing] as the main [ComponentContainer] in this [DrawingView].
	 * When changed, creates a new [DrawingViewContent] for the new [Drawing] and sends
	 * a [PropertyChangeEvent] for [DrawingView.PROP_DRAWING]
	 */
	var drawing: T

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
	val ghostContainer: UnzoomableContainer<Unzoomable>

	/** Holds the [DrawableContainer] for rendering zoomed animations.*/
	val animationContainer: DrawableContainer<Drawable>

	/** Holds the [DrawableContainer] that contains the [Drawable]s that highlight [Component]s.*/
	val highlightContainer: DrawableContainer<Drawable>

	/** Returns the [Component] that has previously been set using [setDropComponent], if */
	val dropComponent: Component?

	/** Creates a new [DrawingViewContent] for the specified [Drawing]*/
	fun createContent(drawing: T): DrawingViewContent<T>

	/**
	 * Adds the specified [DrawableDrawer] at the head of the chain of [DrawableDrawer] responsible for drawing
	 * the main [Drawing].
	 */
	fun addDrawableDrawer(drawableDrawer: DrawableDrawer<Component>)

	/**
	 * Determines the [SelectionDrawingStrategy] of a [Component] in this [DrawingView].
	 *
	 * If the [Component] has a preferred [SelectionDrawingStrategy], that one should be returned.
	 * Otherwise, the default [SelectionDrawingStrategy] of this [DrawingView] will be returned.
	 */
	fun getComponentSelectionDrawingStrategy(component: Component): SelectionDrawingStrategy

	/**
	 * Sets the [Component] to be dragged into this [DrawingView]. This method supports "snapped" drag and drop.
	 * This [DrawingView] adds the specified [Component] to the animation container, if not already present,
	 * and updates its location according to the specified [Point2D].
	 * Removes the previously set [Component] if `null` is specified.
	 */
	fun setDropComponent(component: Component?, location: Point2D?)
}

interface DrawingViewContent<T : Drawing<*>> {

	/** The [DrawingView] that owns this [DrawingViewContent].*/
	val drawingView: DrawingView<T>

	/** Holds the [Drawing] as the main [ComponentContainer] of this [DrawingViewContent].*/
	val drawing: T

	/** Stores the [ZoomPan] of a [DrawingView] in order to re-establish it when going back to this [DrawingViewContent].*/
	var zoomPan: ZoomPan

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

	/** Returns the [DrawableContainer] that contains the zoomable [SelectionModel]s of the specified [SelectionDrawingStrategy].*/
	fun zoomableSelectionContainerFor(strategy: SelectionDrawingStrategy): DrawableContainer<SelectionModel<Component>>?

	/** Returns the [UnzoomableContainer] that contains the [UnzoomableSelectionModel] for the specified [SelectionDrawingStrategy].*/
	fun unzoomableSelectionContainerFor(strategy: SelectionDrawingStrategy): UnzoomableContainer<UnzoomableSelectionModel<Component>>?
}