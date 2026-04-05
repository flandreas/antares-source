package io.antarescircuit.antares.view.gate

import io.antarescircuit.antares.model.gate.DelayGate
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.antares.view.port.AbstractAntaresPortView.Companion.LENGTH
import io.antarescircuit.jabbah.edit.Look
import io.antarescircuit.jabbah.base.Thousands
import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.draw.style.StyleProvider
import io.antarescircuit.jabbah.draw.style.StyleType

/**
 * A view of a [DelayGate].
 */
class DelayGateView(
    styleProvider: StyleProvider = DrawStyleModule.styleProvider,
    delayGate: DelayGate = DelayGate()
) : BoxGateView<DelayGate>(styleProvider, delayGate.delay.toString(), delayGate) {

    init {
        initExternalLabel(Direction.NORTH)
        customFont = Look.ANNOTATION_FONT
        modelExchanged(null)
    }

    override val relativeExternalLabelLocation: Point2D get() = Point2D(-LENGTH - width / 2, -height / 2 - LABEL_DIST)

    var delay: Long
        get() = model.delay
        set(value) {
	        if (delay != value) {
		        invalidate()
		        model.delay = value
		        updateText()
		        validate()
	        }
        }

	var bitWidth: BitWidth
		get() = model.bitWidth
		set(value) {
			if (value != bitWidth) {
				invalidate()
				model.bitWidth = value
				validate()
			}
		}

    override fun modelExchanged(oldModel: DelayGate?) {
        super.modelExchanged(oldModel)
	    addPortView(createInputPortView(model.getInput()))
	    addPortView(createOutputPortView(model.getOutput()))
	    updateText()
	    updateLayout()
    }

    override fun drawImpl(context: DrawContext) {
        val oldColor = context.g.color
        super.drawImpl(context)

        val sizeHalf = 12
        with(context) {
            if (useContextColors) {
                g.color = color!!.foregroundColor
            } else {
                g.color = foregroundColor
            }
            g.stroke = styleProvider.getStyle(StyleType.ANNOTATION).stroke

            translated(bounds.centerX, 0.0) {
                it.g.drawLine(-sizeHalf, 0, sizeHalf, 0)
                it.g.drawLine(-sizeHalf, -3, -sizeHalf, 3)
                it.g.drawLine(sizeHalf, -3, sizeHalf, 3)
            }

            g.color = oldColor
        }
    }

	private fun updateText() {
		var value = Thousands.convert(delay)
		if (value.length < 5) {
			value += " ns"
		}
		internalLabelText = value
	}
}