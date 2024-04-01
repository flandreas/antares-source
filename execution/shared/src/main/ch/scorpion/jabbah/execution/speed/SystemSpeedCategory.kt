package ch.scorpion.jabbah.execution.speed

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.time.SystemSpeed
import ch.scorpion.jabbah.base.time.SystemSpeedEvent
import ch.scorpion.jabbah.base.logger

/**
 * Defines categories of [SystemSpeed] that distinguish the depth of interest of a user when
 * interacting with the system.
 */
enum class SystemSpeedCategory(val customName: String, val speedRange: IntRange) {

    /**
     * When using the system, the user is only interested in the outcome of using the system,
     * e.g. in the results that are produced by the system. Although he'd expect to see the results being produced
     * slower if he reduces the [SystemSpeed], the internals of the system could still run as fast as possible
     */
    Use("use", 67..100),

    /**
     * When observing the system, the user is not only interested in the outcome of using the system,
     * but also in how these results are being produced, at least to some degree. At this [SystemSpeedCategory],
     * the system displays intermediate results and provides some visual insights of how information flows across
     * the system.
     */
    Observe("observer", 34..66),

    /**
     * When exploring the system, the user expects from the system to explain how it works in every possible
     * detail, including using intermediate animations that can further slow down the system speed.
     */
    Explore("explore", 0..33);

	companion object {
		fun withName(customName: String): SystemSpeedCategory {
			return values().firstOrNull { it.customName == customName } ?: throw IllegalArgumentException("Unknown SystemSpeedCategory")
		}
	}

    override fun toString(): String {
        return when(this) {
            Use -> Translations.getString("execution.systemSpeedCategory.use")
            Observe -> Translations.getString("execution.systemSpeedCategory.observe")
            Explore -> Translations.getString("execution.systemSpeedCategory.explore")
        }
    }
}

/**
 * Represents the current [SystemSpeedCategory] depending on the current [SystemSpeed].
 * Listens for changes of [SystemSpeed], updates its current [SystemSpeedCategory], and posts
 * a [SystemSpeedCategoryEvent] on its [EventBus] if the value has changed.
 */
class CurrentSystemSpeedCategory(
    val systemSpeed: SystemSpeed,
    private val eventBus: EventBus = BaseModule.eventBus
) {

	private val systemSpeedHandler: EventHandler<SystemSpeedEvent> = {
		if (it.source === systemSpeed) {
			update()
		}
	}

    init {
        eventBus.register(SystemSpeedEvent::class, systemSpeedHandler)
    }

	fun dispose() {
		eventBus.unregister(systemSpeedHandler)
	}

	companion object {
        private val LOG by logger(CurrentSystemSpeedCategory::class)
	}

    var systemSpeedCategory: SystemSpeedCategory = calculate()
		private set

    private fun update() {
        val oldValue = systemSpeedCategory
        systemSpeedCategory = calculate()
        if (oldValue != systemSpeedCategory) {
            LOG.trace("CurrentSystemSpeedCategory changed to '$systemSpeedCategory'")
            eventBus.post(SystemSpeedCategoryEvent(this, oldValue, systemSpeedCategory))
        }
    }

    private fun calculate(): SystemSpeedCategory =
	    SystemSpeedCategory.entries.first { systemSpeed.speed >= it.speedRange.first }
}

/** Posted by [CurrentSystemSpeedCategory] when the current [SystemSpeedCategory] has changed.*/
data class SystemSpeedCategoryEvent(
    val source: CurrentSystemSpeedCategory,
    val oldValue: SystemSpeedCategory,
    val newValue: SystemSpeedCategory
)