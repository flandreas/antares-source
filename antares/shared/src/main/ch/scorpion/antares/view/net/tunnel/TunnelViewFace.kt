package ch.scorpion.antares.view.net.tunnel

import ch.scorpion.antares.view.Look.SCALE
import ch.scorpion.antares.view.port.AbstractAntaresPortView
import ch.scorpion.jabbah.base.EnumProperty
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.geom.Path
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.drawable.Transparent
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.DropShadow
import ch.scorpion.jabbah.graph.GraphApplicationContext

enum class TunnelViewFace(
	override val customName: String
) : EnumProperty<TunnelViewFace> {

	TUNNEL("tunnel") {

		override val labelDist: Int get() = SCALE

		override fun drawShadow(view: TunnelView, context: DrawContext) {
			if (view.shadow) {
				DropShadow.draw(context, view.transparency) {
					context.g.fillRect(view.xInt, view.yInt, TunnelView.SIZE, TunnelView.SIZE)
				}
			}
		}

		override fun draw(view: TunnelView, context: DrawContext, background: Color) {
			context.g.color = if (context.useContextColors) {
				Transparent.applyTo(view.transparency, context.color!!.backgroundColor)
			} else {
				background
			}
			context.g.fillRect(view.xInt, view.yInt, TunnelView.SIZE, TunnelView.SIZE)

			context.g.color = Transparent.applyTo(view.transparency, context.choose(view.color).foregroundColor)
			context.g.stroke = view.stroke
			context.g.drawRect(view.xInt, view.yInt, TunnelView.SIZE, TunnelView.SIZE)

			if (context.castedAppContext<GraphApplicationContext>()!!.isExecute) {
				context.g.color = Transparent.applyTo(view.transparency, view.model.getInOrOutSignal().color.foregroundColor)
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

		private val outPath = System.createPath()
			.moveTo(0, 0)
			.lineTo(3.0 * SCALE, -1.5 * SCALE)
			.lineTo(3.0 * SCALE, 1.5 * SCALE)
			//.lineTo(0, 0)
			.close()

		private val inPath = System.createPath()
			.moveTo(3.0 * SCALE, 0.0)
			.lineTo(0.0, -1.5 * SCALE)
			.lineTo(0.0, 1.5 * SCALE)
			//.lineTo(3.0 * SCALE, 0.0)
			.close()

		private val inOutPath = System.createPath()
			.moveTo(0, 0)
			.lineTo(1.0 * SCALE, -1.5 * SCALE)
			.lineTo(1.0 * SCALE, 1.5 * SCALE)
			.close()
			.moveTo(3.0 * SCALE, 0.0)
			.lineTo(2.0 * SCALE, -1.5 * SCALE)
			.lineTo(2.0 * SCALE, 1.5 * SCALE)
			.close()

		override val labelDist: Int get() = 0

		override fun drawShadow(view: TunnelView, context: DrawContext) {
			if (view.shadow) {
				DropShadow.draw(context, view.transparency) {
					context.g.translate(AbstractAntaresPortView.LENGTH.toDouble(), 0.0)
					context.g.fill(getPath(view))
					context.g.translate(-AbstractAntaresPortView.LENGTH.toDouble(), 0.0)
				}
			}
		}
		override fun draw(view: TunnelView, context: DrawContext, background: Color) {
			context.g.translate(AbstractAntaresPortView.LENGTH.toDouble(), 0.0)

			// Fill global TunnelViews with the component's foreground color

			if (context.castedAppContext<GraphApplicationContext>()!!.isExecute) {
				context.g.color = Transparent.applyTo(view.transparency, view.model.getInOrOutSignal().color.foregroundColor)
			} else {
				context.g.color = if (context.useContextColors) {
					if (view.isGlobal) {
						context.color!!.foregroundColor
					} else {
						context.color!!.backgroundColor
					}
				} else {
					if (view.isGlobal) {
						view.foregroundColor
					} else {
						background
					}
				}
			}
			context.g.fill(getPath(view))

			context.g.color = Transparent.applyTo(view.transparency, context.choose(view.color).foregroundColor)

			context.g.stroke = view.stroke
			context.g.draw(getPath(view))

			context.g.translate(-AbstractAntaresPortView.LENGTH.toDouble(), 0.0)
		}

		private fun getPath(view: TunnelView): Path =
			when (view.flowDirection) {
				TunnelFlowDirection.Undefined -> outPath
				TunnelFlowDirection.In -> inPath
				TunnelFlowDirection.Out -> outPath
				TunnelFlowDirection.InOut -> inOutPath
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