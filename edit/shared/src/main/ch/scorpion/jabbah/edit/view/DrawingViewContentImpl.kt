package ch.scorpion.jabbah.edit.view

import ch.scorpion.jabbah.draw.*
import ch.scorpion.jabbah.draw.container.DrawableContainerAdapter
import ch.scorpion.jabbah.draw.container.DrawableContainerImpl
import ch.scorpion.jabbah.draw.container.UnzoomableContainer
import ch.scorpion.jabbah.draw.drawable.Unzoomable
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.select.UnzoomableSelectionModel

class DrawingViewContentImpl<T : Drawing<Component>>(
	override val drawingView: DrawingView<T>,
	override val drawing: T,
	selectionManagerFactory: SelectionManagerFactory,
	highlighterFactory: HighlighterFactory
) : DrawingViewContent<T> {

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

	override var zoomPan: ZoomPan = drawingView.zoomPan

	override val selectionManager: SelectionManager = selectionManagerFactory.create(this)

	override val highlighter: Highlighter = highlighterFactory.create(this)

	override val animationContainer: DrawableContainer<Drawable> = DrawableContainerImpl()

	override val ghostContainer: UnzoomableContainer<Unzoomable> = UnzoomableContainer()

	override val highlightContainer: DrawableContainer<Drawable> = DrawableContainerImpl()

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

	override fun zoomableSelectionContainerFor(strategy: SelectionDrawingStrategy): DrawableContainer<SelectionModel<Component>>? {
		return selectionContainers[strategy]
	}

	override fun unzoomableSelectionContainerFor(strategy: SelectionDrawingStrategy): UnzoomableContainer<UnzoomableSelectionModel<Component>>? {
		return unzoomableSelectionContainers[strategy]
	}

	/** Listens for removals of [Component]s and deselects them (if selected) in order to remove the [SelectionModel].*/
	private inner class ComponentRemoveListener : DrawableContainerAdapter<Component>() {
		override fun drawableRemoved(event: DrawableContainerEvent<Component>) {
			if (event.child is Component && selectionManager.isSelected(event.child as Component)) {
				// Due to Kotlin bug KT-15558, the gradle compiler issues warning "No cast needed"
				selectionManager.deselect(event.child as Component)
			}
		}
	}
}