package ch.scorpion.antares.view.addressable

import ch.scorpion.antares.model.addressable.Addressable
import ch.scorpion.antares.model.addressable.LookupTable
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.jabbah.edit.Look
import ch.scorpion.antares.view.gate.BoxGateView
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.base.geom.Rotation
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.InputEventContext
import ch.scorpion.jabbah.draw.InputEventHandler
import ch.scorpion.jabbah.draw.drawable.AbstractDrawable
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.edit.model.text.*
import ch.scorpion.jabbah.execution.actor.ActorInteractionContext
import ch.scorpion.jabbah.execution.actor.ActorInteractionHandler
import ch.scorpion.jabbah.graph.model.GraphElementEvent
import ch.scorpion.jabbah.graph.model.vertice.ImmediateVerticeLink
import ch.scorpion.jabbah.graph.model.vertice.ObjectLink
import ch.scorpion.jabbah.graph.view.port.PortLabelPosition
import ch.scorpion.jabbah.graph.view.vertice.AbstractVerticeView

class LookupTableView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: LookupTable = LookupTable(),
	eventBus: EventBus = BaseModule.eventBus
) : BoxGateView<LookupTable>(styleProvider, "LUT", model, minWidth = 10), Labeled {

	companion object {
		private const val LABEL_DIST = Look.SCALE
	}

	private val inputEventHandler = AddressableInputEventHandler(
		eventBus,
	) { view, newDesktopView ->
		OpenMemoryContentsRequest(
			view,
			this,
			"LUT",
			ImmediateVerticeLink(this.model.id) as ObjectLink<Addressable>,
			newDesktopView
		)
	}

	private val externalLabel = Label(
		model.name,
		font,
		rotationDisplayStrategy = RotationDisplayStrategy.IGNORE)

	override val label: Label get() = externalLabel

	init {
		modelExchanged(null)
	}

	override fun modelExchanged(oldModel: LookupTable?) {
		super.modelExchanged(oldModel)
		addPortView(createInputPortView(model.getAddressInput()))
		createOutputPortView(model.getDataPort()).also {
			it.portLabelPosition = PortLabelPosition.INTERNAL
			addPortView(it)
		}
		updateLayout()
		updateExternalLabel()
	}

	override fun <T : InputEventContext> getInputEventHandler(context: T): InputEventHandler<T> =
		inputEventHandler.getInputEventHandler()

	override fun getActorInteractionHandler(context: ActorInteractionContext): ActorInteractionHandler =
		inputEventHandler.getActorInteractionHandler(this)

	/** ---- UI properties */

	var name: String?
		get() = model.name
		set(value) {
			if (value != model.name) {
				invalidate()
				model.name = value
				invalidate()
				validate()
			}
		}

	var addressWidth: BitWidth
		get() = model.addressWidth
		set(value) {
			invalidate()
			model.addressWidth = value
			invalidate()
			validate()
		}

	var dataWidth: BitWidth
		get() = model.dataWidth
		set(value) {
			invalidate()
			model.dataWidth = value
			invalidate()
			validate()
		}

	/** ---- [AbstractDrawable] */

	override val boundingBox: RectangularShape
		get() {
			val bb = Rectangle2D(super.boundingBox)
			bb.add(externalLabel.boundingBox)
			return bb
		}

	override fun draw(context: DrawContext) {
		super.draw(context)
		externalLabel.draw(context)
	}

	override var location: Point2D
		get() = super.location
		set(value) {
			super.location = value
			updateExternalLabelPosition()
		}

	/** ---- [AbstractVerticeView] */

	override var rotation: Rotation
		get() = super.rotation
		set(value) {
			super.rotation = value
			updateExternalLabel()
		}

	override fun handleStateChanged(event: GraphElementEvent) {
		if (event.signalHandler == null) {
			invalidate()
			updateExternalLabel()
		}
		super.handleStateChanged(event)
	}

	/** ---- [LookupTableView] */

	private fun updateExternalLabel() {
		externalLabel.text = StringUtils.orEmpty(model.name)
		updateExternalLabelPosition()
	}

	private fun updateExternalLabelPosition() {
		val r = super.boundingBox
		if (orientation.isHorizontal()) {
			externalLabel.location = Point2D(r.centerX, r.minY - LABEL_DIST)
			externalLabel.alignment = Alignment(HorizontalAlignment.CENTER, VerticalAlignment.BOTTOM)
		} else {
			externalLabel.location = Point2D(r.maxX + LABEL_DIST, r.centerY)
			externalLabel.alignment = Alignment(HorizontalAlignment.LEFT, VerticalAlignment.CENTER)
		}
		externalLabel.ownerRotation = rotation
	}
}