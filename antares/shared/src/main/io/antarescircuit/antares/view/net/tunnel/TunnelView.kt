package io.antarescircuit.antares.view.net.tunnel

import io.antarescircuit.antares.model.net.Tunnel
import io.antarescircuit.antares.model.net.TunnelName
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.view.DigitalGraphView
import io.antarescircuit.antares.view.port.AbstractAntaresPortView
import io.antarescircuit.antares.view.port.DigitalPortView
import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.drawable.AbstractDrawable
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.Drawing
import io.antarescircuit.jabbah.edit.model.AbstractComponent
import io.antarescircuit.jabbah.graph.model.Port
import io.antarescircuit.jabbah.graph.view.EdgeView
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.LabeledRectangularVerticeView
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule
import io.antarescircuit.jabbah.graph.view.net.edge.EdgeViewConnectionGeometry
import io.antarescircuit.jabbah.io.Storable
import io.antarescircuit.jabbah.io.StoreReader
import io.antarescircuit.jabbah.io.StoreWriter

/**
 * A view representation of a [Tunnel].
 */
class TunnelView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: Tunnel = Tunnel()
) : LabeledRectangularVerticeView<Tunnel>(styleProvider, model) {

	constructor(
		name: String,
		styleProvider: StyleProvider = DrawStyleModule.styleProvider
	) : this(styleProvider, Tunnel(name))

	companion object {
		private val LOG by logger(TunnelView::class)
		const val SIZE = 4 * 7
		val face: TunnelViewFace get() = TunnelViewFace.withName(BaseModule.properties.getString(TunnelViewFace.PROP_TUNNEL_FACE))
	}

	init {
		initExternalLabel()
		modelExchanged(null)
		setBounds(getInput().unconnectedLength, -SIZE / 2, SIZE, SIZE)
	}

	override val relativeExternalLabelLocation: Point2D get() =
		Point2D(SIZE / 2 + (AbstractAntaresPortView.LENGTH + SIZE / 2) + face.labelDist, 0)

	override fun modelExchanged(oldModel: Tunnel?) {
		super.modelExchanged(oldModel)
		val portView = DigitalPortView(
			styleProvider = styleProvider,
			port = model.getPort(),
			direction = Direction.WEST)
		portView.setLocation(portView.length.toDouble(), 0.0)
		addPortView(portView)

		updateLabels()
	}

	/** ---- UI properties */

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
			updateLabels()
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

	override fun drawImpl(context: DrawContext) {
		face.drawShadow(this, context)
		super.drawImpl(context)
		face.draw(this, context, propertiesBackgroundColor)
	}

	/** ---- [AbstractComponent] */

	override fun <G : Any> handleConnect(edgeView: EdgeView<G>, port: Port<G>?, geometry: EdgeViewConnectionGeometry) {
		super.handleConnect(edgeView, port, geometry)
		if (isNotReading) {
			if (tunnelName == null) {
				GraphViewModule.verticeViewNameStrategy.getConnectedName(model, model, edgeView)?.let {
					LOG.userTrail("Set name of Tunnel $id to '$it' derived from Net")
					tunnelName = TunnelName(it)
				}
			}
		}
	}

	/**
	 * Collects all other [TunnelViews][TunnelView] with the same name, and
	 * the [EdgeView] connected to this [TunnelView], if any, along with
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