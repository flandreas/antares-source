package ch.scorpion.jabbah.draw.richtext

import ch.scorpion.jabbah.base.module.BaseModuleJvm
import ch.scorpion.jabbah.draw.drawable.RichTextDrawable
import ch.scorpion.jabbah.draw.graphics.FontImpl
import ch.scorpion.jabbah.draw.module.DrawModule
import java.awt.Dimension
import java.awt.FlowLayout
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import javax.swing.ImageIcon
import javax.swing.JFrame
import kotlin.system.exitProcess

class RichTextLabelTest : JFrame() {

	companion object {

		@JvmStatic fun main(args: Array<String>) {
			BaseModuleJvm.require()
			DrawModule.require()

			val frame = RichTextLabelTest()

			frame.addWindowListener(object : WindowAdapter() {
				override fun windowClosing(e: WindowEvent?) {
					exitProcess(0)
				}
			})

			frame.title = "RichTextLabel"
			frame.size = Dimension(800, 600)
			frame.setLocationRelativeTo(null)
			frame.isVisible = true
		}
	}

	init {
		contentPane.layout = FlowLayout(FlowLayout.LEFT, 0, 0)

		val font = FontImpl()

		contentPane.add(
			RichTextLabel().apply {
				icon = ImageIcon(RichTextLabelTest::class.java.getResource("/img/and.png"))
				richText = RichTextDrawable.of("This_1 is !(red)", font)
			}
		)
		contentPane.add(
			RichTextLabel().apply {
				icon = ImageIcon(RichTextLabelTest::class.java.getResource("/img/or.png"))
				richText = RichTextDrawable.of("This_(12) is !(green)", font)
			}
		)

		contentPane.add(
			RichTextLabel().apply {
				richText = RichTextDrawable.of("This_(12) is !(yellow)", font)
			}
		)

		contentPane.add(
			RichTextLabel().apply {
				icon = ImageIcon(RichTextLabelTest::class.java.getResource("/img/not.png"))
			}
		)
	}
}