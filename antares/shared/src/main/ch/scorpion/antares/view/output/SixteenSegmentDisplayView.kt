package ch.scorpion.antares.view.output

import ch.scorpion.antares.model.output.SixteenSegmentDisplay
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.graph.view.ControlView
import ch.scorpion.jabbah.graph.view.ControlViewSource
import ch.scorpion.jabbah.graph.view.vertice.AbstractVerticeView

class SixteenSegmentDisplayView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	model: SixteenSegmentDisplay = SixteenSegmentDisplay(),
	lightColor: LightColor = DEFAULT_LIGHT_COLOR,
	eventBus: EventBus = BaseModule.eventBus
) : AbstractSegmentDisplayView<SixteenSegmentDisplay>(styleProvider, model, lightColor, eventBus) {

	companion object {
		const val PROP_ICON_PATH = "ch.scorpion.antares.view.output.SixteenSegmentDisplayView.iconPath"
	}

	override fun modelExchanged(oldModel: SixteenSegmentDisplay?) {
		super.modelExchanged(oldModel)
		createCombinedPortViews()
	}

	/** ---- [ControlView] */

	override val controlId: String get() = "16seg:" + model.id

	override fun sourcePropertiesChanged(source: ControlViewSource<SixteenSegmentDisplay>) {
		if (source is SixteenSegmentDisplayView) {
			copyControlViewProperties(source, this)
		}
	}

	/** ---- [ControlViewSource] */

	override val iconPath: String get() = BaseModule.properties.getString(SevenSegmentDisplayView.PROP_ICON_PATH)

	override fun createControlView(): ControlView<SixteenSegmentDisplay> {
		val clone = SixteenSegmentDisplayView(styleProvider, model, lightColor)
		clone.isShowPortViews = false
		clone.location = Point2D(0, 0)
		return clone
	}

	/** ---- [AbstractVerticeView] */

	override fun drawImpl(context: DrawContext) {
		super.drawImpl(context)

		drawA1(context)
		drawA2(context)
		drawB(context)
		drawC(context)
		drawD1(context)
		drawD2(context)
		drawE(context)
		drawF(context)
		drawG1(context)
		drawG2(context)
		drawH(context)
		drawI(context)
		drawJ(context)
		drawK(context)
		drawL(context)
		drawM(context)
	}

	private fun drawA1(context: DrawContext) {
		drawHalfHorizontalSegment(
			context, model.inputValueOf("a1"),
			0.5f * geom.scaledFactor + geom.segHalfWidth,
			geom.scaledFactor + geom.segHalfWidth)
	}

	private fun drawA2(context: DrawContext) {
		drawHalfHorizontalSegment(
			context, model.inputValueOf("a2"),
			0.5f * geom.scaledFactor + geom.segHalfWidth + geom.segLength / 2,
			geom.scaledFactor + geom.segHalfWidth)
	}

	private fun drawD1(context: DrawContext) {
		drawHalfHorizontalSegment(
			context, model.inputValueOf("d1"),
			0.5f * geom.scaledFactor + geom.segHalfWidth,
			7 * geom.scaledFactor + geom.segHalfWidth)
	}

	private fun drawD2(context: DrawContext) {
		drawHalfHorizontalSegment(
			context, model.inputValueOf("d2"),
			0.5f * geom.scaledFactor + geom.segHalfWidth + geom.segLength / 2,
			7 * geom.scaledFactor + geom.segHalfWidth
		)
	}

	private fun drawG1(context: DrawContext) {
		drawHalfHorizontalSegment(
			context, model.inputValueOf("g1"),
			0.5f * geom.scaledFactor + geom.segHalfWidth,
			4 * geom.scaledFactor + geom.segHalfWidth)
	}

	private fun drawG2(context: DrawContext) {
		drawHalfHorizontalSegment(
			context, model.inputValueOf("g2"),
			0.5f * geom.scaledFactor + geom.segHalfWidth + geom.segLength / 2,
			4 * geom.scaledFactor + geom.segHalfWidth)
	}

	private fun drawH(context: DrawContext) {
		drawDiagonalEastSegment(
			context, model.inputValueOf("h"),
			0.5f * geom.scaledFactor + geom.segHalfWidth,
			geom.scaledFactor + geom.segHalfWidth)
	}

	private fun drawI(context: DrawContext) {
		drawVerticalSegment(
			context, model.inputValueOf("i"),
			0.5f * geom.scaledFactor + geom.segHalfWidth + 0.5f * geom.segLength,
			1 * geom.scaledFactor + geom.segHalfWidth
		)
	}

	private fun drawJ(context: DrawContext) {
		drawDiagonalWestSegment(
			context, model.inputValueOf("j"),
			0.5f * geom.scaledFactor + geom.segLength + geom.segHalfWidth,
			geom.scaledFactor + geom.segHalfWidth)
	}

	private fun drawK(context: DrawContext) {
		drawDiagonalWestSegment(
			context, model.inputValueOf("k"),
			0.5f * geom.scaledFactor + geom.segHalfWidth + geom.segLength / 2,
			4 * geom.scaledFactor + geom.segHalfWidth
		)
	}

	private fun drawL(context: DrawContext) {
		drawVerticalSegment(
			context, model.inputValueOf("l"),
			0.5f * geom.scaledFactor + geom.segHalfWidth + 0.5f * geom.segLength,
			4 * geom.scaledFactor + geom.segHalfWidth)
	}

	private fun drawM(context: DrawContext) {
		drawDiagonalEastSegment(
			context, model.inputValueOf("m"),
			0.5f * geom.scaledFactor + geom.segHalfWidth + geom.segLength / 2,
			4 * geom.scaledFactor + geom.segHalfWidth)
	}

	private fun drawDiagonalEastSegment(context: DrawContext, value: Boolean, relX: Float, relY: Float) {
		context.g.translate(relX.toDouble(), relY.toDouble())
		context.g.color = getColor(value, context)
		context.g.fill(geom.diagonalEastPath)
		context.g.translate(-relX.toDouble(), -relY.toDouble())
	}

	private fun drawDiagonalWestSegment(context: DrawContext, value: Boolean, relX: Float, relY: Float) {
		context.g.translate(relX.toDouble(), relY.toDouble())
		context.g.color = getColor(value, context)
		context.g.fill(geom.diagonalWestPath)
		context.g.translate(-relX.toDouble(), -relY.toDouble())
	}
}