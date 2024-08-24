package ch.scorpion.antares.view.analog

import ch.scorpion.antares.model.analog.Capacitor
import ch.scorpion.antares.view.port.AbstractAntaresPortView
import ch.scorpion.jabbah.base.Thousands
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.graph.view.style.GraphStyleType
import ch.scorpion.jabbah.graph.view.vertice.AbstractVerticeView

class CapacitorView(
    styleProvider: StyleProvider = DrawStyleModule.styleProvider,
    model: Capacitor = Capacitor()
) : AbstractAnalogVerticeView<Capacitor>(styleProvider, model) {

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

    /** ---- [AbstractVerticeView] */

    override fun modelExchanged(oldModel: Capacitor?) {
        super.modelExchanged(oldModel)
        addPortView(AnalogPortView(styleProvider, model.getPort(1), AbstractAntaresPortView.LENGTH, 0, Direction.WEST))
        addPortView(AnalogPortView(styleProvider, model.getPort(2), AbstractAntaresPortView.LENGTH + SIZE, 0, Direction.EAST))
        setBounds(AbstractAntaresPortView.LENGTH, -SIZE / 2, SIZE, SIZE)
        updateLabel()
    }

    override fun drawImpl(context: DrawContext) {
        super.drawImpl(context)

        with (context.g) {
            stroke = styleProvider.getStyle(GraphStyleType.EDGE).stroke

            // Left side (port 1)
            (getPortView(model.getPort(1)) as AbstractAntaresPortView<*>).prepareConnectionDrawContext(context)
            drawLine(x, y + SIZE / 2, x + w(1.25), y + SIZE / 2)
            fillRect(x + w(1.25), y + SIZE / 2 - BAR_HEIGHT / 2, BAR_WIDTH, BAR_HEIGHT)

            // Right side (port 2)
            (getPortView(model.getPort(2)) as AbstractAntaresPortView<*>).prepareConnectionDrawContext(context)
            drawLine(x + SIZE, y + SIZE / 2, x + SIZE - w(1.25), y + SIZE / 2)
            fillRect(x + SIZE - w(1.25) - BAR_WIDTH, y + SIZE / 2 - BAR_HEIGHT / 2, BAR_WIDTH, BAR_HEIGHT)
        }
    }

    override val mainPropertyValue: String get() = "${Thousands.convert(model.capacitance / 1_000_000.0, " ")}F"
}