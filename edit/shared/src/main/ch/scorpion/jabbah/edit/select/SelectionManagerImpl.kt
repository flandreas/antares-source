package ch.scorpion.jabbah.edit.select

import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.*

/**
 * Standard implementation of the [SelectionManager] interface.
 *
 * @param content the [DrawingViewContent] whose [Component]s are selected by this [SelectionManager]
 * @param selectionModelProvider provides the [SelectionModel]s for [Component]s that are to be selected
 * @param eventBus the [EventBus] on which [SelectionChangeEvent]s are posted by this [SelectionManager]
 */
class SelectionManagerImpl(
	val content: DrawingViewContent<*>,
	private val selectionModelProvider: SelectionModelProvider = EditSelectModule.selectionModelProvider,
	private val eventBus: EventBus = BaseModule.eventBus
) : SelectionManager {

	companion object {
        private val LOG by logger(SelectionManagerImpl::class)
	}

    private val selectionMap: MutableMap<Component,SelectionModel<Component>> = mutableMapOf()

    /** ---- [SelectionManager] interface */

    override val selection: Collection<Component> get() = selectionMap.keys.toList()

	override fun dispose() {
		// empty
	}

	override fun select(component: Component) {
        if (!isSelected(component)) {
            selectImpl(component)
            content.drawing.validate()
            postSelectionChanged(listOf(component), selected = true)
        }
    }

    override fun select(components: Collection<Component>) {
        val newSelections = mutableListOf<Component>()
        for (c in components) {
            if (!isSelected(c)) {
                selectImpl(c)
                newSelections.add(c)
            }
        }
        if (!newSelections.isEmpty()) {
            postSelectionChanged(newSelections, selected = true)
	        content.drawing.validate()
        }
    }

    override fun selectAll() {
        val list = mutableListOf<Component>()
        for (c in content.drawing.getDrawables()) {
            if (!isSelected(c)) {
                selectImpl(c)
                list.add(c)
            }
        }
        if (!list.isEmpty()) {
            postSelectionChanged(list, selected = true)
	        content.drawing.validate()
        }
    }

    override fun deselect(component: Component) {
        if (isSelected(component)) {
            deselectImpl(component)
	        content.drawing.validate()
            postSelectionChanged(listOf(component), selected = false)
        }
    }

    override fun deselect(components: Collection<Component>) {
        val list = mutableListOf<Component>()
        for (c in content.drawing.getDrawables()) {
            if (isSelected(c)) {
                deselectImpl(c)
                list.add(c)
            }
        }
        if (!list.isEmpty()) {
            postSelectionChanged(list, selected = false)
	        content.drawing.validate()
        }
    }

    override fun deselectAll() {
        val list = mutableListOf<Component>()
        for (c in selectionMap.keys.toList()) {
            deselectImpl(c)
            list.add(c)
        }
        if (!list.isEmpty()) {
            postSelectionChanged(list, selected = false)
	        content.drawing.validate()
        }
    }

    override fun isSelected(component: Component): Boolean {
        return selectionMap.containsKey(component)
    }

    /** ---- [SelectionManagerImpl] */

    private fun postSelectionChanged(components: Collection<Component>, selected: Boolean) {
        eventBus.post(SelectionChangeEvent(content.drawingView, components, selected))
    }

    /** Selects the specified [Component] without posting an event.*/
    private fun selectImpl(component: Component) {
        var strategy = content.drawingView.getComponentSelectionDrawingStrategy(component)
        var selectionModel = selectionModelProvider.provideFor(component, strategy)
        if (selectionModel == null && component.preferredSelectionDrawingStrategy != null) {
            strategy = component.preferredSelectionDrawingStrategy!!
            selectionModel = selectionModelProvider.provideFor(component, strategy)
        }
        if (selectionModel == null) {
            LOG.error("SelectionManagerImpl: No suitable SelectionModel found for " +
                "${System.SYSTEM!!.getClassName(component.selectableComponent)} and strategy $strategy")
            return
        }
        content.addSelectionModel(selectionModel, strategy)
	    selectionMap[component] = selectionModel
        selectionModel.notifyAdded(content.drawingView)
    }

    /** Deselects the specified [Component] without posting an event.*/
    private fun deselectImpl(component: Component) {
        val selectionModel = selectionMap[component]
        if (selectionModel != null) {
            selectionMap.remove(component)
            content.removeSelectionModel(selectionModel)
            selectionModelProvider.release(selectionModel)
            selectionModel.notifyRemoved(content.drawingView)
        }
    }
}