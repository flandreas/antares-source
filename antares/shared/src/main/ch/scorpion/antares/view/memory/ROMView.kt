package ch.scorpion.antares.view.memory

import ch.scorpion.antares.model.memory.ROM
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.view.DigitalComponentView
import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.jabbah.base.Math
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.MouseEvent
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rotation
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.InputEventContext
import ch.scorpion.jabbah.draw.InputEventHandler
import ch.scorpion.jabbah.draw.InputEventHandlerAdapter
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.model.text.HorizontalAlignment
import ch.scorpion.jabbah.edit.model.text.Label
import ch.scorpion.jabbah.edit.model.text.TextProperty
import ch.scorpion.jabbah.edit.model.text.VerticalAlignment
import ch.scorpion.jabbah.execution.actor.ActorInteractionContext
import ch.scorpion.jabbah.execution.actor.ActorInteractionHandler
import ch.scorpion.jabbah.execution.actor.ActorView
import ch.scorpion.jabbah.execution.actor.ClickableActorInteractionHandlerAdapter
import ch.scorpion.jabbah.graph.model.GraphElementEvent
import ch.scorpion.jabbah.graph.view.AbstractGraphElementView
import ch.scorpion.jabbah.graph.view.vertice.AbstractVerticeView
import ch.scorpion.jabbah.io.*


/**
 * A view of a [ROM].
 */
class ROMView(
    styleProvider: StyleProvider = DrawStyleModule.styleProvider,
    private val eventBus: EventBus = BaseModule.eventBus,
    model: ROM = ROM()
) : DigitalComponentView<ROM>(styleProvider, "library.element.ROM", model) {

    companion object {

        /** The width of a ROMView box if the contents are not displayed.*/
        const val MIN_WIDTH = 24 * Look.GRID

        /** The height of a ROMView box if the contents are not displayed.*/
        const val MIN_HEIGHT = 12 * Look.GRID

        /** The horizontal inset between the outer box and the contents box.*/
        const val HORIZONTAL_CONTENTS_INSET = 20

        /** The vertical inset between the outer box and the contents box.*/
        const val VERTICAL_CONTENTS_INSET = 40

        const val LABEL_INSET = 20
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
            horizontalAlignment = HorizontalAlignment.CENTER,
            verticalAlignment = VerticalAlignment.CENTER
    )

    private var contentsView = AddressableContentsView(model)

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
		csPV.setLocation(csPV.length + MIN_WIDTH / 2, MIN_HEIGHT / 2)
		addPortView(csPV)

		val dataPV = DigitalPortView(
			styleProvider = styleProvider,
			port = model!!.getDataOutput(),
			direction = Direction.EAST)
		dataPV.setLocation(dataPV.length+ MIN_WIDTH, 0)
		addPortView(dataPV)

        if (model != null) {
	        label.text = buildLabelText()
            contentsView = AddressableContentsView(
                    addressable = model!!,
                    rowsCount = contentRowsCount,
                    columnsCount = contentColumnsCount,
                    showDisassembler = showDisassembler)
        }
    }

    init {
        modelExchanged(null)
        updateGeometry()
    }

    /** ---- UI properties */

    var addressWidth: BitWidth
        get() = model!!.addressWidth
        set(value) {
            invalidate()
            model!!.setAddressWidth(value)
            invalidate()
            validate()
        }

    var dataWidth: BitWidth
        get() = model!!.dataWidth
        set(value) {
            invalidate()
            model!!.setDataWidth(value)
            invalidate()
            validate()
        }

    var showContents: Boolean = false
        set(value) {
            if (field != value) {
                field = value
                updateGeometry()
                validate()
            }
        }

    var contentRowsCount: Int
        get() = contentsView.rowsCount
        set(value) {
            if (value != contentRowsCount) {
                contentsView.rowsCount = value
                updateGeometry()
                validate()
            }
        }

    var contentColumnsCount: Int
        get() = contentsView.columnsCount
        set(value) {
            if (value != contentColumnsCount) {
                contentsView.columnsCount = value
                updateGeometry()
                validate()
            }
        }

    var disassemblerConfig: TextProperty
        get() = TextProperty(model!!.disassemblerConfig)
        set(value) { model!!.disassemblerConfig = value.text!! }

    var showDisassembler: Boolean
        get() = contentsView.showDisassembler
        set(value) {
            if (value != showDisassembler) {
                contentsView.showDisassembler = value
                updateGeometry()
                validate()
            }
        }

    /** ---- [Storable] interface */

    override fun write(writer: StoreWriter) {
        super.write(writer)
        if (showContents) {
            writer.writeBoolean("showContents", showContents)
        }
        if (text != null) {
            writer.writeString("text", text!!)
        }
        writer.writeInt("contentRowsCount", contentRowsCount)
        writer.writeInt("contentColumnsCount", contentColumnsCount)
        writer.writeBoolean("showDisassembler", showDisassembler)
    }

    override fun read(reader: StoreReader) {
        super.read(reader)
        if (reader.hasAttribute("showContents")) {
            reader.requestResolution(this, Reference(
                    name = "showContents",
                    additionalInfo = reader.readBoolean("showContents"),
                    resolveAfter = listOf(reader.readInt("modelId"))))
        }
        val tempText = if (reader.hasAttribute("text")) reader.readString("text") else null
		// The default text depends on model data, so resolve the text after the model has been read
		reader.requestResolution(this, Reference(
			name = "text",
			additionalInfo = tempText,
			resolveAfter = listOf(reader.readInt("modelId"))))
        if (reader.hasAttribute("contentRowsCount")) {
            contentRowsCount = reader.readInt("contentRowsCount")
        }
        if (reader.hasAttribute("contentColumnsCount")) {
            contentColumnsCount = reader.readInt("contentColumnsCount")
        }
        if (reader.hasAttribute("showDisassembler")) {
            showDisassembler = reader.readBoolean("showDisassembler")
        }
    }

    override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {
        super.resolve(reference, referenceResolver)
        if (reference.name == "text") {
			text = reference.additionalInfo as String?
		} else if (reference.name == "showContents") {
            showContents = reference.additionalInfo as Boolean
        }
    }

    /** ---- [Component] */

    override fun rotationChanged(newRotation: Rotation) {
        super.rotationChanged(newRotation)
        updateGeometry()
    }

    /** ---- [AbstractGraphElementView] */

    override fun handleStateChanged(event: GraphElementEvent) {
        label.text = if (text == null) buildLabelText() else text!!
        contentsView.handleCurrentAddressChanged()
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

        if (showContents) {
            context.g.translate(contentsView.x, contentsView.y)
            context.g.rotate(rotation.inverse().angle)
            context.g.translate(-contentsView.x, -contentsView.y)
            contentsView.draw(context)
            context.g.translate(contentsView.x, contentsView.y)
            context.g.rotate(-rotation.inverse().angle)
            context.g.translate(-contentsView.x, -contentsView.y)
        }

		context.g.color = oldColor
		context.g.stroke = oldStroke

		super.drawImpl(context)
    }

    /** ---- [AbstractVerticeView] */

    override fun <T : InputEventContext> getInputEventHandler(context: T): InputEventHandler<T> {
        return inputEventHandler
    }

    /** ---- [ActorView] */

    override fun getActorInteractionHandler(): ActorInteractionHandler? {
        return actorInteractionHandler
    }

    /** ---- [ROMView] */

    private fun updateGeometry() {
        invalidate()
        contentsView.updateGeometry()

        val addressPV = getPortView(model!!.getAddressInput())!!
        val x = addressPV.unconnectedLength

        val totalHeight = Look.scaleToDoubleGrid(calculateHeight())
        val totalWidth = Look.scaleToDoubleGrid(calculateWidth())
        setBounds(x, -totalHeight / 2, totalWidth, totalHeight)

        if (showContents) {
            contentsView.location = calculateContentsLocation()
        }
        label.location = Point2D(x + width / 2.0, y + LABEL_INSET)

        addressPV.setLocation(addressPV.unconnectedLength, 0)

        val dataPV = getPortView(model!!.getDataOutput())!!
        dataPV.setLocation(dataPV.unconnectedLength + width, 0.0)

        getPortView(model!!.getChipSelectInput())!!.setLocation(x + width / 2, height / 2)

        invalidate()

        update()
    }

    private fun calculateContentsLocation(): Point2D {
        return when(rotation) {
            Rotation.R0 -> Point2D(x + width / 2 - contentsView.width / 2, -contentsView.height / 2)
            Rotation.R90 -> Point2D(x + width / 2 + contentsView.height / 2, y + height / 2 - contentsView.width / 2)
            Rotation.R180 -> Point2D(x + width / 2 + contentsView.width / 2, contentsView.height / 2)
            Rotation.R270 -> Point2D(x + width / 2 - contentsView.height / 2, contentsView.width / 2)
        }
    }

    private fun calculateWidth(): Int {
        return if (showContents) {
            Math.max(MIN_WIDTH, when (rotation) {
                Rotation.R0, Rotation.R180 -> (contentsView.width + 2 * HORIZONTAL_CONTENTS_INSET).toInt()
                Rotation.R90, Rotation.R270 -> (contentsView.height + 2 * HORIZONTAL_CONTENTS_INSET).toInt()
            })
        } else {
            MIN_WIDTH
        }
    }

    private fun calculateHeight(): Int {
        return if (showContents) {
            Math.max(MIN_HEIGHT, when (rotation) {
                Rotation.R0, Rotation.R180 -> (contentsView.height + 2 * VERTICAL_CONTENTS_INSET).toInt()
                Rotation.R90, Rotation.R270 -> (contentsView.width + 2 * VERTICAL_CONTENTS_INSET).toInt()
            })
        } else {
            MIN_HEIGHT
        }
    }

    private fun buildLabelText(): String {
        return "ROM ${addressWidth.size}x${dataWidth.width}"
    }

    private fun requestOpenMemoryContents(event: MouseEvent, readonly: Boolean) {
        eventBus.post(OpenMemoryContentsRequest(label.text, model!!.memory, model!!, event, readonly))
    }

    private inner class DoubleClickHandler : InputEventHandlerAdapter<InputEventContext>() {
        override fun mouseClicked(context: InputEventContext): InputEventHandler<InputEventContext>? {
            if (context.mouseEvent!!.clickCount == 2) {
                requestOpenMemoryContents(context.mouseEvent!!, context.readonly)
                return null
            }
            return super.mouseClicked(context)
        }
    }

    private inner class DoubleClickActorHandler : ClickableActorInteractionHandlerAdapter() {
        override fun mouseClicked(context: ActorInteractionContext) {
            if (context.mouseEvent!!.clickCount == 2) {
                requestOpenMemoryContents(context.mouseEvent!!, true)
            }
        }
    }
}