package ch.scorpion.jabbah.draw.view

import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.draw.style.StyleType
import ch.scorpion.jabbah.draw.style.ThemeEvent
import javafx.geometry.Insets
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.layout.CornerRadii
import javafx.scene.layout.Region
import javafx.scene.paint.Color

/**
 * Listens for [ThemeEvent]s and installs the current background color in the specified [Region].
 */
class BackgroundInstallerFx(
	private val region: Region,
	private val styleProvider: StyleProvider = DrawStyleModule.styleProvider
) {

	init {
		BaseModule.eventBus.register(ThemeEvent::class, { installBackgroundColor() })
		installBackgroundColor()
	}

	private fun installBackgroundColor() {
		val color = styleProvider.getStyle(StyleType.BACKGROUND).color.backgroundColor
		region.background = Background(BackgroundFill(Color.rgb(color.red, color.green, color.blue), CornerRadii.EMPTY, Insets.EMPTY))
	}
}