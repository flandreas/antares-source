package ch.scorpion.antares.view.addressable

import ch.scorpion.antares.model.addressable.RAM
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.view.DigitalComponentView
import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rotation
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.InputEventContext
import ch.scorpion.jabbah.draw.InputEventHandler
import ch.scorpion.jabbah.draw.InputEventHandlerAdapter
import ch.scorpion.jabbah.draw.graphics.DropShadow
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.model.text.HorizontalAlignment
import ch.scorpion.jabbah.edit.model.text.Label
import ch.scorpion.jabbah.edit.model.text.VerticalAlignment
import ch.scorpion.jabbah.execution.actor.ActorInteractionContext
import ch.scorpion.jabbah.execution.actor.ActorInteractionHandler
import ch.scorpion.jabbah.execution.actor.ActorView
import ch.scorpion.jabbah.execution.actor.ClickableActorInteractionHandlerAdapter
import ch.scorpion.jabbah.execution.module.ExecutionModule
import ch.scorpion.jabbah.execution.speed.CurrentSystemSpeedCategory
import ch.scorpion.jabbah.execution.speed.SystemSpeedCategory
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.model.GraphElementEvent
import ch.scorpion.jabbah.graph.view.AbstractGraphElementView
import ch.scorpion.jabbah.graph.view.vertice.AbstractVerticeView
import ch.scorpion.jabbah.io.*
import kotlin.math.max


/**
 * A view of a [RAM].
 * TODO Extract code common with [ROMView] to common base class.
 */
class RAMView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	private val eventBus: EventBus = BaseModule.eventBus,
	model: RAM = RAM()
) : DigitalComponentView<RAM>(styleProvider, model) {

	companion object {
		const val WIDTH = 24 * Look.GRID
		const val HEIGHT = 12 * Look.GRID
		const val LABEL_VERTICAL_FACTOR = 0.3f
		const val CLOCK_PORT_X_FACTOR = 6
		const val CS_PORT_X_FACTOR = 10
		const val WRITE_PORT_X_FACTOR = 14
		const val CLEAR_PORT_X_FACTOR = 18
	}

	private val inputEventHandler = DoubleClickHandler()
	private val actorInteractionHandler = DoubleClickActorHandler()

	/**
	 * The text to be used for overwriting the default [RAMView] text, if any. If `null` no overwriting
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
		rotationDisplayStrategy = Label.RotationDisplayStrategy.ROTATE_HALF,
		horizontalAlignment = HorizontalAlignment.CENTER,
		verticalAlignment = VerticalAlignment.CENTER,
		location = Point2D(x + width / 2, y + LABEL_VERTICAL_FACTOR * height))

	private var contentsView = AddressableContentsView(model)

	override fun modelExchanged(oldModel: RAM?) {
		super.modelExchanged(oldModel)

		val addressPV = DigitalPortView(
			styleProvider = styleProvider,
			port = model.getAddressInput(),
			direction = Direction.WEST)
		addressPV.setLocation(addressPV.length, 0)
		addPortView(addressPV)

		val dataPV = DigitalPortView(
			styleProvider = styleProvider,
			port = model.getDataPort(),
			direction = Direction.EAST)
		dataPV.setLocation(dataPV.length + WIDTH, 0)
		addPortView(dataPV)

		if (model.hasClock) {
			val clockPV = DigitalPortView(
				styleProvider = styleProvider,
				port = model.getClockInput()!!,
				direction = Direction.SOUTH)
			clockPV.setLocation(clockPV.length + CLOCK_PORT_X_FACTOR * Look.GRID, HEIGHT / 2)
			addPortView(clockPV)
		}

		val csPV = DigitalPortView(
			styleProvider = styleProvider,
			port = model.getChipSelectInput(),
			direction = Direction.SOUTH)
		csPV.setLocation(csPV.length + CS_PORT_X_FACTOR * Look.GRID, HEIGHT / 2)
		addPortView(csPV)

		val writePV = DigitalPortView(
			styleProvider = styleProvider,
			port = model.getWriteInput(),
			direction = Direction.SOUTH)
		writePV.setLocation(writePV.length + WRITE_PORT_X_FACTOR * Look.GRID, HEIGHT / 2)
		addPortView(writePV)

		val clearPV = DigitalPortView(
			styleProvider = styleProvider,
			port = model.getClearInput(),
			direction = Direction.SOUTH)
		clearPV.setLocation(clearPV.length + CLEAR_PORT_X_FACTOR * Look.GRID, HEIGHT / 2)
		addPortView(clearPV)

		label.text = buildLabelText()
		contentsView = AddressableContentsView(
			addressable = model,
			rowsCount = contentRowsCount,
			columnsCount = contentColumnsCount,
			showDisassembler = false)
	}

	init {
		modelExchanged(null)
		updateGeometry()
	}

	/** ---- UI properties */

	var addressWidth: BitWidth
		get() = model.addressWidth
		set(value) {
			invalidate()
			model.setAddressWidth(value)
			invalidate()
			validate()
		}

	var dataWidth: BitWidth
		get() = model.dataWidth
		set(value) {
			invalidate()
			model.setDataWidth(value)
			invalidate()
			validate()
		}

	@Suppress("unused")
	var hasClock: Boolean
		get() = model.hasClock
		set(value) {
			invalidate()
			model.hasClock = value
			modelExchanged(model)
			updateGeometry()
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
		label.ownerRotation = rotation
		updateGeometry()
	}

	/** ---- [AbstractGraphElementView] */

	override fun handleStateChanged(event: GraphElementEvent) {
		label.text = if (text == null) buildLabelText() else text!!
		if (model.isSelected) {
			contentsView.handleCurrentAddressChanged()
		}
		super.handleStateChanged(event)
	}

	override fun drawImpl(context: DrawContext) {
		val oldColor = context.g.color
		val oldStroke = context.g.stroke

		if (shadow) {
			DropShadow.draw(context, transparency) {
				context.g.fill(bounds)
			}
		}

		context.g.color = context.choose(color).backgroundColor
		context.g.fill(bounds)

		context.g.color = context.choose(color).foregroundColor
		context.g.stroke = stroke
		context.g.draw(bounds)

		label.draw(context)

		if (requireDrawContents(context)) {
			context.stylable = this
			context.g.translate(contentsView.x, contentsView.y)
			context.g.rotate(rotation.inverse().angle)
			context.g.translate(-contentsView.x, -contentsView.y)
			contentsView.draw(context)
			context.g.translate(contentsView.x, contentsView.y)
			context.g.rotate(-rotation.inverse().angle)
			context.g.translate(-contentsView.x, -contentsView.y)
			context.stylable = null
		}

		context.g.color = oldColor
		context.g.stroke = oldStroke

		super.drawImpl(context)
	}

	/** Determines whether drawing hte [AddressableContentsView] is required depending on the [CurrentSystemSpeedCategory].*/
	private fun requireDrawContents(context: DrawContext): Boolean {
		return showContents && (
			!context.castedAppContext<GraphApplicationContext>()!!.isExecute
				|| context.castedAppContext<GraphApplicationContext>()!!.isPausing
				|| ExecutionModule.currentSystemSpeedCategory.systemSpeedCategory >= SystemSpeedCategory.Observe)
	}

	/** ---- [AbstractVerticeView] */

	override fun <T : InputEventContext> getInputEventHandler(context: T): InputEventHandler<T> {
		return inputEventHandler
	}

	/** ---- [ActorView] */

	override fun getActorInteractionHandler(context: ActorInteractionContext): ActorInteractionHandler {
		return actorInteractionHandler
	}

	/** ---- [ROMView] */

	private fun updateGeometry() {
		invalidate()
		contentsView.updateGeometry()

		val addressPV = getPortView(model.getAddressInput())!!
		val x = addressPV.unconnectedLength

		val totalHeight = Look.scaleToDoubleGrid(calculateHeight())
		val totalWidth = Look.scaleToDoubleGrid(calculateWidth())
		setBounds(x, -totalHeight / 2, totalWidth, totalHeight)

		if (showContents) {
			contentsView.location = calculateContentsLocation()
		}
		label.location = Point2D(x + width / 2.0, y + ROMView.LABEL_INSET)

		addressPV.setLocation(addressPV.unconnectedLength, 0)

		val dataPV = getPortView(model.getDataPort())!!
		dataPV.setLocation(dataPV.unconnectedLength + width, 0.0)

		if (model.hasClock) {
			getPortView(model.getClockInput()!!)!!.setLocation(x + CLOCK_PORT_X_FACTOR * Look.GRID.toDouble(), height / 2)
		}
		getPortView(model.getChipSelectInput())!!.setLocation(x + CS_PORT_X_FACTOR * Look.GRID.toDouble(), height / 2)
		getPortView(model.getWriteInput())!!.setLocation(x + WRITE_PORT_X_FACTOR * Look.GRID.toDouble(), height / 2)
		getPortView(model.getClearInput())!!.setLocation(x + CLEAR_PORT_X_FACTOR * Look.GRID.toDouble(), height / 2)

		invalidate()

		update()
	}

	private fun calculateContentsLocation(): Point2D {
		return when (rotation) {
			Rotation.R0 -> Point2D(x + width / 2 - contentsView.width / 2, -contentsView.height / 2)
			Rotation.R90 -> Point2D(x + width / 2 + contentsView.height / 2, y + height / 2 - contentsView.width / 2)
			Rotation.R180 -> Point2D(x + width / 2 + contentsView.width / 2, contentsView.height / 2)
			Rotation.R270 -> Point2D(x + width / 2 - contentsView.height / 2, contentsView.width / 2)
		}
	}

	private fun calculateWidth(): Int {
		return if (showContents) {
			max(ROMView.MIN_WIDTH, when (rotation) {
				Rotation.R0, Rotation.R180 -> (contentsView.width + 2 * ROMView.HORIZONTAL_CONTENTS_INSET).toInt()
				Rotation.R90, Rotation.R270 -> (contentsView.height + 2 * ROMView.HORIZONTAL_CONTENTS_INSET).toInt()
			})
		} else {
			ROMView.MIN_WIDTH
		}
	}

	private fun calculateHeight(): Int {
		return if (showContents) {
			max(ROMView.MIN_HEIGHT, when (rotation) {
				Rotation.R0, Rotation.R180 -> (contentsView.height + 2 * ROMView.VERTICAL_CONTENTS_INSET).toInt()
				Rotation.R90, Rotation.R270 -> (contentsView.width + 2 * ROMView.VERTICAL_CONTENTS_INSET).toInt()
			})
		} else {
			ROMView.MIN_HEIGHT
		}
	}

	private fun buildLabelText(): String {
		return "RAM ${addressWidth.size}x${dataWidth.width}"
	}

	private fun requestOpenMemoryContents(readonly: Boolean, newDesktopView: Boolean) {
		eventBus.post(OpenMemoryContentsRequest(this, label.text, model, readonly, newDesktopView))
	}

	private inner class DoubleClickHandler : InputEventHandlerAdapter<InputEventContext>() {
		override fun mouseClicked(context: InputEventContext): InputEventHandler<InputEventContext>? {
			if (context.mouseEvent!!.clickCount == 2) {
				requestOpenMemoryContents(true, context.mouseEvent!!.isAltDown)
				return null
			}
			return super.mouseClicked(context)
		}
	}

	private inner class DoubleClickActorHandler : ClickableActorInteractionHandlerAdapter() {
		override fun mouseClicked(context: ActorInteractionContext): ActorInteractionHandler? {
			if (context.mouseEvent!!.clickCount == 2) {
				requestOpenMemoryContents(false, context.mouseEvent!!.isAltDown)
			}
			return null
		}
	}
}