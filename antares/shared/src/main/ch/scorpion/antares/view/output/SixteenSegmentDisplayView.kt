package ch.scorpion.antares.view.output

import ch.scorpion.antares.model.output.SixteenSegmentDisplay
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.graphics.Graphics2D
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.graph.GraphApplicationContext
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

		val isExecute = context.castedAppContext<GraphApplicationContext>()!!.isExecute

		drawA1(context, isExecute)
		drawA2(context, isExecute)
		drawB(context, isExecute)
		drawC(context, isExecute)
		drawD1(context, isExecute)
		drawD2(context, isExecute)
		drawE(context, isExecute)
		drawF(context, isExecute)
		drawG1(context, isExecute)
		drawG2(context, isExecute)
		drawH(context, isExecute)
		drawI(context, isExecute)
		drawJ(context, isExecute)
		drawK(context, isExecute)
		drawL(context, isExecute)
		drawM(context, isExecute)
	}

	private fun drawA1(context: DrawContext, isExecute: Boolean) {
		drawHalfHorizontalSegment(
			context.g, isExecute, model.inputValueOf("a1"),
			0.5f * geom.scaledFactor + geom.segHalfWidth,
			geom.scaledFactor + geom.segHalfWidth)
	}

	private fun drawA2(context: DrawContext, isExecute: Boolean) {
		drawHalfHorizontalSegment(
			context.g, isExecute, model.inputValueOf("a2"),
			0.5f * geom.scaledFactor + geom.segHalfWidth + geom.segLength / 2,
			geom.scaledFactor + geom.segHalfWidth)
	}

	private fun drawD1(context: DrawContext, isExecute: Boolean) {
		drawHalfHorizontalSegment(
			context.g, isExecute, model.inputValueOf("d1"),
			0.5f * geom.scaledFactor + geom.segHalfWidth,
			7 * geom.scaledFactor + geom.segHalfWidth)
	}

	private fun drawD2(context: DrawContext, isExecute: Boolean) {
		drawHalfHorizontalSegment(
			context.g, isExecute, model.inputValueOf("d2"),
			0.5f * geom.scaledFactor + geom.segHalfWidth + geom.segLength / 2,
			7 * geom.scaledFactor + geom.segHalfWidth
		)
	}

	private fun drawG1(context: DrawContext, isExecute: Boolean) {
		drawHalfHorizontalSegment(
			context.g, isExecute, model.inputValueOf("g1"),
			0.5f * geom.scaledFactor + geom.segHalfWidth,
			4 * geom.scaledFactor + geom.segHalfWidth)
	}

	private fun drawG2(context: DrawContext, isExecute: Boolean) {
		drawHalfHorizontalSegment(
			context.g, isExecute, model.inputValueOf("g2"),
			0.5f * geom.scaledFactor + geom.segHalfWidth + geom.segLength / 2,
			4 * geom.scaledFactor + geom.segHalfWidth)
	}

	private fun drawH(context: DrawContext, isExecute: Boolean) {
		drawDiagonalEastSegment(
			context.g, isExecute, model.inputValueOf("h"),
			0.5f * geom.scaledFactor + geom.segHalfWidth,
			geom.scaledFactor + geom.segHalfWidth)
	}

	private fun drawI(context: DrawContext, isExecute: Boolean) {
		drawVerticalSegment(
			context.g, isExecute, model.inputValueOf("i"),
			0.5f * geom.scaledFactor + geom.segHalfWidth + 0.5f * geom.segLength,
			1 * geom.scaledFactor + geom.segHalfWidth
		)
	}

	private fun drawJ(context: DrawContext, isExecute: Boolean) {
		drawDiagonalWestSegment(
			context.g, isExecute, model.inputValueOf("j"),
			0.5f * geom.scaledFactor + geom.segLength + geom.segHalfWidth,
			geom.scaledFactor + geom.segHalfWidth)
	}

	private fun drawK(context: DrawContext, isExecute: Boolean) {
		drawDiagonalWestSegment(
			context.g, isExecute, model.inputValueOf("k"),
			0.5f * geom.scaledFactor + geom.segHalfWidth + geom.segLength / 2,
			4 * geom.scaledFactor + geom.segHalfWidth
		)
	}

	private fun drawL(context: DrawContext, isExecute: Boolean) {
		drawVerticalSegment(
			context.g, isExecute, model.inputValueOf("l"),
			0.5f * geom.scaledFactor + geom.segHalfWidth + 0.5f * geom.segLength,
			4 * geom.scaledFactor + geom.segHalfWidth)
	}

	private fun drawM(context: DrawContext, isExecute: Boolean) {
		drawDiagonalEastSegment(
			context.g, isExecute, model.inputValueOf("m"),
			0.5f * geom.scaledFactor + geom.segHalfWidth + geom.segLength / 2,
			4 * geom.scaledFactor + geom.segHalfWidth)
	}

	private fun drawDiagonalEastSegment(g: Graphics2D, isExecute: Boolean, value: Boolean, relX: Float, relY: Float) {
		g.translate(relX.toDouble(), relY.toDouble())
		g.color = getColor(value, isExecute)
		g.fill(geom.diagonalEastPath)
		g.translate(-relX.toDouble(), -relY.toDouble())
	}

	private fun drawDiagonalWestSegment(g: Graphics2D, isExecute: Boolean, value: Boolean, relX: Float, relY: Float) {
		g.translate(relX.toDouble(), relY.toDouble())
		g.color = getColor(value, isExecute)
		g.fill(geom.diagonalWestPath)
		g.translate(-relX.toDouble(), -relY.toDouble())
	}

}