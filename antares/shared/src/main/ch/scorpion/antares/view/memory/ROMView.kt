package ch.scorpion.antares.view.memory

import ch.scorpion.antares.model.memory.ROM
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.view.DigitalComponentView
import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.MouseEvent
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.InputEventContext
import ch.scorpion.jabbah.draw.InputEventHandler
import ch.scorpion.jabbah.draw.InputEventHandlerAdapter
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.edit.EditInputEventContext
import ch.scorpion.jabbah.edit.model.text.Label
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.execution.actor.ActorView
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.graph.model.GraphElementEvent
import ch.scorpion.jabbah.graph.view.vertice.AbstractVerticeView
import ch.scorpion.jabbah.graph.view.AbstractGraphElementView
import ch.scorpion.jabbah.execution.actor.ActorInteractionHandler
import ch.scorpion.jabbah.execution.actor.ActorInteractionHandlerAdapter
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter
import ch.scorpion.jabbah.io.Reference
import ch.scorpion.jabbah.io.ReferenceResolver
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.graphics.Color


/**
 * A view of a [ROM].
 */
class ROMView(
    styleProvider: StyleProvider = DrawStyleModule.styleProvider,
    private val eventBus: EventBus = BaseModule.eventBus,
    model: ROM = ROM()
) : DigitalComponentView<ROM>(styleProvider, "library.element.ROM", model) {

    companion object {
        val WIDTH = 24 * Look.GRID
        val HEIGHT = 12 * Look.GRID
        val LABEL_VERTICAL_FACTOR = 0.3f
    }

    init {
        modelExchanged(null)
        setBounds((getPortView(model.getAddressInput()) as DigitalPortView).length, -HEIGHT / 2, WIDTH, HEIGHT)
    }

    private val inputEventHandler = DoubleClickHandler()
    private val actorInteractionHandler = DoubleClickActorHandler()

    /**
     * The text to be used for overwriting the default [ROMView] text, if any. If `null` no overwriting
     * takes place. Can also be set to an empty [String] in order to hide the predefined label.
     */
    var text: String? = null
        set(value) {
            if (value != text) {
                field = value
                label.text = if (StringUtils.isEmpty(value)) buildLabelText() else value!!
            }
        }

    private val label = Label(
            font = font,
            text = buildLabelText(),
            horizontalAlignment = Label.HorizontalAlignment.CENTER,
            verticalAlignment = Label.VerticalAlignment.CENTER,
            location = Point2D(x + width / 2, y + LABEL_VERTICAL_FACTOR * height)
    )

    override fun modelExchanged(oldModel: ROM?) {
        super.modelExchanged(oldModel)

        val addressPV = DigitalPortView(
			styleProvider = styleProvider,
			port = model!!.getAddressInput(),
			direction = Direction.WEST)
		addressPV.setLocation(addressPV.length, 0)
		addPortView(addressPV)

		val csPV = DigitalPortView(
			styleProvider = styleProvider,
			port = model!!.getChipSelectInput(),
			direction = Direction.SOUTH)
		csPV.setLocation(csPV.length + WIDTH / 2, HEIGHT / 2)
		addPortView(csPV)

		val dataPV = DigitalPortView(
			styleProvider = styleProvider,
			port = model!!.getDataOutput(),
			direction = Direction.EAST)
		dataPV.setLocation(dataPV.length+ WIDTH, 0)
		addPortView(dataPV)
    }

    /** ---- UI properties */

    var addressWidth: BitWidth
        get() = model!!.getAddressWidth()
        set(value) {
            invalidate()
            model!!.setAddressWidth(value)
            invalidate()
            validate()
        }

    var dataWidth: BitWidth
        get() = model!!.getDataWidth()
        set(value) {
            invalidate()
            model!!.setDataWidth(value)
            invalidate()
            validate()
        }

    /** ---- [Storable] interface */

    override fun write(writer: StoreWriter) {
        super.write(writer)
        if (text != null) {
            writer.writeString("text", text!!)
        }
    }

    override fun read(reader: StoreReader) {
        super.read(reader)
        val tempText = if (reader.hasAttribute("text")) reader.readString("text") else null
		// The default text depends on model data, so resolve the text after the model has been read
		reader.requestResolution(this, Reference(
			name = "text",
			additionalInfo = tempText,
			resolveAfter = listOf(reader.readInt("modelId"))))
    }

    override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {
        super.resolve(reference, referenceResolver)
        if (reference.name == "text") {
			text = reference.additionalInfo as String?
		}
    }

    /** ---- [AbstractGraphElementView] */

    override fun handleStateChanged(event: GraphElementEvent) {
        label.text = if (text == null) buildLabelText() else text!!
        super.handleStateChanged(event)
    }

    override fun drawImpl(context: DrawContext) {
        if (context.useContextColors) {
            drawImpl(context, context.color!!.foregroundColor, context.color!!.backgroundColor)
        } else {
            drawImpl(context, foregroundColor, if (filled) backgroundColor else null)
        }
    }

    private fun drawImpl(context: DrawContext, lineColor: Color, fillColor: Color?) {
        val oldColor = context.g.color
		val oldStroke = context.g.stroke

		if (fillColor != null) {
			context.g.color = fillColor
			context.g.fill(bounds)
		}

		context.g.color = lineColor
		context.g.stroke = stroke
		context.g.draw(bounds)

		label.draw(context)

		context.g.color = oldColor
		context.g.stroke = oldStroke

		super.drawImpl(context)
    }

    /** ---- [AbstractVerticeView] */

    override fun <T : InputEventContext> getInputEventHandler(context: T): InputEventHandler<T> {
        return inputEventHandler as InputEventHandler<T>
    }

    /** ---- [ActorView] */

    override fun getActorInteractionHandler(): ActorInteractionHandler? {
        return actorInteractionHandler
    }

    /** ---- [ROMView] */

    private fun buildLabelText(): String {
        return "ROM ${addressWidth.size}x${dataWidth.width}"
    }

    private fun requestOpenMemoryContents(event: MouseEvent) {
        eventBus.post(OpenMemoryContentsRequest(model!!.memory, model!!.getAddressWidth(), model!!.getDataWidth(), event))
    }

    private inner class DoubleClickHandler : InputEventHandlerAdapter<EditInputEventContext>() {
        override fun mouseClicked(context: EditInputEventContext): InputEventHandler<EditInputEventContext>? {
            if (context.mouseEvent!!.clickCount == 2) {
                requestOpenMemoryContents(context.mouseEvent!!)
                return null
            }
            return super.mouseClicked(context)
        }
    }

    private inner class DoubleClickActorHandler : ActorInteractionHandlerAdapter() {
        override fun mouseClicked(signalHandler: SignalHandler, event: MouseEvent, x: Double, y: Double) {
            if (event.clickCount == 2) {
                requestOpenMemoryContents(event)
            }
        }
    }

}