package ch.scorpion.antares.view.net

import ch.scorpion.antares.model.net.Tunnel
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.view.DigitalComponentView
import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.Rotation
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.drawable.AbstractDrawable
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.DropShadow
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.edit.model.AbstractComponent
import ch.scorpion.jabbah.edit.model.text.HorizontalLabel
import ch.scorpion.jabbah.graph.GraphApplicationContext

enum class TunnelViewFace(val customName: String) {

	TUNNEL("tunnel") {

		override val labelDist: Int get() = Look.SCALE

		override fun drawShadow(view: TunnelView, context: DrawContext) {
			if (view.shadow) {
				DropShadow.draw(context, view.transparency) {
					context.g.fillRect(view.xInt, view.yInt, TunnelView.SIZE, TunnelView.SIZE)
				}
			}
		}

		override fun draw(view: TunnelView, context: DrawContext, background: Color) {
			context.g.color = if (context.useContextColors) {
				context.color!!.backgroundColor
			} else {
				background
			}
			context.g.fillRect(view.xInt, view.yInt, TunnelView.SIZE, TunnelView.SIZE)

			context.g.color = context.choose(view.color).foregroundColor
			context.g.stroke = view.stroke
			context.g.drawRect(view.xInt, view.yInt, TunnelView.SIZE, TunnelView.SIZE)

			if (context.castedAppContext<GraphApplicationContext>()!!.isExecute) {
				context.g.color = view.model.getInOrOutSignal().getColor().foregroundColor
			}

			// Draw tunnel entry
			context.g.translate(view.xInt + TunnelView.SIZE / 2.0, 0.0)
			context.g.rotate(view.rotation.inverse().angle)
			context.g.fillOval(-TunnelView.SIZE / 4, -TunnelView.SIZE / 4, TunnelView.SIZE / 2, TunnelView.SIZE / 2)
			context.g.fillRect(-TunnelView.SIZE / 4, 0, TunnelView.SIZE / 2, TunnelView.SIZE / 4)

			context.g.rotate(-view.rotation.inverse().angle)
			context.g.translate(-(view.xInt + TunnelView.SIZE / 2.0), 0.0)
		}
	},

	ARROW ("arrow") {

		private val path = System.createPath()
			.moveTo(0, 0)
			.lineTo(3.0 * 7, - 1.5 * 7)
			.lineTo(3.0 * 7, 1.5 * 7)
			.lineTo(0, 0)
			.close()

		override val labelDist: Int get() = 0

		override fun drawShadow(view: TunnelView, context: DrawContext) {
			if (view.shadow) {
				DropShadow.draw(context, view.transparency) {
					context.g.translate(DigitalPortView.LENGTH.toDouble(), 0.0)
					context.g.fill(path)
					context.g.translate(-DigitalPortView.LENGTH.toDouble(), 0.0)
				}
			}
		}
		override fun draw(view: TunnelView, context: DrawContext, background: Color) {
			context.g.translate(DigitalPortView.LENGTH.toDouble(), 0.0)

			if (context.castedAppContext<GraphApplicationContext>()!!.isExecute) {
				context.g.color = view.model.getInOrOutSignal().getColor().foregroundColor
			} else {
				context.g.color = if (context.useContextColors) {
					context.color!!.backgroundColor
				} else {
					background
				}
			}
			context.g.fill(path)

			if (context.castedAppContext<GraphApplicationContext>()!!.isExecute) {
				context.g.color = view.model.getInOrOutSignal().getColor().backgroundColor
			} else {
				context.g.color = context.choose(view.color).foregroundColor
			}

			context.g.stroke = view.stroke
			context.g.draw(path)

			context.g.translate(-DigitalPortView.LENGTH.toDouble(), 0.0)
		}
	};

	companion object {

		/** The name of the [String] property in [Properties] designating the [TunnelViewFace]'s custom name.*/
		const val PROP_TUNNEL_FACE = "ch.scorpion.antares.view.net.tunnelFace"

		fun withName(customName: String): TunnelViewFace {
			for (tunnelViewFace in values()) {
				if (tunnelViewFace.customName == customName) {
					return tunnelViewFace
				}
			}
			throw IllegalArgumentException("Unknown TunnelViewFace '$customName'")
		}
	}

	abstract val labelDist: Int
	abstract fun drawShadow(view: TunnelView, context: DrawContext)
	abstract fun draw(view: TunnelView, context: DrawContext, background: Color)

	override fun toString(): String {
		return when(this) {
			TUNNEL -> Translations.getString("element.tunnelViewFace.tunnel")
			ARROW -> Translations.getString("element.tunnelViewFace.arrow")
		}
	}
}

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
		private val face: TunnelViewFace get() = TunnelViewFace.withName(BaseModule.properties.getString(TunnelViewFace.PROP_TUNNEL_FACE))
	}

	private val label = HorizontalLabel(
		owner = this,
		relLocation = Point2D(SIZE / 2 + (DigitalPortView.LENGTH + SIZE / 2) + face.labelDist, 0),
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
		face.drawShadow(this, context)
		super.drawImpl(context)
		face.draw(this, context, propertiesBackgroundColor)
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