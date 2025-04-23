package ch.scorpion.jabbah.graph.view.net.netview

import ch.scorpion.jabbah.draw.DrawContext

interface NetViewElementColorProvider<T: Any> {

    fun setColor(context: DrawContext, element: AbstractNetViewElement<T>)
}