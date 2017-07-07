package ch.scorpion.jabbah.base.module

import ch.scorpion.jabbah.base.*

/**
 * Setup of the [ch.scorpion.jabbah.base] module for the JavaScript target.
 */
object BaseModuleJs : AbstractModule() {

    override fun initialize() {
        System.SYSTEM = SystemJs()
        Math = MathJs()
        LOG_SYSTEM = LogSystemJs()
        Translations = TranslationsJs()

        BaseModule.require()
    }
}