package ch.scorpion.jabbah.edit.model.curve

import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.drawable.AbstractRectangularUnzoomable
import ch.scorpion.jabbah.draw.module.DrawModule
import ch.scorpion.jabbah.edit.select.Handle

class QuadCurveHandleSelectionModel(
	c: QuadCurveComponent
) : AbstractCurveHandleSelectionModel<QuadCurveComponent>(c) {

	override fun calculateRequiredHandlesCount(): Int = 3

	override fun draw(context: DrawContext) {
		drawTangents(context)
		super.draw(context)
	}

	private fun drawTangents(context: DrawContext) {
		context.g.stroke = TANGENT_STROKE
		context.g.color = DrawModule.properties.getColor(Handle.PROP_BORDER_COLOR)
		context.g.drawLine(
			(getHandle(0) as AbstractRectangularUnzoomable).viewLocation,
			(getHandle(1) as AbstractRectangularUnzoomable).viewLocation
		)
		context.g.drawLine(
			(getHandle(1) as AbstractRectangularUnzoomable).viewLocation,
			(getHandle(2) as AbstractRectangularUnzoomable).viewLocation
		)
	}
}