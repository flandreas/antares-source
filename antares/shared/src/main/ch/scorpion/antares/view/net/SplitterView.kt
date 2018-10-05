package ch.scorpion.antares.view.net

import ch.scorpion.antares.model.net.BranchCount
import ch.scorpion.antares.model.net.Splitter
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.view.DigitalComponentView
import ch.scorpion.antares.view.Handedness
import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.graph.model.Port
import ch.scorpion.jabbah.graph.view.port.PortLabelPosition
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter
import ch.scorpion.jabbah.draw.graphics.Color


/**
 * A view of a [Splitter].
 */
class SplitterView(
    styleProvider: StyleProvider = DrawStyleModule.styleProvider,
    model: Splitter = Splitter()
) : DigitalComponentView<Splitter>(styleProvider, model) {

    companion object {
        const val WIDTH = 2 * Look.GRID
        const val PORT_INSET = Look.SCALE
        const val PORT_DISTANCE = 2 * Look.SCALE
    }

    var handedness: Handedness = Handedness.RIGHT
        set(value) {
            if (field != value) {
                invalidate()
                field = value
                modelExchanged(model)
                invalidate()
                update()
            }
        }

    var bitWidth: BitWidth
        get() = model!!.bitWidth
        set(value) {
            if (value != bitWidth) {
                invalidate()
                model!!.bitWidth = value
                modelExchanged(model)
                invalidate()
                update()
            }
        }

    var branchCount: BranchCount
        get() = model!!.branchCount
        set(value) {
            if (value != branchCount) {
                invalidate()
                model!!.branchCount = value
                modelExchanged(model)
                invalidate()
                update()
            }
        }

    init {
        modelExchanged(null)
    }

    override fun modelExchanged(oldModel: Splitter?) {
        super.modelExchanged(oldModel)

		val height = 2 * PORT_INSET + PORT_DISTANCE * (model!!.outputCount - 1)

		val inputPortView = DigitalPortView(
			styleProvider = styleProvider,
			port = model!!.getInput(),
			direction = Direction.WEST,
			portLabelPosition = PortLabelPosition.EXTERNAL,
			x = 0,
            y = (height / 2))
		inputPortView.setLocation(inputPortView.length.toDouble(), 0.0)
		addPortView(inputPortView)

		setBounds(getInput().length, -height / 2, WIDTH, height)

		val dy = if (handedness == Handedness.RIGHT) -PORT_DISTANCE else +PORT_DISTANCE
		var y = if (handedness == Handedness.RIGHT) height / 2 - PORT_INSET else -height / 2 + PORT_INSET

		for (output in model!!.getOutputs()) {
			val opv = DigitalPortView(
				styleProvider = styleProvider,
				port = output as Port<DigitalSignal>,
				direction = Direction.EAST,
				portLabelPosition = PortLabelPosition.EXTERNAL)
			opv.setLocation(inputPortView.length + WIDTH, y)
			opv.showBitWidthAnnotation = false
			addPortView(opv)
			y += dy
		}
    }

    /** ---- [Storable] */

    override fun write(writer: StoreWriter) {
        super.write(writer)
        writer.writeString("handedness", handedness.customName)
    }

    override fun read(reader: StoreReader) {
        super.read(reader)
        if (reader.hasAttribute("handedness")) {
            handedness = Handedness.withName(reader.readString("handedness"))
        }
    }

    override fun drawImpl(context: DrawContext) {
        super.drawImpl(context)
        if (context.useContextColors) {
            drawImpl(context, context.color!!.foregroundColor, context.color!!.backgroundColor)
        } else {
            drawImpl(context, foregroundColor, if (filled) backgroundColor else null)
        }
    }

    private fun drawImpl(context: DrawContext, lineColor: Color, fillColor: Color?) {
        val oldColor = context.g.color
        context.g.stroke = stroke
        if (fillColor != null) {
            context.g.color = fillColor
        }
        context.g.fillRect(xInt, yInt, width.toInt(), height.toInt())
        context.g.color = lineColor
        context.g.drawRect(xInt, yInt, width.toInt(), height.toInt())

        context.g.color = oldColor
    }
}