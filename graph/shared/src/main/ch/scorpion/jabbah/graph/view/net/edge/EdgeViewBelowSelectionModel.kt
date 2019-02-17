package ch.scorpion.jabbah.graph.view.net.edge

import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.edit.SelectionDrawingStrategy
import ch.scorpion.jabbah.edit.SelectionModel
import ch.scorpion.jabbah.edit.select.AbstractSelectionModel
import ch.scorpion.jabbah.edit.style.EditStyleType
import ch.scorpion.jabbah.graph.view.EdgeView


/**
 * A [SelectionModel] for [SelectionDrawingStrategy.BELOW] of [EdgeView].
 */
class EdgeViewBelowSelectionModel(
	component: EdgeView<*>,
	private val styleProvider: StyleProvider) : AbstractSelectionModel<EdgeView<*>>(component) {

	constructor(component: EdgeView<*>) : this(component, DrawStyleModule.styleProvider)

	/** ---- [AbstractSelectionModel] */

	override val boundingBox: RectangularShape = Rectangle2D()

	override fun draw(context: DrawContext) {
		val oldStroke = context.g.stroke
		val oldColor = context.g.color

		context.g.stroke = styleProvider.getStyle(EditStyleType.HIGHLIGHT).stroke
		context.g.color = styleProvider.getStyle(EditStyleType.HIGHLIGHT).color.backgroundColor

		for (i in 0 until component.segmentPointCount - 1) {
			val begin = component.getSegmentPoint(i)
			val end = component.getSegmentPoint(i + 1)
			context.g.drawLine(begin.x.toInt(), begin.y.toInt(), end.x.toInt(), end.y.toInt())
		}

		context.g.color = oldColor
		context.g.stroke = oldStroke
	}

	override fun componentUpdated() {
		invalidate()
		val bbox = component.boundingBox
		val outset = strokeWidth / 2
		boundingBox.setFrame(
			bbox.x - outset,
			bbox.y - outset,
			bbox.width + 2 * outset,
			bbox.height + 2 * outset)
		invalidate()
		validate()
	}

	override fun contains(x: Double, y: Double): Boolean = boundingBox.contains(x, y)

	/** ---- [EdgeViewBelowSelectionModel] */

	private val strokeWidth: Float = styleProvider.getStyle(EditStyleType.HIGHLIGHT).stroke.width

}