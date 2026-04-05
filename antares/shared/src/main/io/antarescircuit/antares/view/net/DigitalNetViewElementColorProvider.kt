package io.antarescircuit.antares.view.net

import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.view.style.AntaresTheme
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.graphics.Color
import io.antarescircuit.jabbah.draw.graphics.CompositeColor
import io.antarescircuit.jabbah.draw.style.Themes
import io.antarescircuit.jabbah.graph.GraphApplicationContext
import io.antarescircuit.jabbah.graph.view.net.netview.AbstractNetViewElement
import io.antarescircuit.jabbah.graph.view.net.netview.NetViewElementColorProvider

object DigitalNetViewElementColorProvider : NetViewElementColorProvider<DigitalSignal> {

    /** Used to avoid creation of [CompositeColor] objects in drawing operations. */
    private val areaColorMap = mutableMapOf<Color, CompositeColor>()

    override fun setColor(context: DrawContext, element: AbstractNetViewElement<DigitalSignal>) {
        val graphAppContext = context.castedAppContext<GraphApplicationContext>()!!

        context.color = if (graphAppContext.showNetState) {
            val signalColor = if (element.model.isError) {
                Themes.get<AntaresTheme>().error
            } else {
                element.model.signal!!.color
            }

            if (element.styling.isArea) {
                areaColorMap.getOrPut(signalColor.foregroundColor) {
                    CompositeColor(signalColor.foregroundColor, Themes.get<AntaresTheme>().word.backgroundColor)
                }
            } else {
                signalColor
            }
        } else {
            context.choose(element.color)
        }
    }
}