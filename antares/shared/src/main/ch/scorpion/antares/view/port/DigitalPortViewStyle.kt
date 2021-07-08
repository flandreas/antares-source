package ch.scorpion.antares.view.port

import ch.scorpion.antares.model.Logic
import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.geom.Direction
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.drawable.Transparent
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.graph.view.style.GraphStyleType

/**
 * Defines the various ways in which the access (and only the access) of a [DigitalPortView] is drawn.
 */
enum class DigitalPortViewStyle(val customName: String) {

	Line("line") {

		override val unconnectedLength: Int get() = DigitalPortView.LENGTH

		override fun getConnectedLength(portView: DigitalPortView): Int {
			with(portView) {
				if (showLogicAnnotation && getDigitalPort().logic == Logic.NEGATIVE) {
					return DigitalPortView.LOGIC_SIZE
				}
				return 0
			}
		}

		override fun isDrawAccess(portView: DigitalPortView): Boolean = !portView.port.isConnected

		override fun drawAccess(portView: DigitalPortView, context: DrawContext, styleProvider: StyleProvider, transparent: Transparent) {
			val connPoint = portView.connectionPoint
			context.g.drawLine(portView.locationX.toInt(), portView.locationY.toInt(), connPoint.x.toInt(), connPoint.y.toInt())
		}

		override fun drawLogic(portView: DigitalPortView, context: DrawContext, styleProvider: StyleProvider, transparent: Transparent) {
			with(portView) {
				if (showLogicAnnotation && getDigitalPort().logic == Logic.NEGATIVE) {
					val logicBox = LOGIC_BOXES[portView.direction]!!

					val fillColor = if (Look.FILL_BASIC_COMPONENTS) {
						context.styleColor(styleProvider.getStyle(StyleType.BACKGROUND).color)
					} else {
						styleProvider.getStyle(StyleType.BACKGROUND).color
					}
					context.g.color = transparent.applyTo(context.choose(fillColor).backgroundColor)
					context.g.fillOval(logicBox.x, logicBox.y, logicBox.width, logicBox.height)

					context.g.stroke = Themes.get<AntaresTheme>().figure.stroke
					context.g.color = transparent.applyTo(context.choose(context.styleColor(styleProvider.getStyle(GraphStyleType.VERTICE).color)).foregroundColor)
					context.g.drawOval(logicBox.x, logicBox.y, logicBox.width, logicBox.height)
				}
			}
		}
	},

	DIL("DIL") {

		override val unconnectedLength: Int get() = DIL_ACCESS_W

		override fun getConnectedLength(portView: DigitalPortView): Int = DIL_ACCESS_W

		override fun isDrawAccess(portView: DigitalPortView): Boolean = true

		override fun drawAccess(portView: DigitalPortView, context: DrawContext, styleProvider: StyleProvider, transparent: Transparent) {
			with(portView) {
				val access = DIL_ACCESSES[portView.direction]!!

				val fillColor = if (Look.FILL_BASIC_COMPONENTS) {
					context.styleColor(styleProvider.getStyle(StyleType.BACKGROUND).color)
				} else {
					styleProvider.getStyle(StyleType.BACKGROUND).color
				}
				context.g.color = transparent.applyTo(context.choose(fillColor).backgroundColor)
				context.g.fillRect(locationX + access.x, locationY + access.y, access.width, access.height)

				context.g.stroke = Themes.get<AntaresTheme>().figure.stroke
				context.g.color = transparent.applyTo(context.choose(context.styleColor(styleProvider.getStyle(GraphStyleType.VERTICE).color)).foregroundColor)
				context.g.drawRect(locationX + access.x, locationY + access.y, access.width, access.height)
			}
		}

		override fun drawLogic(portView: DigitalPortView, context: DrawContext, styleProvider: StyleProvider, transparent: Transparent) { }
	};

	companion object {

		private val LOGIC_BOX = Rectangle2D(0, -DigitalPortView.LOGIC_SIZE / 2, DigitalPortView.LOGIC_SIZE, DigitalPortView.LOGIC_SIZE)
		private val LOGIC_BOXES = mapOf(
			Direction.EAST to Direction.EAST.rotation.rotateRectangleAround(Point2D.ZERO, LOGIC_BOX),
			Direction.NORTH to Direction.NORTH.rotation.rotateRectangleAround(Point2D.ZERO, LOGIC_BOX),
			Direction.WEST to Direction.WEST.rotation.rotateRectangleAround(Point2D.ZERO, LOGIC_BOX),
			Direction.SOUTH to Direction.SOUTH.rotation.rotateRectangleAround(Point2D.ZERO, LOGIC_BOX))

		private const val DIL_ACCESS_W = DigitalPortView.LOGIC_SIZE / 2
		private const val DIL_ACCESS_H = DigitalPortView.LOGIC_SIZE
		private val DIL_BOX = Rectangle2D(0, -DIL_ACCESS_H / 2, DIL_ACCESS_W, DIL_ACCESS_H)
		private val DIL_ACCESSES = mapOf(
			Direction.EAST to Direction.EAST.rotation.rotateRectangleAround(Point2D.ZERO, DIL_BOX),
			Direction.NORTH to Direction.NORTH.rotation.rotateRectangleAround(Point2D.ZERO, DIL_BOX),
			Direction.WEST to Direction.WEST.rotation.rotateRectangleAround(Point2D.ZERO, DIL_BOX),
			Direction.SOUTH to Direction.SOUTH.rotation.rotateRectangleAround(Point2D.ZERO, DIL_BOX))

		fun withName(customName: String): DigitalPortViewStyle {
			return values().firstOrNull { it.customName == customName }
				?: throw IllegalArgumentException("unknown DigitalPortViewStyle '$customName'")
		}
	}

	abstract val unconnectedLength: Int

	abstract fun getConnectedLength(portView: DigitalPortView): Int

	abstract fun isDrawAccess(portView: DigitalPortView): Boolean

	/** Draws the access (e.g. a line) of [portView] in a [DrawContext] located at the origin of [DigitalPortView]. */
	abstract fun drawAccess(portView: DigitalPortView, context: DrawContext, styleProvider: StyleProvider, transparent: Transparent)

	abstract fun drawLogic(portView: DigitalPortView, context: DrawContext, styleProvider: StyleProvider, transparent: Transparent)

	override fun toString(): String {
		return when (this) {
			Line -> Translations.getString("element.property.DigitalPortViewStyle.Line.name")
			DIL -> Translations.getString("element.property.DigitalPortViewStyle.DIL.name")
		}
	}
}