package io.antarescircuit.jabbah.base.help

import io.antarescircuit.jabbah.base.logger

/** Returned by [HelpIdProvider] objects as identification for the help system.*/
data class HelpId(val id: String)

/**
 * Identifies help source content, e.g. as part of an URL containing a help page.
 * Different [HelpProvider]s will interpret this differently.
 */
data class HelpSource(val source: String) {
	override fun toString(): String = source
}

/** An object with a [HelpId] for which a user can request help. */
interface HelpIdProvider {
	val helpId: HelpId?
}

/**
 * Maps [HelpId]s to the corresponding [HelpSource].
 * Applications will typically make registrations for their important object when
 * the Application starts. [HelpProvider] will query the [HelpSource] for a given
 * [HelpId] when the user requests help.
 */
object HelpSourceRegistry {

	private val LOG by logger(HelpSourceRegistry::class)

	private val helpIds = mutableMapOf<HelpId, HelpSource>()

	fun register(helpId: HelpId, source: HelpSource) {
		if (helpIds.contains(helpId)) {
			LOG.warn("Replacing existing HelpId ${helpId.id}")
		}
		helpIds[helpId] = source
	}

	fun getHelpSource(helpId: HelpId): HelpSource? = helpIds[helpId]
}

/**
 * Implemented differently on the supported platforms to provide help for a [HelpId].
 * For example, a JVM client implementation could start a local web browser and
 * point it to a web page containing the requested help.
 */
interface HelpProvider {
	fun provideHelpFor(helpId: HelpId?)
}