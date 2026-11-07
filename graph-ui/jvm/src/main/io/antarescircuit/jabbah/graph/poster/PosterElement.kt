package io.antarescircuit.jabbah.graph.poster

import io.antarescircuit.jabbah.base.geom.Margin
import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.graphics.Stroke
import io.antarescircuit.jabbah.draw.graphics.FontImpl
import io.antarescircuit.jabbah.edit.model.rectangle.AbstractRectangularComponent
import io.antarescircuit.jabbah.edit.model.text.Label
import io.antarescircuit.jabbah.edit.model.text.VerticalAlignment
import io.antarescircuit.jabbah.graph.MetaGraph

class PosterElement(
    val metaGraph: MetaGraph,
    var zoom: Double = 1.0,
    private val drawBorder: Boolean,
    val margin: Margin
) : AbstractRectangularComponent(shape = Rectangle2D()) {

    companion object {
        private val MARGIN_STROKE = Stroke(0.1f)
        private val BORDER_STROKE = Stroke(0.3f)
        private val FONT = FontImpl(size = 3)
    }

    override val type: String get() = metaGraph.name

    private val label = Label(metaGraph.name, FONT, verticalAlignment = VerticalAlignment.TOP)

    init {
        filled = false
    }

    override fun update() {
        super.update()
        label.location = topCenter
    }

    override fun draw(context: DrawContext) {
        // This would draw the border, which was already made smaller by the PosterPacker
        if (drawBorder) {
            drawStroke(context, shapeToDraw, foregroundColor, BORDER_STROKE)
        }

        val bbox = metaGraph.graph.graphView.boundingBox
        val cX = minX + margin.left + (width - margin.horizontalSum) / 2.0
        val cY = minY + margin.top + (height - margin.verticalSum) / 2.0

        context.g.translate(cX, cY)

        context.g.scale(zoom, zoom)
        context.g.translate(bbox.center.negate)

        context.modelClip = null
        metaGraph.graph.graphView.draw(context)

        context.g.translate(bbox.center)
        context.g.scale(1.0 / zoom, 1.0 / zoom)

        context.g.translate(-cX, -cY)

        context.g.color = style.color.foregroundColor
        label.draw(context)

        // BEGIN Debug: Draw margin
        /*
        context.g.color = Color.RED
        context.g.stroke = MARGIN_STROKE
        context.g.drawRect(x + margin.left, y + margin.top, width - margin.horizontalSum, height - margin.verticalSum)
        */
        // END Debug
    }
}