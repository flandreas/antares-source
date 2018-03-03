package ch.scorpion.jabbah.draw.view

import ch.scorpion.jabbah.base.event.MouseAdapter
import ch.scorpion.jabbah.base.event.MouseEvent
import ch.scorpion.jabbah.draw.InputEventContext
import ch.scorpion.jabbah.draw.View
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.Graphics2DFx
import javafx.scene.canvas.Canvas
import javafx.scene.layout.*

/**
 * A pane that displays focus ownership, and that activates its [View] in the [ViewManager] when
 * it gets the focus.
 *
 * TODO This class cheats by explicitly specify the focus color of the modena CSS. A better solution would be
 * to develop a special control with a skin implementation that uses the CSS properties. Or a control-like Canvas
 * implementation?
 */
class FocusPaneFx(
	private val view: View<out InputEventContext>,
	private val viewManager: ViewManager = DrawViewModule.viewManager
) : BorderPane() {
	companion object {
		private val focusBorder = Border(BorderStroke(Graphics2DFx.toFxColor(Color(3, 158, 211)), BorderStrokeStyle.SOLID, CornerRadii.EMPTY, BorderStroke.DEFAULT_WIDTHS))
		private val nonFocusBorder = Border(BorderStroke(Graphics2DFx.toFxColor(Color.BLACK), BorderStrokeStyle.NONE, CornerRadii.EMPTY, BorderStroke.DEFAULT_WIDTHS))
	}

	init {
		view.addMouseListener(object : MouseAdapter() {
			override fun mousePressed(e: MouseEvent) {
				view.requestFocus()
			}
		})

		getFxCanvas().focusedProperty().addListener { _ ->
			if (getFxCanvas().isFocused) {
				border = focusBorder
				viewManager.activeView = view
			} else {
				border = nonFocusBorder
			}
		}

		center = getFxCanvas()
		border = nonFocusBorder

		//getFxCanvas().widthProperty().bind(this.widthProperty().subtract(focusBorder.insets.left + focusBorder.insets.right))
		//getFxCanvas().heightProperty().bind(this.heightProperty().subtract(focusBorder.insets.top + focusBorder.insets.bottom))

		//getFxCanvas().widthProperty().addListener { _ -> view.repaint() }
		//getFxCanvas().heightProperty().addListener { _ -> view.repaint() }

		BackgroundInstallerFx(this)
	}

	private fun getFxCanvas(): Canvas = (view.canvas as CanvasFx).canvas
}