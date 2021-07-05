package ch.scorpion.antares.view.net

import ch.scorpion.antares.model.net.DigitalNet
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.signal.DigitalSignalRepresentation
import ch.scorpion.antares.view.signal.DigitalSignalView
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.drawable.AbstractDrawable
import ch.scorpion.jabbah.draw.drawable.Locatable
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.execution.module.ExecutionModule
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.net.netview.NetViewStyle
import ch.scorpion.jabbah.graph.view.net.node.NodeView
import ch.scorpion.jabbah.graph.view.style.EdgeStyle

/**
 * An animatable representation of an [EdgeView] while being executed.
 *
 * Consists of a [DigitalSignalView] that flows along the [DigitalEdgeView], and a repainting of the
 * [DigitalEdgeView] segments that have already been visited by the flowing [DigitalSignalView].
 *
 * The location of this [DigitalEdgeAnimationView] is the location of its inner [DigitalSignalView].
 *
 * @property edgeView the [DigitalEdgeView] along which the signal flows
 * @param signal the [DigitalSignal] that flows along [edgeView]
 * @param signalRepresentation determines the representation of the inner [DigitalSignalView]
 * @property reverseDirection 'false' if the signal is flowing from the origin to the destination,
 * 'true' if it is flowing from the destination to the origin
 */
class DigitalEdgeAnimationView(
	val edgeView: DigitalEdgeView,
	signal: DigitalSignal,
	signalRepresentation: DigitalSignalRepresentation,
	val reverseDirection: Boolean,
	styleProvider: StyleProvider
) : AbstractDrawable(), Locatable {

	/** The [DigitalSignalView] being drawn at the head of the signal flow animation.*/
	private val signalView = DigitalSignalView(signal, signal.bitWidth, signalRepresentation)

	/**
	 * Determines whether [signalView] is drawn or not. Used when [DigitalEdgeAnimationView]s get split
	 * at a [NodeView], and the [EdgeView] incoming at the [NodeView] should still be drawn, but
	 * without the [DigitalSignalView], which is only to be drawn by the front-running animations.
	 **/
	var drawSignalView: Boolean = true
		set(value) {
			if (value != field) {
				invalidate()
				field = value
				if (!value && artificialNodeView != null) {
					signalView.orthoPolyline.add(artificialNodeView!!.location)
				}
				invalidate()
				validate()
			}
		}

	/** An artificial [NodeView] to be drawn above the end [NodeView], if any. */
	private var artificialNodeView: DigitalNodeView? = null

	init {
		DrawableOwner(this, signalView)

		if (edgeView.netView!!.style == NetViewStyle.LINE) {
			if (reverseDirection) {
				if (edgeView.origin?.connectableView is NodeView<*>) {
					artificialNodeView = DigitalNodeView(styleProvider, ExecutionModule.currentSystemSpeedCategory,
						DummyNet(signal), NetViewStyle.LINE)
					artificialNodeView!!.location = edgeView.getSegmentPoint(0)
				}
			} else {
				if (edgeView.destination?.connectableView is NodeView<*>) {
					artificialNodeView = DigitalNodeView(styleProvider, ExecutionModule.currentSystemSpeedCategory,
						DummyNet(signal), NetViewStyle.LINE)
					artificialNodeView!!.location = edgeView.getSegmentPoint(edgeView.segmentPointCount - 1)
				}
			}
		}
	}

	/**
	 * A dummy [DigitalNet] implementation that returns the animated signal, which is used for animation
	 * painting.
	 */
	private class DummyNet(override val signal: DigitalSignal?) : DigitalNet()

	/** ---- [Drawable] */

	override val boundingBox: RectangularShape
		get() {
			val bbox = signalView.boundingBox
			bbox.add(signalView.orthoPolyline.boundingBox)
			if (artificialNodeView != null) {
				bbox.add(artificialNodeView!!.boundingBox)
			}
			return bbox
		}

	override fun draw(context: DrawContext) {
		context.g.color = signalView.signal.getColor().foregroundColor
		context.g.stroke = (edgeView.style as EdgeStyle).executionStroke

		val polyline = signalView.orthoPolyline
		for (i in 0..polyline.size - 2) {
			val begin = polyline.get(i)
			val end = polyline.get(i + 1)
			context.g.drawLine(begin.x.toInt(), begin.y.toInt(), end.x.toInt(), end.y.toInt())
		}

		if (drawSignalView) {
			signalView.draw(context)
		} else {
			artificialNodeView?.draw(context)
		}
	}

	override fun contains(x: Double, y: Double): Boolean {
		return boundingBox.contains(x, y)
	}

	/** ---- [Locatable] */

	override var location: Point2D
		get() = signalView.location
		set(value) {
			invalidate()
			signalView.location = value
			invalidate()
		}
}