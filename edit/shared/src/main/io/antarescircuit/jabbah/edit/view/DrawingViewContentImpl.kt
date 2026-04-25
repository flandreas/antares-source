package io.antarescircuit.jabbah.edit.view

import io.antarescircuit.jabbah.base.geom.RectangularShape
import io.antarescircuit.jabbah.draw.*
import io.antarescircuit.jabbah.draw.container.DrawableContainerAdapter
import io.antarescircuit.jabbah.draw.container.DrawableContainerImpl
import io.antarescircuit.jabbah.draw.container.UnzoomableContainer
import io.antarescircuit.jabbah.draw.drawable.AbstractDrawable
import io.antarescircuit.jabbah.draw.drawable.Unzoomable
import io.antarescircuit.jabbah.draw.style.StyleType
import io.antarescircuit.jabbah.edit.*
import io.antarescircuit.jabbah.edit.select.UnzoomableSelectionModel

class DrawingViewContentImpl<C : Component, T : Drawing<C>>(
	override val drawingView: DrawingView<C,T>,
	override val drawing: T,
	selectionManagerFactory: SelectionManagerFactory,
	highlighterFactory: HighlighterFactory
) : DrawingViewContent<C,T> {

	/** Holds a [DrawableContainer] for every supported [SelectionDrawingStrategy].*/
	private val selectionContainers =
		mutableMapOf<SelectionDrawingStrategy, DrawableContainer<SelectionModel<Component>>>()

	/** Used for managing [SelectionModel]s that are [Unzoomable]. */
	private val unzoomableSelectionContainers =
		mutableMapOf<SelectionDrawingStrategy, UnzoomableContainer<UnzoomableSelectionModel<Component>>>()

	/** Listens for [Component]s being removed from the current [Drawing] while being selected.*/
	private val componentRemoveListener = ComponentRemoveListener()

	init {
		selectionContainers[SelectionDrawingStrategy.ABOVE] = DrawableContainerImpl()
		selectionContainers[SelectionDrawingStrategy.REPLACE] = DrawableContainerImpl()
		selectionContainers[SelectionDrawingStrategy.BELOW] = DrawableContainerImpl()
		unzoomableSelectionContainers[SelectionDrawingStrategy.ABOVE] = UnzoomableContainer()
		drawing.addDrawableContainerListener(componentRemoveListener)
	}

	/** ---- [DrawingViewContent] interface */

	// Create a copy of the current ViewTransformation so new DrawingViewContents created due to
	// establishing an undo snapshot doesn't change the transformation
	override var transformation: ViewTransformation = drawingView.transformation.copy()

	override val selectionManager: SelectionManager = selectionManagerFactory.invoke(this)

	override val highlighter: Highlighter = highlighterFactory.create(this)

	override val animationContainer: DrawableContainer<Drawable> = DrawableContainerImpl()

	override val ghostContainer: UnzoomableContainer<Unzoomable> = UnzoomableContainer()

	override val highlightContainer: DrawableContainer<Drawable> = DrawableContainerImpl()

	override val backdropDrawer: Drawable = BackdropDrawer()

	/**
	 * Initially invisible to not interfere with ZoomStrategy bounding box calculation if the background is empty.
	 * A visible, empty background would add point 0/0 to the bounding box (bug #1130).
	 */
	override val backgroundContainer: DrawableContainer<Drawable> = DrawableContainerImpl(visible = false)

	override fun dispose() {
		drawing.removeDrawableContainerListener(componentRemoveListener)
		drawing.dispose()
		selectionManager.dispose()
	}

	override fun addSelectionModel(selectionModel: SelectionModel<Component>, strategy: SelectionDrawingStrategy) {
		if (selectionModel is Unzoomable) {
			unzoomableSelectionContainers[strategy]?.add(selectionModel as UnzoomableSelectionModel<Component>)
				?: throw IllegalArgumentException("no suitable selection container found")
		} else {
			selectionContainers[strategy]?.add(selectionModel)
				?: throw IllegalArgumentException("no suitable selection container found")
		}
	}

	override fun removeSelectionModel(selectionModel: SelectionModel<Component>) {
		selectionContainers.values.forEach { it.remove(selectionModel) }
		if (selectionModel is UnzoomableSelectionModel) {
			unzoomableSelectionContainers.values.forEach { it.remove(selectionModel) }
		}
	}

	override fun removeAllSelectionModels() {
		selectionContainers.values.forEach { it.clear() }
	}

	override fun hasSelectionModelFor(component: Component): Boolean {
		selectionContainers.values.forEach { container ->
			if (!container.drawables.none { it.component === component }) {
				return true
			}
		}
		unzoomableSelectionContainers.values.forEach { container ->
			if (!container.drawables.none { it.component === component }) {
				return true
			}
		}
		return false
	}

	override fun getReplacingSelectionModel(component: Component): SelectionModel<Component>? =
		selectionContainers[SelectionDrawingStrategy.REPLACE]!!.drawables.firstOrNull { it.component === component.selectableComponent }

	override fun zoomableSelectionContainerFor(strategy: SelectionDrawingStrategy): DrawableContainer<SelectionModel<Component>>? {
		return selectionContainers[strategy]
	}

	override fun unzoomableSelectionContainerFor(strategy: SelectionDrawingStrategy): UnzoomableContainer<UnzoomableSelectionModel<Component>>? {
		return unzoomableSelectionContainers[strategy]
	}

	/** Listens for removals of [Component]s and deselects them (if selected) in order to remove the [SelectionModel].*/
	private inner class ComponentRemoveListener : DrawableContainerAdapter<C>() {
		override fun drawableRemoved(event: DrawableContainerEvent<C>) {
			if (event.child is Component && selectionManager.isSelected(event.child as Component)) {
				// Due to Kotlin bug KT-15558, the Gradle compiler issues warning "No cast needed"
				selectionManager.deselect(event.child as Component)
			}
		}
	}

	/**
	 * A virtual layer that draws all [Components][Component] of [drawing] whose [StyleType.isBackdrop]
	 * is `true`.
	 */
	private inner class BackdropDrawer : AbstractDrawable() {

		override val boundingBox: RectangularShape get() = drawing.boundingBox

		override fun draw(context: DrawContext) {
			drawing
				.getDrawables { it.styleType.isBackdrop }
				.forEach { it.draw(context) }
		}

		override fun contains(x: Double, y: Double): Boolean = boundingBox.contains(x, y)
	}
}