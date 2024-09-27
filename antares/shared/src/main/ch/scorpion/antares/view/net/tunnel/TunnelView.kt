package ch.scorpion.antares.view.net.tunnel

import ch.scorpion.antares.model.net.Tunnel
import ch.scorpion.antares.model.net.TunnelName
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.view.OrientableRectangularVerticeView
import ch.scorpion.antares.view.DigitalGraphView
import ch.scorpion.antares.view.port.AbstractAntaresPortView
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.geom.*
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.drawable.AbstractDrawable
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.model.AbstractComponent
import ch.scorpion.jabbah.edit.model.text.HorizontalLabel
import ch.scorpion.jabbah.edit.model.text.Label
import ch.scorpion.jabbah.edit.model.text.Labeled
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.io.Storable
import ch.scorpion.jabbah.io.StoreReader
import ch.scorpion.jabbah.io.StoreWriter

/**
 * A view representation of a [Tunnel].
 */
class TunnelView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: Tunnel = Tunnel()
) : OrientableRectangularVerticeView<Tunnel>(styleProvider, model), Labeled {

	constructor(
		name: String,
		styleProvider: StyleProvider = DrawStyleModule.styleProvider
	) : this(styleProvider, Tunnel(name))

	companion object {
		const val SIZE = 4 * 7
		val face: TunnelViewFace get() = TunnelViewFace.withName(BaseModule.properties.getString(TunnelViewFace.PROP_TUNNEL_FACE))
	}

	private val horizontalLabel = HorizontalLabel(
		owner = this,
		relLocation = Point2D(SIZE / 2 + (AbstractAntaresPortView.LENGTH + SIZE / 2) + face.labelDist, 0),
		font = font
	)

	init {
		modelExchanged(null)
		setBounds(getInput().unconnectedLength, -SIZE / 2, SIZE, SIZE)
	}

	override fun modelExchanged(oldModel: Tunnel?) {
		super.modelExchanged(oldModel)
		val portView = DigitalPortView(
			styleProvider = styleProvider,
			port = model.getPort(),
			direction = Direction.WEST)
		portView.setLocation(portView.length.toDouble(), 0.0)
		addPortView(portView)

		updateLabel()
	}

	/** ---- UI properties */

	var name: String?
		get() = model.name
		set(value) {
			model.name = value
			updateLabel()
		}

	var bitWidth: BitWidth
		get() = model.bitWidth
		set(value) {
			invalidate()
			model.bitWidth = value
			invalidate()
		}

	@Suppress("unused") // Reflection
	var tunnelName: TunnelName?
		get() = model.tunnelName
		set(value) {
			model.tunnelName = value
			updateLabel()
		}

	/** Defaults to [TunnelFlowDirection.Undefined] due to backward compatibility. */
	var flowDirection: TunnelFlowDirection = TunnelFlowDirection.Undefined

	@Suppress("unused") // Reflection
	var isGlobal: Boolean
		get() = model.isGlobal
		set(value) {
			invalidate()
			model.isGlobal = value
			invalidate()
			validate()
		}

	/** ---- [AbstractDrawable] */

	override fun draw(context: DrawContext) {
		super.draw(context)
		context.g.color = context.choose(styleProvider.getStyle(StyleType.BACKGROUND).color).textColor
		horizontalLabel.draw(context)
	}

	override fun drawImpl(context: DrawContext) {
		face.drawShadow(this, context)
		super.drawImpl(context)
		face.draw(this, context, propertiesBackgroundColor)
	}

	override val boundingBox: RectangularShape
		get() {
			val bb = Rectangle2D(super.boundingBox)
			val lbb = Rectangle2D(horizontalLabel.boundingBox).moveBy(location)
			bb.add(lbb)
			return bb
		}

	/** ---- [Labeled] */

	override val label: Label get() = horizontalLabel.label

	/** ---- [AbstractComponent] */

	override fun rotationChanged(newRotation: Rotation) {
		super.rotationChanged(newRotation)
		horizontalLabel.rotationChanged()
	}

	/**
	 * Collects all other [TunnelViews][TunnelView] with the same name, and
	 * the [DigitalEdgeView] connected to this [TunnelView], if any, along with
	 * their select buddies.
	 */
	override fun collectSelectBuddies(drawing: Drawing<Component>, buddies: MutableSet<Component>) {
		(drawing as DigitalGraphView)
			.getDrawables { it !== this && it is TunnelView && it.name == name }
			.forEach { otherTunnel ->
				if (!buddies.contains(otherTunnel)) {
					buddies.add(otherTunnel)
					otherTunnel.collectSelectBuddies(drawing, buddies)
				}
			}

		(drawing as GraphView).getEdgeView(model.getPort<DigitalSignal>())?.let {
			if (!buddies.contains(it)) {
				buddies.add(it)
				it.collectSelectBuddies(drawing, buddies)
			}
		}
	}

	/** ---- [TunnelView] */

	private fun updateLabel() {
		invalidate()
		horizontalLabel.text = StringUtils.orEmpty(name)
		horizontalLabel.rotationChanged()
		invalidate()
	}

	/** ---- [Storable] interface */

	override fun write(writer: StoreWriter) {
		super.write(writer)
		if (flowDirection != TunnelFlowDirection.Undefined) {
			writer.writeString("flowDir", flowDirection.customName)
		}
	}

	override fun read(reader: StoreReader) {
		super.read(reader)
		if (reader.hasAttribute("flowDir")) {
			flowDirection = TunnelFlowDirection.withName(reader.readString("flowDir"))
		}
	}
}