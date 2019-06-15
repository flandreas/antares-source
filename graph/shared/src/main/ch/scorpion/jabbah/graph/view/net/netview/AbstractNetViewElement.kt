package ch.scorpion.jabbah.graph.view.net.netview

import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.execution.module.ExecutionModule
import ch.scorpion.jabbah.execution.speed.CurrentSystemSpeedCategory
import ch.scorpion.jabbah.execution.speed.SystemSpeedCategory
import ch.scorpion.jabbah.graph.model.GraphElementEvent
import ch.scorpion.jabbah.graph.model.Net
import ch.scorpion.jabbah.graph.view.AbstractGraphElementView
import ch.scorpion.jabbah.graph.view.NetView
import ch.scorpion.jabbah.graph.view.NetViewElement
import ch.scorpion.jabbah.graph.view.style.GraphStyleType

/**
 * Abstract base implementation of the [NetViewElement] interface.
 * @param T the type of signal
 */
abstract class AbstractNetViewElement<T: Any>(
    styleProvider: StyleProvider,
    protected val currentSystemSpeedCategory: CurrentSystemSpeedCategory = ExecutionModule.currentSystemSpeedCategory,
    model: Net<T>
) : AbstractGraphElementView<Net<T>>(styleProvider, GraphStyleType.EDGE, model), NetViewElement<T> {

    override var net: Net<T>?
	    get() = model
	    set(value) {
		    if (model !== value) {
			    model = value
		    }
	    }

    override var netView: NetView<T>? = null
        set(value) {
            if (value == field) {
                return
            }
            if (field != null) {
                field!!.remove(this)
            }
            field = value
        }

    override fun handleStateChanged(event: GraphElementEvent) {
        if (showNetState()) {
            super.handleStateChanged(event)
        }
    }

    protected fun showNetState(): Boolean {
        return currentSystemSpeedCategory.systemSpeedCategory > SystemSpeedCategory.Use
    }
}