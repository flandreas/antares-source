package ch.scorpion.jabbah.app

import ch.scorpion.jabbah.base.Module
import ch.scorpion.jabbah.base.Properties
import kotlin.js.JsExport

/**
 * Defines the environments in which an [Application] runs.
 * The current environment of an [Application] can be determined at start-up as command line arguments.
 * [Modules][Module] can then register corresponding values in [Properties].
 *
 * @property names the possible names to be used as command line argument value
 */
@JsExport
enum class Environment(
	private val names: Set<String>
) {
	Development(setOf("development", "dev")),
	Production(setOf("production", "prod"));

	companion object {
		fun withName(name: String): Environment =
			values().firstOrNull { it.names.contains(name.lowercase()) }
				?: throw IllegalArgumentException("unknown environment $name")
	}
}