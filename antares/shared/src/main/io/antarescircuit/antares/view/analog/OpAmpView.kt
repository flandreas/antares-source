package io.antarescircuit.antares.view.analog

import io.antarescircuit.antares.model.analog.OpAmp
import io.antarescircuit.antares.view.Handedness
import io.antarescircuit.antares.view.Handedness.LEFT
import io.antarescircuit.antares.view.Handedness.RIGHT
import io.antarescircuit.antares.view.port.AbstractAntaresPortView.Companion.LENGTH
import io.antarescircuit.jabbah.base.System
import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.base.geom.Direction.NORTH
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.graphics.DropShadow
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.edit.Look.SCALE
import io.antarescircuit.jabbah.edit.properties.magnitude.MagnitudeValue
import io.antarescircuit.jabbah.graph.view.vertice.AbstractVerticeView
import io.antarescircuit.jabbah.graph.view.LabeledRectangularVerticeView
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter
import kotlin.jvm.JvmStatic

class OpAmpView(
    styleProvider: StyleProvider = DrawStyleModule.styleProvider,
    model: OpAmp = OpAmp()
) : AbstractAnalogVerticeView<OpAmp>(styleProvider, model, NORTH, Rectangle2D(-LENGTH - WIDTH, -HEIGHT / 2, WIDTH, HEIGHT)) {

    companion object {
        @JvmStatic private val WIDTH = wInt(10)
        @JvmStatic private val HEIGHT = hInt(12)

        private val SHAPE = System.createPath()
            .moveTo(-LENGTH, 0)
            .lineTo(-LENGTH - WIDTH, -HEIGHT / 2)
            .lineTo(-LENGTH - WIDTH, HEIGHT / 2)
            .close()
    }

    /** Determines where the non-inverting (+) input lies relative to signal flow direction. */
    var handedness: Handedness = RIGHT
        set(value) {
            if (value != field) {
                field = value
                updateLayout()
            }
        }

    @Suppress("unused") // Reflective bean property
    var maxOutput: MagnitudeValue
        get() = model.maxOutput
        set(value) {
            model.maxOutput = value
        }

    @Suppress("unused") // Reflective bean property
    var minOutput: MagnitudeValue
        get() = model.minOutput
        set(value) {
            model.minOutput = value
        }

    init {
        modelExchanged(null)
    }

    /** ---- [AbstractVerticeView] */

    override fun modelExchanged(oldModel: OpAmp?) {
        super.modelExchanged(oldModel)
        addPortView(AnalogPortView(styleProvider, model.invertingPort, -LENGTH - WIDTH, -3 * SCALE, Direction.WEST))
        addPortView(AnalogPortView(styleProvider, model.nonInvertingPort, -LENGTH - WIDTH, 3 * SCALE, Direction.WEST))
        addPortView(AnalogPortView(styleProvider, model.getPort(3), -LENGTH, 0, Direction.EAST))
        updateMainPropertyLabel()
        updateLayout()
    }

    override fun drawImpl(context: DrawContext) {
        val oldStylable = context.stylable

        context.stylable = this
        drawImplBeforeBorder(context)
        context.stylable = oldStylable

        drawBody(context)

        context.stylable = this
        drawImplAfterBorder(context)
        context.stylable = oldStylable
    }

    private fun drawBody(context: DrawContext) {
        if (shadow) {
            DropShadow.draw(context, transparency) {
                context.g.fill(SHAPE)
            }
        }

        context.g.stroke = stroke
        context.g.color = context.chooseBackground(transparent.applyTo(backgroundColor))
        context.g.fill(SHAPE)
        context.g.color = context.chooseForeground(transparent.applyTo(foregroundColor))
        context.g.draw(SHAPE)
    }

    /** ---- [LabeledRectangularVerticeView] */

    override val relativeExternalLabelLocation: Point2D get() =
        Point2D(bounds.centerX, bounds.minY - LABEL_DIST)

    override fun read(reader: StoreReader) {
        super.read(reader)
        if (reader.hasAttribute("handedness")) {
            handedness = Handedness.withName(reader.readString("handedness"))
        }
    }

    override fun write(writer: StoreWriter) {
        super.write(writer)
        writer.writeString("handedness", handedness.customName)
    }

    /** ---- [OpAmpView] */

    private fun updateLayout() {
        invalidate()

        val invertingPortView = getPortView(model.invertingPort)!!
        val nonInvertingPortView = getPortView(model.nonInvertingPort)!!

        when (handedness) {
            RIGHT -> {
                invertingPortView.setLocation(-LENGTH - WIDTH, -3 * SCALE)
                nonInvertingPortView.setLocation(-LENGTH - WIDTH, 3 * SCALE)
            }
            LEFT -> {
                invertingPortView.setLocation(-LENGTH - WIDTH, 3 * SCALE)
                nonInvertingPortView.setLocation(-LENGTH - WIDTH, -3 * SCALE)
            }
        }

        validate()
    }
}