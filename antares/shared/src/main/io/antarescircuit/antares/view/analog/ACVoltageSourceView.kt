package io.antarescircuit.antares.view.analog

import io.antarescircuit.antares.model.analog.ACVoltageSource
import io.antarescircuit.antares.view.port.AbstractAntaresPortView.Companion.LENGTH
import io.antarescircuit.jabbah.base.System
import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.base.geom.Direction.*
import io.antarescircuit.jabbah.base.geom.Path
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.graphics.DropShadow
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.edit.Look.SCALE
import io.antarescircuit.jabbah.edit.properties.magnitude.MagnitudeValue
import io.antarescircuit.jabbah.graph.view.vertice.AbstractVerticeView
import kotlin.math.PI
import kotlin.math.sin

class ACVoltageSourceView(
    styleProvider: StyleProvider = DrawStyleModule.styleProvider,
    model: ACVoltageSource = ACVoltageSource()
) : AbstractAnalogVerticeView<ACVoltageSource>(styleProvider, model, EAST, Rectangle2D(-SIZE / 2, LENGTH, SIZE, SIZE)) {

    companion object {
        private const val SIZE = 6 * SCALE

        private val WAVEFORM_PATH: Path by lazy {
            val path = System.createPath()
            val xl = 10
            var first = true
            var xc = 0.0
            var yc = 0.0
            for (i in -xl..xl) {
                val yy = yc + (0.95 * sin(i * PI / xl) * 8)
                if (first) {
                    path.moveTo(xc + i, yy)
                    first = false
                } else {
                    path.lineTo(xc + i, yy)
                }
            }
            path
        }
    }

    @Suppress("unused") // Reflective bean property
    var maxVoltage: MagnitudeValue
        get() = model.maxVoltage
        set(value) {
            model.maxVoltage = value
        }

    @Suppress("unused") // Reflective bean property
    var frequency: MagnitudeValue
        get() = model.frequency
        set(value) {
            model.frequency = value
        }

    override val relativeExternalLabelLocation: Point2D get() = Point2D(bounds.maxX + LABEL_DIST, bounds.centerY)

    init {
        modelExchanged(null)
    }

    /** ---- [AbstractVerticeView] */

    override fun modelExchanged(oldModel: ACVoltageSource?) {
        super.modelExchanged(oldModel)
        addPortView(AnalogPortView(styleProvider, model.getPort(1), 0, LENGTH, NORTH))
        addPortView(AnalogPortView(styleProvider, model.getPort(2), 0, LENGTH + SIZE, SOUTH))
    }

    override fun drawImpl(context: DrawContext) {
        super.drawImpl(context)

        if (shadow) {
            DropShadow.draw(context, transparency) {
                context.g.fillOval(xInt, yInt, SIZE, SIZE)
            }
        }

        context.g.stroke = stroke
        context.g.color = context.chooseBackground(transparent.applyTo(backgroundColor))
        context.g.fillOval(xInt, yInt, SIZE, SIZE)
        context.g.color = context.chooseForeground(transparent.applyTo(foregroundColor))
        context.g.drawOval(xInt, yInt, SIZE, SIZE)

        // Wave form
        context.translated(bounds.center) { it.g.draw(WAVEFORM_PATH)}
    }

    /** ---- [AbstractAnalogVerticeView] */

    override val mainPropertyValue: String get() = frequency.toString()

    override val mainPropertylabelLocation: Point2D
        get() = Point2D(bounds.minX - MAIN_PROPERTY_LABEL_DIST, bounds.centerY)

    override val mainPropertylabelOrientation: Direction get() = WEST
}