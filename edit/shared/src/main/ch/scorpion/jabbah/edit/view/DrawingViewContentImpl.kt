package ch.scorpion.jabbah.edit.view

import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.DrawableContainer
import ch.scorpion.jabbah.draw.container.DrawableContainerImpl
import ch.scorpion.jabbah.draw.container.UnzoomableContainer
import ch.scorpion.jabbah.draw.drawable.Unzoomable
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.select.UnzoomableSelectionModel

class DrawingViewContentImpl<T: Drawing<*>>(
        override val drawingView: DrawingView<T>,
        override val drawing: T,
        override val selectionManager: SelectionManager,
        highlighterFactory: HighlighterFactory
) : DrawingViewContent<T> {

    /** Holds a [DrawableContainer] for every supported [SelectionDrawingStrategy].*/
    private val selectionContainers = mutableMapOf<SelectionDrawingStrategy, DrawableContainer<SelectionModel<Component>>>()

    /** Used for managing [SelectionModel]s that are [Unzoomable]. */
    private val unzoomableSelectionContainers = mutableMapOf<SelectionDrawingStrategy, UnzoomableContainer<UnzoomableSelectionModel<Component>>>()

    init {
        selectionContainers.put(SelectionDrawingStrategy.ABOVE, DrawableContainerImpl<SelectionModel<Component>>())
        selectionContainers.put(SelectionDrawingStrategy.REPLACE, DrawableContainerImpl<SelectionModel<Component>>())
        selectionContainers.put(SelectionDrawingStrategy.BELOW, DrawableContainerImpl<SelectionModel<Component>>())
        unzoomableSelectionContainers.put(SelectionDrawingStrategy.ABOVE, UnzoomableContainer<UnzoomableSelectionModel<Component>>())
    }

    /** ---- [DrawingViewContent] interface */

    override val highlighter: Highlighter = highlighterFactory.create(this)

    override val animationContainer: DrawableContainer<Drawable> = DrawableContainerImpl()

    override val ghostContainer: UnzoomableContainer<Unzoomable> = UnzoomableContainer()

    override val highlightContainer: DrawableContainer<Drawable> = DrawableContainerImpl()

    override fun dispose() {
        drawing.dispose()
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
        selectionContainers.values.forEach {
            if (!it.getDrawables().filter { it.component === component }.isEmpty()) {
                return true
            }
        }
        unzoomableSelectionContainers.values.forEach {
            if (!it.getDrawables().filter { it.component === component }.isEmpty()) {
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
}