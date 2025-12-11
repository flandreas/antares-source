package ch.scorpion.antares.view

import ch.scorpion.antares.view.output.LightColor
import ch.scorpion.antares.view.output.LightColorExpression
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.swing.ColorIcon
import ch.scorpion.jabbah.base.swing.EnumRenderer
import ch.scorpion.jabbah.base.ui.UI
import ch.scorpion.jabbah.draw.graphics.Graphics2DJvm
import java.awt.Color

class LightColorRenderer : EnumRenderer<LightColor>() {

    private val icon = ColorIcon()

    private val systemDefaultIcon: ColorIcon by lazy {
        var color = LightColor.getSystemDefault().executeColor(true)
        if (UI.isDark) {
           color = color.darker()
        }
        ColorIcon(Graphics2DJvm.toAwtColor(color))
    }

    private val colorCache = mutableMapOf<LightColor, Color>()

    private fun getColor(lightColor: LightColor): Color =
        colorCache.computeIfAbsent(lightColor) {
            var color = lightColor.executeColor(true)
            if (UI.isDark) {
                color = color.darker()
            }
            Graphics2DJvm.toAwtColor(color)
        }

    override fun setValue(value: Any?) {
        when (value) {
            null -> {
                text = Translations.getString("element.color.none")
                setIcon(systemDefaultIcon)
            }
            is LightColorExpression -> {
                text = value.toString()
                setIcon(null)
            }
            is LightColor -> {
                icon.backgroundColor = getColor(value)
                text = value.toString()
                setIcon(icon)
            }
            else -> {
                text = value.toString()
                setIcon(null)
            }
        }
    }
}
