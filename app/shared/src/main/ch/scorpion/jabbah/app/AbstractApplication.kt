package ch.scorpion.jabbah.app

import ch.scorpion.jabbah.base.logger

/**
 * Abstract base implementation of the [Application] interface to be used for all target environments.
 */
abstract class AbstractApplication(
	override val controller: ApplicationDataViewController
) : Application {

	companion object {
		private val LOG by lazy { logger(AbstractApplication::class) }
	}

    init {
        configureCustomModules()
    }

    /** ---- [Application] interface */

	override val aboutInfo: AboutInfo get() = AboutInfo(
		iconPath = null,
		name = displayName,
		claim = "A cool application",
		version = ApplicationVersion("0.1.0"),
		disclaimer = "All rights reserved."
	)

	override fun showAboutInfo() {
		LOG.value.info("${aboutInfo.name} Version ${aboutInfo.version}")
	}

    /** ---- [AbstractApplication] */

    /** Called from the constructor to allow subclasses to require custom modules.*/
    private fun configureCustomModules() {
        // empty
    }

    /** Initialisation method that is called after the [AbstractApplication] constructor has been left.*/
    protected open fun init() {
        // empty
    }
}