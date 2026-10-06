package io.antarescircuit.jabbah.app.tip

import io.antarescircuit.jabbah.base.Settings
import io.antarescircuit.jabbah.base.Properties
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.style.Theme
import io.antarescircuit.jabbah.draw.style.Themes
import kotlin.math.max

/**
 * Provides [TipOfTheDay]s from the application's resources.
 *
 * Example:
 * application.tip.0.title = This is the title of the first tip
 * application.tip.0.content = This is the content of the first tip
 * application.tip.0.image.dark = /img/tip/some-dark.png
 * application.tip.0.image.light = /img/tip/some-light.png
 */
class ResourcesTipProvider(
    val baseName: String ="application.tip",
    private val settings: Settings = BaseModule.settings,
    private val properties: Properties = BaseModule.properties
) : TipOfTheDayProvider {

    companion object {

        /** The name of the [Int] value in [Settings] holding the ID of the last displayed tip.*/
        private const val SETTING_LAST_TIP_ID = "jabbah.app.tip.lastId"

        /** The name of the [Boolean] value in [Properties] indicating whether the tips should be shown on application startup.*/
        const val PROP_TIPS_ON_STARTUP = "jabbah.app.tip.onStartup"

        private const val TITLE_SUFFIX = ".title"
        private const val CONTENT_SUFFIX = ".content"
        private const val IMAGE_SUFFIX = ".image"
        private const val DARK_IMAGE_SUFFIX = ".image.dark"
        private const val LIGHT_IMAGE_SUFFIX = ".image.light"
    }

    private var maxTipId = -1;

    override var showOnStartup: Boolean
        get() = properties.getBoolean(PROP_TIPS_ON_STARTUP)
        set(value) = properties.customize(PROP_TIPS_ON_STARTUP, value)

    override fun resetCurrentId() {
        settings.remove(SETTING_LAST_TIP_ID)
        maxTipId = -1
    }

    override fun hasNextTip(): Boolean =
        hasNextTip(settings.getInt(SETTING_LAST_TIP_ID, -1))

    override fun getNextTip(): TipOfTheDay? =
        getNextTip(settings.getInt(SETTING_LAST_TIP_ID, -1))

    override fun hasNextTip(currentId: Int): Boolean {
        val id = currentId + 1
        return Translations.getOptionalString("$baseName.$id$TITLE_SUFFIX") != null
            && Translations.getOptionalString("$baseName.$id$CONTENT_SUFFIX") != null
    }

    override fun getNextTip(currentId: Int): TipOfTheDay? {
        val id = currentId + 1
        Translations.getOptionalString("$baseName.$id$TITLE_SUFFIX")?.let { title ->
            Translations.getOptionalString("$baseName.$id$CONTENT_SUFFIX")?.let { content ->
                maxTipId = max(maxTipId, id)
                settings.set(SETTING_LAST_TIP_ID, maxTipId)
                return TipOfTheDay(id, title, content, getImagePath(id))
            }
        }
        return null
    }

    private fun getImagePath(id: Int): String? {
        Translations.getOptionalString("$baseName.$id$DARK_IMAGE_SUFFIX")?.let { path ->
            if (Themes.get<Theme>().dark) {
                return path
            }
        }
        Translations.getOptionalString("$baseName.$id$LIGHT_IMAGE_SUFFIX")?.let { path ->
            if (Themes.get<Theme>().light) {
                return path
            }
        }
        return Translations.getOptionalString("$baseName.$id$IMAGE_SUFFIX")
    }
}