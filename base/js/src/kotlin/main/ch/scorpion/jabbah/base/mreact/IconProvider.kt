package ch.scorpion.jabbah.base.mreact

import react.ReactElement

typealias IconProvider = () -> ReactElement

/**
 * Allows to register [IconProvider]s that provide icons of particular name as [ReactElement].
 * The name of the icons is typically the name of PNGs used in the desktop client, but mapped
 * by this class to logic that delivers the corresponding SVG React element.
 */
object IconProviderRegistry {

	private val providers = mutableMapOf<String, IconProvider>()

	fun register(name: String, provider: IconProvider) {
		providers[name] = provider
	}

	fun getIcon(name: String): ReactElement? = providers[name]?.invoke()
}