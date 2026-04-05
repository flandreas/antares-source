package io.antarescircuit.jabbah.graph.view.net.edge

import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.base.geom.RectangularShape
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.edit.SelectionDrawingStrategy
import io.antarescircuit.jabbah.edit.SelectionModel
import io.antarescircuit.jabbah.edit.select.AbstractSelectionModel
import io.antarescircuit.jabbah.edit.style.EditStyleType
import io.antarescircuit.jabbah.graph.view.EdgeView


/**
 * A [SelectionModel] for [SelectionDrawingStrategy.BELOW] of [EdgeView].
 */
class EdgeViewBelowSelectionModel(
	component: EdgeView<*>,
	private val styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	private val styleType: EditStyleType = EditStyleType.SELECTION
) : AbstractSelectionModel<EdgeView<*>>(component) {

	/** ---- [AbstractSelectionModel] */

	private var _boundingBox = Rectangle2D()
	override val boundingBox: RectangularShape get() = _boundingBox

	override fun draw(context: DrawContext) {
		val oldStroke = context.g.stroke
		val oldColor = context.g.color

		context.g.stroke = styleProvider.getStyle(styleType).stroke
		context.g.color = styleProvider.getStyle(styleType).color.backgroundColor

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
		_boundingBox.setFrame(
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