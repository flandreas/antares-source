package io.antarescircuit.jabbah.graph.view.net.netview

import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.graph.model.Net
import io.antarescircuit.jabbah.graph.view.AbstractGraphElementView
import io.antarescircuit.jabbah.graph.view.NetView
import io.antarescircuit.jabbah.graph.view.NetViewElement
import io.antarescircuit.jabbah.graph.view.style.GraphStyleType

/**
 * Abstract base implementation of the [NetViewElement] interface.
 * @param T the type of signal
 */
abstract class AbstractNetViewElement<T: Any>(
    styleProvider: StyleProvider,
    model: Net<T>
) : AbstractGraphElementView<Net<T>>(styleProvider, GraphStyleType.EDGE, model), NetViewElement<T> {

    abstract val styling: NetViewStyling

    override var net: Net<T>?
	    get() = model
	    set(value) {
		    if (model !== value) {
			    model = value!!
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
}