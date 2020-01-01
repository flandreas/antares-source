package ch.scorpion.antares.view.net

import ch.scorpion.antares.model.net.Tunnel
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.view.DigitalComponentView
import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.Rotation
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.drawable.AbstractDrawable
import ch.scorpion.jabbah.draw.graphics.DropShadow
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.edit.model.AbstractComponent
import ch.scorpion.jabbah.edit.model.text.HorizontalLabel
import ch.scorpion.jabbah.graph.GraphApplicationContext

/**
 * A view representation of a [Tunnel].
 */
class TunnelView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: Tunnel = Tunnel()
) : DigitalComponentView<Tunnel>(styleProvider, model) {

	constructor(
		name: String,
		styleProvider: StyleProvider = DrawStyleModule.styleProvider
	) : this(styleProvider, Tunnel(name))

	companion object {
		const val SIZE = 4 * 7
		const val LABEL_DIST = Look.SCALE
	}

	private val label = HorizontalLabel(
		owner = this,
		relLocation = Point2D(SIZE / 2 + (DigitalPortView.LENGTH + SIZE / 2) + LABEL_DIST, 0),
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

	/** ---- [AbstractDrawable] */

	override fun draw(context: DrawContext) {
		super.draw(context)
		context.g.color = context.choose(styleProvider.getStyle(StyleType.BACKGROUND).color).textColor
		label.draw(context)
	}

	override fun drawImpl(context: DrawContext) {
		if (shadow) {
			DropShadow.draw(context, transparency) {
				context.g.fillRect(xInt, yInt, SIZE, SIZE)
			}
		}

		context.g.color = if (context.useContextColors) {
			context.color!!.backgroundColor
		} else {
			propertiesBackgroundColor
		}
		context.g.fillRect(xInt, yInt, SIZE, SIZE)

		context.g.color = context.choose(color).foregroundColor
		context.g.stroke = stroke
		context.g.drawRect(xInt, yInt, SIZE, SIZE)

		// Draw the PortView above the border
		super.drawImpl(context)

		if (context.castedAppContext<GraphApplicationContext>()!!.isExecute) {
			context.g.color = model.getInOrOutSignal().getColor().foregroundColor
		}

		// Draw tunnel entry
		context.g.translate(xInt + SIZE / 2.0, 0.0)
		context.g.rotate(rotation.inverse().angle)
		context.g.fillOval(-SIZE / 4, -SIZE / 4, SIZE / 2, SIZE / 2)
		context.g.fillRect(-SIZE / 4, 0, SIZE / 2, SIZE / 4)

		context.g.rotate(-rotation.inverse().angle)
		context.g.translate(-(xInt + SIZE / 2.0), 0.0)
	}

	override val boundingBox: Rectangle2D
		get() {
			val bb = super.boundingBox
			val lbb = label.boundingBox.moveBy(location)
			bb.add(lbb)
			return bb
		}

	/** ---- [AbstractComponent] */

	override fun rotationChanged(newRotation: Rotation) {
		super.rotationChanged(newRotation)
		label.rotationChanged()
	}

	/** ---- [TunnelView] */

	private fun updateLabel() {
		invalidate()
		label.text = StringUtils.orEmpty(name)
		label.rotationChanged()
		invalidate()
	}
}