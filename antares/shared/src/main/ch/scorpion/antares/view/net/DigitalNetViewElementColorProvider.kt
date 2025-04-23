package ch.scorpion.antares.view.net

import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.view.net.netview.AbstractNetViewElement
import ch.scorpion.jabbah.graph.view.net.netview.NetViewElementColorProvider

object DigitalNetViewElementColorProvider : NetViewElementColorProvider<DigitalSignal> {

    /** Used to avoid creation of [CompositeColor] objects in drawing operations. */
    private val areaColorMap = mutableMapOf<Color, CompositeColor>()

    override fun setColor(context: DrawContext, element: AbstractNetViewElement<DigitalSignal>) {
        val graphAppContext = context.castedAppContext<GraphApplicationContext>()!!

        context.color = if (graphAppContext.showNetState) {
            if (element.model.isError) {
                Themes.get<AntaresTheme>().error
            } else {
                val signalColor = element.model.signal!!.color
                if (element.styling.isArea) {
                    areaColorMap.getOrPut(signalColor.foregroundColor) {
                        CompositeColor(signalColor.foregroundColor, Themes.get<AntaresTheme>().word.backgroundColor)
                    }
                } else {
                    signalColor
                }
            }
        } else {
            context.choose(element.color)
        }
    }
}