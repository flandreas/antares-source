package io.antarescircuit.antares.view

import io.antarescircuit.antares.view.output.LightColor
import io.antarescircuit.antares.view.output.LightColorExpression
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.swing.ColorIcon
import io.antarescircuit.jabbah.base.swing.EnumRenderer
import io.antarescircuit.jabbah.base.ui.UI
import io.antarescircuit.jabbah.draw.graphics.Graphics2DJvm
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
