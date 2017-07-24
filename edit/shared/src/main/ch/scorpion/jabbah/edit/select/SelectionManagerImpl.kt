package ch.scorpion.jabbah.edit.select

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.PropertyChangeEvent
import ch.scorpion.jabbah.base.event.PropertyChangeListener
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.DrawableContainerEvent
import ch.scorpion.jabbah.draw.DrawableContainerListener
import ch.scorpion.jabbah.draw.container.DrawableContainerAdapter
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.logger

/**
 * Standard implementation of the [SelectionManager] interface.
 *
 * @param view the [DrawingView] whose [Component]s are selected by this [SelectionManager]
 * @param selectionModelProvider provides the [SelectionModel]s for [Component]s that are to be selected
 * @param eventBus the [EventBus] on which [SelectionChangeEvent]s are posted by this [SelectionManager]
 */
class SelectionManagerImpl(
        val view: DrawingView<out Drawing<Component>>,
        val selectionModelProvider: SelectionModelProvider,
        val eventBus: EventBus
) : SelectionManager {

    constructor(view: DrawingView<out Drawing<Component>>)
        : this(view, EditSelectModule.selectionModelProvider, BaseModule.eventBus)

    private val LOG by logger(SelectionManagerImpl::class)

    init {
        view.addPropertyChangeListener(object : PropertyChangeListener<Any> {
            override fun propertyChanged(e: PropertyChangeEvent<Any>) {
                if (e.name == DrawingView.PROP_DRAWING) {
                    handleDrawingChanged(e.oldValue as Drawing<Component>?)
                }
            }
        })
    }

    /**
     * Listens for [Component]s being removed from the current [Drawing] while being selected. Note that this
     * listener must be relinked from the current [Drawing] to the new [Drawing] if it is changed in [view].
     * This is done in [handleDrawingChanged].
     */
    private val componentRemoveListener: DrawableContainerListener<Component> = ComponentRemoveListener()

    private val selectionMap: MutableMap<Component,SelectionModel<Component>> = mutableMapOf()

    /** ---- [SelectionManager] interface */

    override val selection: Collection<Component> get() = selectionMap.keys.toList()

    override fun select(component: Component) {
        if (!isSelected(component)) {
            selectImpl(component)
            view.drawing.validate()
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
            view.drawing.validate()
        }
    }

    override fun selectAll() {
        val list = mutableListOf<Component>()
        for (c in view.drawing.getDrawables()) {
            if (!isSelected(c)) {
                selectImpl(c)
                list.add(c)
            }
        }
        if (!list.isEmpty()) {
            postSelectionChanged(list, selected = true)
            view.drawing.validate()
        }
    }

    override fun deselect(component: Component) {
        if (isSelected(component)) {
            deselectImpl(component)
            view.drawing.validate()
            postSelectionChanged(listOf(component), selected = false)
        }
    }

    override fun deselect(components: Collection<Component>) {
        val list = mutableListOf<Component>()
        for (c in view.drawing.getDrawables()) {
            if (isSelected(c)) {
                deselectImpl(c)
                list.add(c)
            }
        }
        if (!list.isEmpty()) {
            postSelectionChanged(list, selected = false)
            view.drawing.validate()
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
            view.drawing.validate()
        }
    }

    override fun isSelected(component: Component): Boolean {
        return selectionMap.containsKey(component)
    }

    /** ---- [SelectionManagerImpl] */

    /** Listens for removals of [Component]s and deselects them (if selected) in order to remove the [SelectionModel].*/
    private inner class ComponentRemoveListener : DrawableContainerAdapter<Component>() {
        override fun drawableRemoved(event: DrawableContainerEvent<Component>) {
            if (event.child is Component && isSelected(event.child as Component)) {
                deselect(event.child as Component)
            }
        }
    }

    private fun handleDrawingChanged(oldDrawing: Drawing<Component>?) {
        if (oldDrawing == null) {
            return
        }

        // Because [view.drawing] has already a new value, we cannot use [deselectAll] for cleanup,
        // because it iterates over all [Component]s of [view.drawing], which already contains other
        // (and unselected) [Component]s.

        // relink componentRemoveListener
        oldDrawing.removeDrawableContainerListener(componentRemoveListener)
        view.drawing.addDrawableContainerListener(componentRemoveListener)

        view.removeAllSelectionModels()


        selectionMap.clear()
        postSelectionChanged(emptyList<Component>(), selected = false)
    }

    private fun postSelectionChanged(components: Collection<Component>, selected: Boolean) {
        eventBus.post(SelectionChangeEvent(view, components, selected))
    }

    /** Selects the specified [Component] without posting an event.*/
    private fun selectImpl(component: Component) {
        var strategy = view.getComponentSelectionDrawingStrategy(component)
        var selectionModel = selectionModelProvider.provideFor(component, strategy)
        if (selectionModel == null && component.preferredSelectionDrawingStrategy != null) {
            strategy = component.preferredSelectionDrawingStrategy!!
            selectionModel = selectionModelProvider.provideFor(component, strategy)
        }
        if (selectionModel == null) {
            LOG.error("SelectionManagerImpl: No suitable SelectionModel found for ${System.SYSTEM!!.getClassName(component.selectableComponent)}")
            return
        }
        view.addSelectionModel(selectionModel, strategy)
        selectionMap.put(component, selectionModel)
        selectionModel.notifyAdded(view)
    }

    /** Deselects the specified [Component] without posting an event.*/
    private fun deselectImpl(component: Component) {
        val selectionModel = selectionMap[component]
        if (selectionModel != null) {
            selectionMap.remove(component)
            view.removeSelectionModel(selectionModel)
            selectionModelProvider.release(selectionModel)
            selectionModel.notifyRemoved(view)
        }
    }
}