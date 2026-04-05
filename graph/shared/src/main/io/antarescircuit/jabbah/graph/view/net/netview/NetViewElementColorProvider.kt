package io.antarescircuit.jabbah.graph.view.net.netview

import io.antarescircuit.jabbah.draw.DrawContext

interface NetViewElementColorProvider<T: Any> {

    fun setColor(context: DrawContext, element: AbstractNetViewElement<T>)
}