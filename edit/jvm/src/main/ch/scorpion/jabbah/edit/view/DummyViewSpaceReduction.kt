package ch.scorpion.jabbah.edit.view

import ch.scorpion.jabbah.base.event.ActionEvent
import ch.scorpion.jabbah.base.event.PropertyChangeListener
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.PredefinedColorIdentity
import ch.scorpion.jabbah.draw.graphics.PredefinedColorRepository
import ch.scorpion.jabbah.draw.view.ViewSpace
import ch.scorpion.jabbah.draw.view.AbstractViewAction
import ch.scorpion.jabbah.edit.model.rectangle.RectangleComponent

class DummyViewSpaceReductionAction : AbstractViewAction("view.action.dummyViewSpaceReduction") {
	override fun execute(event: ActionEvent) {
		view?.let {
			if (DummyViewSpaceReduction.showed) {
				DummyViewSpaceReduction.hide(it)
			} else {
				DummyViewSpaceReduction.show(it)
			}
		}
		selected = DummyViewSpaceReduction.showed
	}
}

/** Used for testing and experimenting with [ViewSpace] reductions.*/
object DummyViewSpaceReduction {

	private const val HEIGHT = 200
	private var block = ReductionView()
	private val reductionColor = PredefinedColorRepository.withIdentity(PredefinedColorIdentity.Violet)!!.color.withAlpha(128)
	private var view: View<*>? = null

	var showed: Boolean = false

	private val canvasSizeListener = PropertyChangeListener<Any> {
		view?.let {
			updateBlock(it.width.toDouble())
		}
	}

	fun show(view: View<*>) {
		this.view = view
		view.canvas.addPropertyChangeListener(canvasSizeListener)
		updateBlock(view.width.toDouble())
		view.overlayContainer.add(block)
		view.overlayContainer.validate()
		view.space.reduceTop(HEIGHT)
		showed = true
	}

	fun hide(view: View<*>) {
		this.view = null
		view.canvas.removePropertyChangeListener(canvasSizeListener)
		view.overlayContainer.remove(block)
		view.overlayContainer.validate()
		view.space.removeTopReduction(HEIGHT)
		showed = false
	}

	private fun updateBlock(width: Double) {
		block.setFrame(0.0, 0.0, width, HEIGHT.toDouble())
	}

	private class ReductionView(shape: Rectangle2D = Rectangle2D.ZERO) : RectangleComponent(shape = shape) {
		override fun drawShape(context: DrawContext, strokeColor: Color?, fillColor: Color?) {
			drawFill(context, shapeToDraw, reductionColor.backgroundColor)
		}
	}
}