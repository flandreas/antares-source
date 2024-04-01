package ch.scorpion.jabbah.execution.speed

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.time.SystemSpeed
import ch.scorpion.jabbah.execution.ExecutionTestRule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class CurrentSystemSpeedCategoryTest {

    companion object {

        private var event: SystemSpeedCategoryEvent? = null

        init {
            Translations.withAnyKey()
            ExecutionTestRule.configure()
            BaseModule.eventBus.register(SystemSpeedCategoryEvent::class) {
                event = it
            }
        }
    }

    init {
        event = null
    }

    @Test
    fun shouldChangeFromObserveToExplore() {
        val category = CurrentSystemSpeedCategory(SystemSpeed(50))
        category.systemSpeed.speed = 20
        assertEquals(SystemSpeedCategory.Observe, event!!.oldValue)
        assertEquals(SystemSpeedCategory.Explore, event!!.newValue)
    }

    @Test
    fun shouldChangeFromExploreToObserve() {
        val category = CurrentSystemSpeedCategory(SystemSpeed(20))
        category.systemSpeed.speed = 50
        assertEquals(SystemSpeedCategory.Explore, event!!.oldValue)
        assertEquals(SystemSpeedCategory.Observe, event!!.newValue)
    }

    @Test
    fun shouldNotChange() {
        val category = CurrentSystemSpeedCategory(SystemSpeed(20))
        category.systemSpeed.speed = 21
        assertNull(event)
    }
}