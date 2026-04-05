package io.antarescircuit.jabbah.edit.model.curve

import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.drawable.AbstractRectangularUnzoomable
import io.antarescircuit.jabbah.draw.module.DrawModule
import io.antarescircuit.jabbah.edit.select.Handle

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