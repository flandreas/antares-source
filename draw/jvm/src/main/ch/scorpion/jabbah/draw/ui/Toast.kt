package ch.scorpion.jabbah.draw.ui

import ch.scorpion.jabbah.animation.AnimationTask
import ch.scorpion.jabbah.animation.AnimationTaskAdapter
import ch.scorpion.jabbah.animation.Animator
import ch.scorpion.jabbah.base.System
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Frame
import java.awt.Point
import java.awt.event.ComponentAdapter
import java.awt.event.ComponentEvent
import java.awt.geom.RoundRectangle2D
import javax.swing.BorderFactory
import javax.swing.JFrame
import javax.swing.JTextPane
import javax.swing.text.SimpleAttributeSet
import javax.swing.text.StyleConstants

actual object Toast {

	private const val SHOW_DURATION_MS = 2000
	private const val FADE_DURATION_MS = 600.0
	private const val BOTTOM_DIST = 70
	private const val POSITION_CENTER = true

	actual fun show(message: String, animator: Animator) {
		val frame = ToastFrame(message)

		frame.isFocusable = false
		frame.focusableWindowState = false
		frame.isAlwaysOnTop = true
		frame.opacity = 1f
		frame.isVisible = true

		System
			.createTimer()
			.initialize(SHOW_DURATION_MS, repeats = false) {
				val fadeOut = OpacityAnimation.fadeOut(frame, FADE_DURATION_MS).apply {
					addListener(object : AnimationTaskAdapter() {
						override fun ended(task: AnimationTask) {
							frame.isVisible = false
							frame.dispose()
						}
					})
				}
				animator.schedule(fadeOut)
				fadeOut.start()
			}
			.start()
	}

	private class ToastFrame(message: String) : JFrame() {

		companion object {
			private const val SMALL_WIDTH = 150
			private const val LARGE_WIDTH = 250
			private const val INSET = 10
			private const val CORNER_ARC = 10.0
		}

		private val textPane = JTextPane()

		init {
			textPane.contentType = "text/plain"
			textPane.isEditable = false
			textPane.border = BorderFactory.createEmptyBorder(INSET, INSET, INSET, INSET)
			textPane.text = message

			determineSizeAndStyle()

			isUndecorated = true
			isAlwaysOnTop = true

			contentPane.layout = BorderLayout()
			contentPane.add(textPane, BorderLayout.CENTER)

			pack()

			if (POSITION_CENTER) {
				positionAtCenterOfMainFrame()
			} else {
				positionAtCenterBottomOfMainFrame()
			}

			addComponentListener(object : ComponentAdapter() {
				override fun componentResized(e: ComponentEvent?) {
					shape = RoundRectangle2D.Double(0.0, 0.0, width.toDouble(), height.toDouble(), CORNER_ARC, CORNER_ARC)
				}
			})
		}

		private fun determineSizeAndStyle() {
			textPane.size = Dimension(SMALL_WIDTH, Int.MAX_VALUE)
			val smallPreferredHeight = textPane.preferredSize.height

			textPane.size = Dimension(LARGE_WIDTH, Int.MAX_VALUE)
			val largePreferredHeight = textPane.preferredSize.height

			if (smallPreferredHeight == largePreferredHeight) {
				centerText()
				textPane.preferredSize = Dimension(SMALL_WIDTH, textPane.preferredSize.height)

			} else {
				textPane.preferredSize = Dimension(LARGE_WIDTH, textPane.preferredSize.height)
			}
		}

		private fun centerText() {
			val attr = SimpleAttributeSet()
			StyleConstants.setAlignment(attr, StyleConstants.ALIGN_CENTER)
			textPane.styledDocument.setParagraphAttributes(0, textPane.styledDocument.length, attr, false)
		}

		private fun positionAtCenterOfMainFrame() {
			setLocationRelativeTo(Frame.getFrames()[0])
		}

		private fun positionAtCenterBottomOfMainFrame() {
			val mainBounds = Frame.getFrames()[0].bounds
			val toastBounds = bounds

			location = Point(
				mainBounds.x + mainBounds.width / 2 - toastBounds.width / 2,
				mainBounds.y + mainBounds.height - BOTTOM_DIST - toastBounds.height
			)
		}
	}
}