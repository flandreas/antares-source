package ch.scorpion.antares.view.analog

import ch.scorpion.antares.model.analog.Capacitor
import ch.scorpion.antares.view.port.AbstractAntaresPortView
import ch.scorpion.antares.view.port.AbstractAntaresPortView.Companion.LENGTH
import ch.scorpion.jabbah.base.Thousands
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.view.style.GraphStyleType
import ch.scorpion.jabbah.graph.view.vertice.AbstractVerticeView

class CapacitorView(
    styleProvider: StyleProvider = DrawStyleModule.styleProvider,
    model: Capacitor = Capacitor()
) : AbstractAnalogVerticeView<Capacitor>(styleProvider, model, Direction.NORTH, Rectangle2D(LENGTH, -SIZE / 2, SIZE, SIZE)
) {

    companion object {
        private val SIZE = wInt(4)
        private val BAR_WIDTH = w(0.4)
        private val BAR_HEIGHT = h(4)
    }

    @Suppress("unused") // Reflective bean property
    var capacitance: Double
        get() = model.capacitance
        set(value) {
            model.capacitance = value
        }

    override val relativeExternalLabelLocation: Point2D get() = Point2D(bounds.centerX, bounds.minY - LABEL_DIST)

    /** ---- [AbstractVerticeView] */

    override fun modelExchanged(oldModel: Capacitor?) {
        super.modelExchanged(oldModel)
        addPortView(AnalogPortView(styleProvider, model.getPort(1), LENGTH, 0, Direction.WEST))
        addPortView(AnalogPortView(styleProvider, model.getPort(2), LENGTH + SIZE, 0, Direction.EAST))
    }

    override fun drawImpl(context: DrawContext) {
        super.drawImpl(context)

        with (context.g) {
            stroke = styleProvider.getStyle(GraphStyleType.EDGE).stroke

            // Left side (port 1)
            // Don't draw shadow, doesn't look good
            (getPortView(model.getPort(1)) as AbstractAntaresPortView<*>).prepareConnectionDrawContext(context)
            drawLine(x, y + SIZE / 2, x + w(1.25), y + SIZE / 2)
            if (!context.castedAppContext<GraphApplicationContext>()!!.isExecute) {
                context.g.color = context.chooseForeground(this@CapacitorView.foregroundColor)
            }
            fillRect(x + w(1.25), y + SIZE / 2 - BAR_HEIGHT / 2, BAR_WIDTH, BAR_HEIGHT)

            // Right side (port 2)
            // Don't draw shadow, doesn't look good
            (getPortView(model.getPort(2)) as AbstractAntaresPortView<*>).prepareConnectionDrawContext(context)
            drawLine(x + SIZE, y + SIZE / 2, x + SIZE - w(1.25), y + SIZE / 2)
            if (!context.castedAppContext<GraphApplicationContext>()!!.isExecute) {
                context.g.color = context.chooseForeground(this@CapacitorView.foregroundColor)
            }
            fillRect(x + SIZE - w(1.25) - BAR_WIDTH, y + SIZE / 2 - BAR_HEIGHT / 2, BAR_WIDTH, BAR_HEIGHT)
        }
    }

    override val mainPropertyValue: String get() = "${Thousands.convert(model.capacitance / 1_000_000.0, " ")}F"
}