package ch.scorpion.jabbah.execution.speed

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.time.SystemSpeed
import ch.scorpion.jabbah.base.time.SystemSpeedEvent
import ch.scorpion.jabbah.base.logger

/**
 * Defines categories of [SystemSpeed].
 */
enum class SystemSpeedCategory(val speedRange: IntRange) {

    Use(66..100),
    Observe(33..66),
    Explore(0..33);

    override fun toString(): String {
        return when(this) {
            SystemSpeedCategory.Use -> Translations.getString("execution.systemSpeedCategory.use")
            SystemSpeedCategory.Observe -> Translations.getString("execution.systemSpeedCategory.observe")
            SystemSpeedCategory.Explore -> Translations.getString("execution.systemSpeedCategory.explore")
        }
    }
}

/**
 * Represents the current [SystemSpeedCategory] depending on the current [SystemSpeed].
 * Listens for changes of [SystemSpeed], updates its current [SystemSpeedCategory], and posts
 * a [SystemSpeedCategoryEvent] on its [EventBus] if the value has changed.
 */
class CurrentSystemSpeedCategory(
    private val systemSpeed: SystemSpeed,
    private val eventBus: EventBus
) {
    @Suppress("unused")
    constructor(): this(BaseModule.systemSpeed, BaseModule.eventBus)

    init {
        eventBus.register(SystemSpeedEvent::class, { update() })
    }

    private val LOG by logger(CurrentSystemSpeedCategory::class)

    var systemSpeedCategory: SystemSpeedCategory = calculate()

    private fun update() {
        val oldValue = systemSpeedCategory
        systemSpeedCategory = calculate()
        if (oldValue != systemSpeedCategory) {
            LOG.debug("CurrentSystemSpeedCategory changed to '$systemSpeedCategory'")
            eventBus.post(SystemSpeedCategoryEvent(oldValue, systemSpeedCategory))
        }
    }

    private fun calculate(): SystemSpeedCategory {
        return SystemSpeedCategory.values().first { systemSpeed.speed >= it.speedRange.first}
    }
}

/** Posted by [CurrentSystemSpeedCategory] when the current [SystemSpeedCategory] has changed.*/
data class SystemSpeedCategoryEvent(val oldValue: SystemSpeedCategory, val newValue: SystemSpeedCategory)