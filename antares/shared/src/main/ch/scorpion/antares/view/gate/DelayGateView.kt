package ch.scorpion.antares.view.gate

import ch.scorpion.antares.model.gate.DelayGate
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.jabbah.edit.Look
import ch.scorpion.jabbah.base.Thousands
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType

/**
 * A view of a [DelayGate].
 */
class DelayGateView(
    styleProvider: StyleProvider = DrawStyleModule.styleProvider,
    delayGate: DelayGate = DelayGate()
) : BoxGateView<DelayGate>(styleProvider, delayGate.delay.toString(), delayGate) {

    init {
        customFont = Look.ANNOTATION_FONT
        modelExchanged(null)
    }

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

        if (context.useContextColors) {
            context.g.color = context.color!!.foregroundColor
        } else {
            context.g.color = foregroundColor
        }
        context.g.stroke = styleProvider.getStyle(StyleType.ANNOTATION).stroke

        val sizeHalf = 12
        context.translatedAndRotated(bounds.centerX, 0.0, -rotation.angle) {
            it.g.drawLine(-sizeHalf, 0, sizeHalf, 0)
            it.g.drawLine(-sizeHalf, -3, -sizeHalf, 3)
            it.g.drawLine(sizeHalf, -3, sizeHalf, 3)
        }

        context.g.color = oldColor
    }

	private fun updateText() {
		var value = Thousands.convert(delay)
		if (value.length < 5) {
			value += " ns"
		}
		labelText = value
	}
}