package ch.scorpion.antares.view.gate

import ch.scorpion.antares.model.PortCount
import ch.scorpion.antares.model.gate.effectiveGateInputBit
import ch.scorpion.antares.model.port.DigitalPort
import ch.scorpion.antares.model.signal.Bit
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.view.OrientableRectangularVerticeView
import ch.scorpion.antares.view.Handedness
import ch.scorpion.antares.view.Look
import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.base.geom.AffineTransform
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.drawable.Transparent
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.FontImpl
import ch.scorpion.jabbah.draw.graphics.Stroke
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.execution.speed.SystemSpeedCategory
import ch.scorpion.jabbah.graph.GraphApplicationContext
import ch.scorpion.jabbah.graph.app.ApplicationMode

class GateMnemonicsEvent

/**
 * Draws a mnemonic for digital gate views using a switch representation for the logical function
 * which the model of the digital gate view implements.
 */
object GateMnemonic {

	private const val PROP_ENABLED = "ch.scorpion.antares.view.GateMnemonics"
	private val FONT = FontImpl(size = 8)
	private val LINE_STROKE = Stroke(width = 0.5f)
	private val SWITCH_STROKE = Stroke(width = 1.5f)
	private const val ZOOM_LIMIT = 1.6

	var enabled: Boolean = BaseModule.settings.getString(PROP_ENABLED, "false") == "true"
		set(value) {
			if (field != value) {
				field = value
				BaseModule.settings.set(PROP_ENABLED, field)
				BaseModule.eventBus.post(GateMnemonicsEvent())
			}
		}

	/**
	 * Determines whether [GateMnemonic] should be displayed for the current scale factors in the specified [AffineTransform].
	 * Returns `true` only if the scale factors are above a certain limit. Returns always `false` if not [enabled].
	 */
	private fun isDisplayableFor(transform: AffineTransform): Boolean = enabled && transform.uniformScale >= ZOOM_LIMIT

	private fun isDisplayableFor(mode: ApplicationMode, systemSpeedCategory: SystemSpeedCategory): Boolean {
		return mode == ApplicationMode.EDIT || systemSpeedCategory >= SystemSpeedCategory.Observe
	}

	fun drawAnd(gateView: AndGateView, context: DrawContext, foreground: Color, background: Color) {
		if (!begin(gateView, context)) {
			return
		}
		drawSerial(gateView, context, foreground, background, invert1 = false, invert2 = false)
		end(gateView, context)
	}

	fun drawNand(gateView: NandGateView, context: DrawContext, foreground: Color, background: Color) {
		if (!begin(gateView, context)) {
			return
		}
		drawParallel(gateView, context, foreground, background, invert1 = true, invert2 = true, inputOffsetX = 0)
		end(gateView, context)
	}

	fun drawOr(gateView: OrGateView, context: DrawContext, foreground: Color, background: Color, inputOffsetX: Int) {
		if (!begin(gateView, context)) {
			return
		}
		drawParallel(gateView, context, foreground, background, invert1 = false, invert2 = false, inputOffsetX = inputOffsetX)
		end(gateView, context)
	}

	fun drawNor(gateView: NorGateView, context: DrawContext, foreground: Color, background: Color) {
		if (!begin(gateView, context)) {
			return
		}
		drawSerial(gateView, context, foreground, background, invert1 = true, invert2 = true)
		end(gateView, context)
	}

	fun drawXor(gateView: XorGateView, context: DrawContext, foreground: Color, background: Color, inputOffsetX: Int) {
		if (!begin(gateView, context)) {
			return
		}
		drawParallelTwice(gateView, context, foreground, background, invert = false, inputOffsetX = inputOffsetX)
		end(gateView, context)
	}

	fun drawXnor(gateView: XnorGateView, context: DrawContext, foreground: Color, background: Color, inputOffsetX: Int) {
		if (!begin(gateView, context)) {
			return
		}
		drawParallelTwice(gateView, context, foreground, background, invert = true, inputOffsetX = inputOffsetX)
		end(gateView, context)
	}

	fun drawNot(gateView: NotGateView, context: DrawContext, foreground: Color, background: Color) {
		if (!begin(gateView, context)) {
			return
		}
		drawInverter(gateView, context, foreground, background)
		end(gateView, context)
	}

	fun drawBuffer(gateView: BufferGateView, context: DrawContext, foreground: Color) {
		if (!begin(gateView, context)) {
			return
		}
		drawBufferImpl(gateView, context, foreground)
		end(gateView, context)
	}

	fun drawTriStateBuffer(gateView: TriStateBufferGateView, context: DrawContext, foreground: Color) {
		if (!begin(gateView, context)) {
			return
		}
		when (gateView.handedness) {
			Handedness.RIGHT -> drawTriStateRight(gateView, context, foreground)
			Handedness.LEFT -> drawTriStateLeft(gateView, context, foreground)
		}
		end(gateView, context)
	}

	private fun transparent(transparent: Transparent, color: Color): Color {
		return Transparent.applyTo(transparent.transparency, color)
	}

	private fun getInputSignal(gateView: OrientableRectangularVerticeView<*>, portId: Int): Bit {
		val inputPort = gateView.model.getInput<DigitalSignal>(portId) as DigitalPort
		return effectiveGateInputBit(inputPort.logic.evaluate(inputPort.getIncomingSignal()!!.bitAt(0)))
	}

	private fun drawSerial(gateView: AbstractDigitalGateView<*>, context: DrawContext, foreground: Color, background: Color, invert1: Boolean, invert2: Boolean) {
		val passive = gateView.bitWidth.width > 1
		val isExec = context.castedAppContext<GraphApplicationContext>()!!.isExecute && !passive

		val signal1 = if (passive) Bit.False else getInputSignal(gateView, 1)
		val signal2 = if (passive) Bit.False else getInputSignal(gateView, 2)
		val signalOut = if (passive) Bit.False else gateView.model.getOutput<DigitalSignal>().getOutgoingSignal()!!.bitAt(0)

		// Internal connection
		context.g.font = FONT
		context.g.stroke = LINE_STROKE
		// Segment 1
		context.g.color = transparent(gateView, if (isExec) Themes.get<AntaresTheme>().one.foregroundColor else foreground)
		context.g.drawLine(s(1.0), s(4.0), s(1.5), s(4.0))
		drawSource(gateView, context, isExec, foreground, background)
		// Segment 2
		context.g.color = transparent(gateView, if (isExec) signal1.invert(invert1).color.foregroundColor else foreground)
		context.g.drawLine(s(2.5), s(4.0), s(3.0), s(4.0))
		// Segment 3
		context.g.color = transparent(gateView, if (isExec) signalOut.color.foregroundColor else foreground)
		context.g.drawLine(s(4.0), s(4.0), s(6.0), s(4.0))

		val y1 = if (invert1) {
			if (signal1.isSet) s(4.5) else s(4.0)
		} else {
			if (signal1.isSet) s(4.0) else s(3.5)
		}
		val y2 = if (invert2) {
			if (signal2.isSet) s(3.5) else s(4.0)
		} else {
			if (signal2.isSet) s(4.0) else s(4.5)
		}

		val portX = gateView.getPortViews()[0].locationX.toInt() - gateView.x

		val color1 = if (isExec) signal1.invert(invert1).color.foregroundColor else foreground
		val color2 = if (isExec) signalOut.color.foregroundColor else foreground

		// Input 1
		context.g.stroke = LINE_STROKE
		context.g.color = transparent(gateView, if (isExec) signal1.color.foregroundColor else foreground)
		context.g.drawLine(portX, s(2.0), s(2.0), s(2.0))
		context.g.drawLine(s(2.0), s(2.0), s(2.0), y1)
		context.g.stroke = SWITCH_STROKE
		context.g.color = transparent(gateView, color1)
		context.g.drawLine(s(1.5) + 1, y1, s(2.5) - 1, y1)

		// Input 2
		context.g.stroke = LINE_STROKE
		context.g.color = transparent(gateView, if (isExec) signal2.color.foregroundColor else foreground)
		context.g.drawLine(portX, s(6.0), s(3.5), s(6.0))
		context.g.drawLine(s(3.5), s(6.0), s(3.5), y2)
		context.g.stroke = SWITCH_STROKE
		context.g.color = transparent(gateView, color2)
		context.g.drawLine(s(3.0) + 1, y2, s(4.0) - 1, y2)
	}

	private fun drawParallelTwice(gateView: AbstractDigitalGateView<*>, context: DrawContext, foreground: Color, background: Color, invert: Boolean, inputOffsetX: Int) {
		val passive = gateView.bitWidth.width > 1
		val isExec = context.castedAppContext<GraphApplicationContext>()!!.isExecute && !passive

		val signal1 = if (passive) Bit.False else getInputSignal(gateView, 1)
		val signal2 = if (passive) Bit.False else getInputSignal(gateView, 2)
		val signalOut = if (passive) Bit.False else gateView.model.getOutput<DigitalSignal>().getOutgoingSignal()!!.bitAt(0)

		val yu = s(3.0)
		val yl = s(5.0)

		// Internal connection
		context.g.font = FONT
		context.g.stroke = LINE_STROKE
		// Segment 1
		context.g.color = transparent(gateView, if (isExec) Themes.get<AntaresTheme>().one.foregroundColor else foreground)
		context.g.drawLine(s(1.0), s(4.0), s(1.0), yu)
		context.g.drawLine(s(1.0), yu, s(1.5), yu)
		context.g.drawLine(s(1.0), s(4.0), s(1.0), yl)
		context.g.drawLine(s(1.0), yl, s(1.5), yl)
		drawSource(gateView, context, isExec, foreground, background)
		// Segment 2.upper
		context.g.color = transparent(gateView, if (isExec) signal1.not().color.foregroundColor else foreground)
		context.g.drawLine(s(2.5), yu, s(3.0), yu)
		// Segment 2.lower
		context.g.color = transparent(gateView, if (isExec) signal1.color.foregroundColor else foreground)
		context.g.drawLine(s(2.5), yl, s(3.0), yl)
		// Segment 3.upper
		context.g.color = transparent(gateView, if (isExec) Bit.of(!signal1.isSet && signal2.invert(invert).isSet).color.foregroundColor else foreground)
		context.g.drawLine(s(4.0), yu, s(4.5), yu)
		context.g.drawLine(s(4.5), yu, s(4.5), s(4.0))
		// Segment 3.lower
		context.g.color = transparent(gateView, if (isExec) Bit.of(signal1.isSet && !signal2.invert(invert).isSet).color.foregroundColor else foreground)
		context.g.drawLine(s(4.0), yl, s(4.5), yl)
		context.g.drawLine(s(4.5), yl, s(4.5), s(4.0))
		// Segment 4
		context.g.color = transparent(gateView, if (isExec) signalOut.color.foregroundColor else foreground)
		context.g.drawLine(s(4.5), s(4.0), s(6.0), s(4.0))

		val portX = (gateView.getPortViews()[0].locationX.toInt() + inputOffsetX - gateView.x).toInt()

		// Input 1
		val y1u = if (signal1.isSet) yu + s(0.5) else yu
		val y1l = if (signal1.isSet) yl else yl - s(0.5)

		context.g.stroke = LINE_STROKE
		context.g.color = transparent(gateView, if (isExec) signal1.color.foregroundColor else foreground)
		context.g.drawLine(portX.toDouble(), s(2.0), s(2.0), s(2.0))
		context.g.drawLine(s(2.0), s(2.0), s(2.0), y1l)
		context.g.stroke = SWITCH_STROKE
		context.g.color = transparent(gateView, if (isExec) signal1.not().color.foregroundColor else foreground)
		context.g.drawLine(s(1.5) + 1, y1u, s(2.5) - 1, y1u)
		context.g.color = transparent(gateView, if (isExec) signal1.color.foregroundColor else foreground)
		context.g.drawLine(s(1.5) + 1, y1l, s(2.5) - 1, y1l)

		// Input 2
		val y2l = if (invert) {
			if (signal2.isSet) yl else yl - s(0.5)
		} else {
			if (signal2.isSet) yl - s(0.5) else yl
		}
		val y2u = if (invert) {
			if (signal2.isSet) yu + s(0.5) else yu
		} else {
			if (signal2.isSet) yu else yu + s(0.5)
		}

		context.g.stroke = LINE_STROKE
		context.g.color = transparent(gateView, if (isExec) signal2.color.foregroundColor else foreground)
		if (invert) {
			context.g.drawLine(portX.toDouble(), s(6.0), s(0.5), s(6.0))
			context.g.drawLine(s(0.5), s(6.0), s(0.5), s(2.5))
			context.g.drawLine(s(0.5), s(2.5), s(3.5), s(2.5))
			context.g.drawLine(s(3.5), s(2.5), s(3.5), y2l)
		} else {
			context.g.drawLine(portX.toDouble(), s(6.0), s(3.5) + 1, s(6.0))
			context.g.drawLine(s(3.5) + 1, s(6.0), s(3.5) + 1, y2u)
		}
		context.g.stroke = SWITCH_STROKE
		context.g.color = transparent(gateView, if (isExec) Bit.of(signal1.isSet && !signal2.invert(invert).isSet).color.foregroundColor else foreground)
		context.g.drawLine(s(3.0) + 1, y2l, s(4.0) - 1, y2l)
		context.g.color = transparent(gateView, if (isExec) Bit.of(!signal1.isSet && signal2.invert(invert).isSet).color.foregroundColor else foreground)
		context.g.drawLine(s(3.0) + 1, y2u, s(4.0) - 1, y2u)
	}

	private fun drawParallel(gateView: AbstractDigitalGateView<*>, context: DrawContext, foreground: Color, background: Color, invert1: Boolean, invert2: Boolean, inputOffsetX: Int) {
		val passive = gateView.bitWidth.width > 1
		val isExec = context.castedAppContext<GraphApplicationContext>()!!.isExecute && !passive

		val signal1 = if (passive) Bit.False else getInputSignal(gateView, 1)
		val signal2 = if (passive) Bit.False else getInputSignal(gateView, 2)
		val signalOut = if (passive) Bit.False else gateView.model.getOutput<DigitalSignal>().getOutgoingSignal()!!.bitAt(0)

		val color1 = if (isExec) signal1.invert(invert1).color.foregroundColor else foreground
		val color2 = if (isExec) signal2.invert(invert2).color.foregroundColor else foreground

		val yu = s(3.0)
		val yl = s(5.0)

		// Internal connection
		context.g.font = FONT
		context.g.stroke = LINE_STROKE
		// Segment 1
		context.g.color = transparent(gateView, if (isExec) Themes.get<AntaresTheme>().one.foregroundColor else foreground)
		context.g.drawLine(s(1.0), s(4.0), s(1.0), yu)
		context.g.drawLine(s(1.0), yu, s(2.25), yu)
		context.g.drawLine(s(1.0), s(4.0), s(1.0), yl)
		context.g.drawLine(s(1.0), yl, s(2.25), yl)
		drawSource(gateView, context, isExec, foreground, background)
		// Segment 2.1
		context.g.color = transparent(gateView, if (isExec) signal1.invert(invert1).color.foregroundColor else foreground)
		context.g.drawLine(s(3.25), yu, s(4.5), yu)
		context.g.drawLine(s(4.5), yu, s(4.5), s(4.0))
		// Segment 2.2
		context.g.color = transparent(gateView, if (isExec) signal2.invert(invert2).color.foregroundColor else foreground)
		context.g.drawLine(s(3.25), yl, s(4.5), yl)
		context.g.drawLine(s(4.5), yl, s(4.5), s(4.0))
		// Segment 2.out
		context.g.color = transparent(gateView, if (isExec) signalOut.color.foregroundColor else foreground)
		context.g.drawLine(s(4.5), s(4.0), s(6.0), s(4.0))

		val portX = (gateView.getPortViews()[0].locationX.toInt() + inputOffsetX - gateView.x)
		val y1 = if (invert1) {
			if (signal1.isSet) yu + s(0.5) else yu
		} else {
			if (signal1.isSet) yu else yu - s(0.5)
		}

		val y2 = if (invert2) {
			if (signal2.isSet) yl - s(0.5) else yl
		} else {
			if (signal2.isSet) yl else yl + s(0.5)
		}

		// Input 1
		context.g.stroke = LINE_STROKE
		context.g.color = transparent(gateView, if (isExec) signal1.color.foregroundColor else foreground)
		context.g.drawLine(portX, s(2.0), s(2.75), s(2.0))
		context.g.drawLine(s(2.75), s(2.0), s(2.75), y1)
		context.g.stroke = SWITCH_STROKE
		context.g.color = transparent(gateView, color1)
		context.g.drawLine(s(2.25) + 1, y1, s(3.25) - 1, y1)

		// Input 2
		context.g.stroke = LINE_STROKE
		context.g.color = transparent(gateView, if (isExec) signal2.color.foregroundColor else foreground)
		context.g.drawLine(portX, s(6.0), s(2.75), s(6.0))
		context.g.drawLine(s(2.75), s(6.0), s(2.75), y2)
		context.g.stroke = SWITCH_STROKE
		context.g.color = transparent(gateView, color2)
		context.g.drawLine(s(2.25) + 1, y2, s(3.25) - 1, y2)
	}

	private fun drawInverter(gateView: NotGateView, context: DrawContext, foreground: Color, background: Color) {
		val passive = gateView.bitWidth.width > 1
		val isExec = context.castedAppContext<GraphApplicationContext>()!!.isExecute && !passive

		val signalOut = if (passive) Bit.False else gateView.model.getOutput<DigitalSignal>().getOutgoingSignal()!!.bitAt(0)
		val signal = if (passive) Bit.True else signalOut.not()

		val yu = s(3.0)

		// Internal connection
		context.g.font = FONT
		context.g.stroke = LINE_STROKE
		// Segment 1
		context.g.color = transparent(gateView, if (isExec) Themes.get<AntaresTheme>().one.foregroundColor else foreground)
		context.g.drawLine(s(1.0), s(4.0), s(2.25), s(4.0))
		drawSource(gateView, context, isExec, foreground, background)
		// Segment 2
		context.g.color = transparent(gateView, if (isExec) signalOut.color.foregroundColor else foreground)
		context.g.drawLine(s(3.25), s(4.0), s(6.0), s(4.0))

		val portX = (gateView.getPortViews()[0].locationX.toInt() - gateView.x)
		val y = if (signal.isSet) s(4.5) else s(4.0)

		// Input
		context.g.stroke = LINE_STROKE
		context.g.color = transparent(gateView, if (isExec) signal.color.foregroundColor else foreground)
		context.g.drawLine(portX, s(4.0), s(0.5), s(4.0))
		context.g.drawLine(s(0.5), s(4.0), s(0.5), yu)
		context.g.drawLine(s(0.5), yu, s(2.75), yu)
		context.g.drawLine(s(2.75), yu, s(2.75), y)
		context.g.stroke = SWITCH_STROKE
		context.g.color = transparent(gateView, if (isExec) signalOut.color.foregroundColor else foreground)
		context.g.drawLine(s(2.25) + 1, y, s(3.25) - 1, y)
	}

	private fun drawBufferImpl(gateView: BufferGateView, context: DrawContext, foreground: Color) {
		val passive = gateView.bitWidth.width > 1
		val isExec = context.castedAppContext<GraphApplicationContext>()!!.isExecute && !passive

		val signalOut = if (passive) Bit.False else gateView.model.getOutput<DigitalSignal>().getOutgoingSignal()!!.bitAt(0)
		val portX = (gateView.getPortViews()[0].locationX.toInt() - gateView.x)
		context.g.stroke = LINE_STROKE
		context.g.color = transparent(gateView, if (isExec) signalOut.color.foregroundColor else foreground)
		context.g.drawLine(portX, s(4.0), s(6.0), s(4.0))
	}

	private fun drawTriStateRight(gateView: TriStateBufferGateView, context: DrawContext, foreground: Color) {
		val passive = gateView.bitWidth.width > 1
		val isExec = context.castedAppContext<GraphApplicationContext>()!!.isExecute && !passive

		val signal = if (passive) Bit.False else gateView.model.getInput<DigitalSignal>(1).getIncomingSignal()!!.bitAt(0)
		val control = if (passive) Bit.False else getInputSignal(gateView, 2)
		val signalOut = if (passive) Bit.Undefined else gateView.model.getOutput<DigitalSignal>().getOutgoingSignal()!!.bitAt(0)

		// Internal connection
		context.g.font = FONT
		context.g.stroke = LINE_STROKE
		// Segment 'undefined'
		context.g.color = transparent(gateView, if (isExec) Themes.get<AntaresTheme>().undefined.foregroundColor else foreground)
		context.g.drawLine(s(1.0), s(4.0), s(2.5), s(4.0))
		context.g.fillOval(s(0.75), s(3.75), s(0.5) + 1, s(0.5) + 1)
		context.g.color = transparent(gateView, if (isExec) Themes.get<AntaresTheme>().undefined.backgroundColor else foreground)
		context.g.drawOval(s(0.75), s(3.75), s(0.5) + 1, s(0.5) + 1)
		// Segment 'signal'
		context.g.color = transparent(gateView, if (isExec) signal.color.foregroundColor else foreground)
		context.g.drawLine(0.0, s(3.0), s(2.5), s(3.0))
		// Segment 'control'
		context.g.color = transparent(gateView, if (isExec) control.color.foregroundColor else foreground)
		context.g.drawLine(s(3.0), s(4.5), s(3.0), if (control.isSet) s(3.0) else s(3.5))
		// Segment 'output'
		context.g.color = transparent(gateView, if (isExec) signalOut.color.foregroundColor else foreground)
		context.g.drawLine(s(3.5), s(3.0), s(6.0), s(3.0))

		// Switch
		context.g.stroke = SWITCH_STROKE
		context.g.drawLine(s(2.5), if (control.isSet) s(3.0) else s(4.0), s(3.5), s(3.0))
	}

	private fun drawTriStateLeft(gateView: TriStateBufferGateView, context: DrawContext, foreground: Color) {
		val passive = gateView.bitWidth.width > 1
		val isExec = context.castedAppContext<GraphApplicationContext>()!!.isExecute && !passive

		val signal = if (passive) Bit.False else getInputSignal(gateView, 1)
		val control = if (passive) Bit.False else getInputSignal(gateView, 2)
		val signalOut = if (passive) Bit.Undefined else gateView.model.getOutput<DigitalSignal>().getOutgoingSignal()!!.bitAt(0)

		// Internal connection
		context.g.font = FONT
		context.g.stroke = LINE_STROKE
		// Segment 'undefined'
		context.g.color = transparent(gateView, if (isExec) Themes.get<AntaresTheme>().undefined.foregroundColor else foreground)
		context.g.drawLine(s(1.0), s(2.0), s(2.5), s(2.0))
		context.g.fillOval(s(0.75), s(1.75), s(0.5) + 1, s(0.5) + 1)
		context.g.color = transparent(gateView, if (isExec) Themes.get<AntaresTheme>().undefined.backgroundColor else foreground)
		context.g.drawOval(s(0.75), s(1.75), s(0.5) + 1, s(0.5) + 1)
		// Segment 'signal'
		context.g.color = transparent(gateView, if (isExec) signal.color.foregroundColor else foreground)
		context.g.drawLine(0.0, s(3.0), s(2.5), s(3.0))
		// Segment 'control'
		context.g.color = transparent(gateView, if (isExec) control.color.foregroundColor else foreground)
		context.g.drawLine(s(3.0), s(1.5), s(3.0), if (control.isSet) s(3.0) else s(2.5))
		// Segment 'output'
		context.g.color = transparent(gateView, if (isExec) signalOut.color.foregroundColor else foreground)
		context.g.drawLine(s(3.5), s(3.0), s(6.0), s(3.0))

		// Switch
		context.g.stroke = SWITCH_STROKE
		context.g.drawLine(s(2.5), if (control.isSet) s(3.0) else s(2.0), s(3.5), s(3.0))
	}

	private fun drawSource(transparent: Transparent, context: DrawContext, isExec: Boolean, foreground: Color, background: Color) {
		context.g.color = transparent(transparent, if (isExec) Themes.get<AntaresTheme>().one.foregroundColor else background)
		context.g.fillCircle(s(1.0), s(4.0), s(0.25))
		context.g.color = transparent(transparent, if (isExec) Themes.get<AntaresTheme>().one.backgroundColor else foreground)
		context.g.drawCircle(s(1.0), s(4.0), s(0.25))
	}

	/**
	 * Determines whether gate mnemonics have to be drawn (depending on the [PortCount], the zoom level,
	 * the [ApplicationMode]) and the general enabledness, and prepares drawing by setting up the
	 * coordinate system origin if drawing is required.
	 */
	private fun begin(gateView: OrientableRectangularVerticeView<*>, context: DrawContext): Boolean {
		val graphApplicationContext = context.castedAppContext<GraphApplicationContext>()!!
		if (gateView.model.inputCount <= 2
			&& isDisplayableFor(context.g.transform)
			&& isDisplayableFor(graphApplicationContext.mode, graphApplicationContext.systemSpeedCategory.systemSpeedCategory)
		) {
			if (gateView is BoxGateView<*>) {
				gateView.labelStyle = BoxGateView.LabelStyle.SMALL_UPPER_LEFT
			}
			context.g.translate(gateView.x, gateView.y)
			return true
		}
		if (gateView is BoxGateView<*>) {
			// TODO Remember old labelStyle and re-establish in [end]
			gateView.labelStyle = BoxGateView.LabelStyle.LARGE_CENTERED
		}
		return false
	}

	private fun end(gateView: OrientableRectangularVerticeView<*>, context: DrawContext) {
		context.g.translate(-gateView.x, -gateView.y)
	}

	private fun s(d: Double): Double = d * Look.SCALE
}