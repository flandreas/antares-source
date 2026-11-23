package io.antarescircuit.jabbah.draw.rasterimg

import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.swing.FileSelectionField
import io.antarescircuit.jabbah.draw.Drawable
import io.antarescircuit.jabbah.draw.MainContent
import io.antarescircuit.jabbah.draw.container.DrawableContainerImpl
import io.antarescircuit.jabbah.draw.graphics.Color
import io.antarescircuit.jabbah.draw.module.DrawModuleUiJvm
import java.nio.file.Files
import javax.swing.JButton
import javax.swing.JFormattedTextField
import javax.swing.JLabel
import javax.swing.JTextField
import javax.swing.SwingUtilities
import kotlin.test.*

class ExportRasterImagePanelTest {

	@Test
	fun shouldKeepTheDialogOpenAndShowAnErrorForAnOversizedImage() {
		val directory = Files.createTempDirectory("jabbah-export-panel-")
		try {
			SwingUtilities.invokeAndWait {
				DrawModuleUiJvm.require()
				val previousPath = BaseModule.settings.getString("draw.rasterimg.export.path", "")
				try {
					var closed = false
					val panel = panel { closed = true }
					panel.field<FileSelectionField>("directorySelectionField").path = directory.toString()
					panel.field<JTextField>("fileNameField").text = "oversized.png"
					panel.field<JFormattedTextField>("widthField").text = "50000"
					panel.field<JFormattedTextField>("heightField").text = "50000"
					panel.field<JButton>("okButton").doClick()
					assertFalse(closed)
					assertFalse(Files.exists(directory.resolve("oversized.png")))
					val expected = "<html>" + Translations.getString("draw.image.tooLarge.msg", 50000, 50000)
						.replace("\n", "<br>") + "</html>"
					assertEquals(expected, panel.field<JLabel>("errorLabel").text)
				} finally {
					BaseModule.settings.set("draw.rasterimg.export.path", previousPath)
				}
			}
		} finally {
			Files.delete(directory)
		}
	}

	@Test
	fun shouldRejectDimensionsAboveTheIntegerLimitWithoutWrapping() {
		SwingUtilities.invokeAndWait {
			DrawModuleUiJvm.require()
			for (fieldName in listOf("widthField", "heightField")) {
				for (value in listOf("2147483648", "4294967297", "999999999999999999999")) {
					var closed = false
					val panel = panel { closed = true }
					panel.field<JFormattedTextField>(fieldName).text = value
					panel.field<JButton>("okButton").doClick()
					assertFalse(closed)
					val key = if (fieldName == "widthField") "draw.image.invalidWidth.msg" else "draw.image.invalidHeight.msg"
					assertEquals(Translations.getString(key), panel.field<JLabel>("errorLabel").text)
				}
			}
		}
	}

	private fun panel(closeHandler: () -> Unit) = ExportRasterImagePanel(
		MainContent("Test", DrawableContainerImpl<Drawable>(), Color.WHITE),
		closeHandler = closeHandler,
	)

	private inline fun <reified T> ExportRasterImagePanel.field(name: String): T =
		ExportRasterImagePanel::class.java.getDeclaredField(name).also { it.isAccessible = true }.get(this) as T
}
