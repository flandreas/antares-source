package io.antarescircuit.jabbah.base.event

class EventBusStatistics(
    private val eventBusId: String
) {

    /** Maps event class names to number of registered handlers for that event.*/
    private val registrationCount: MutableMap<String, Int> = mutableMapOf()

    fun addRegistrationCount(event: String, count: Int) {
        registrationCount[event] = registrationCount.getOrPut(event) { 0 } + count
    }

    fun print(): String {
        val sb = StringBuilder()
        val handlerCount = registrationCount.map { it.value }.sum()

        sb.appendLine("EventBus $eventBusId: ${registrationCount.size} events, $handlerCount handlers")
        registrationCount.entries
            .sortedByDescending { it.value }
            .forEach { sb.appendLine("${it.value}: ${it.key}") }

        return sb.toString()
    }

    /**
     * Prints how many handlers of certain event classes have been added to this [EventBusStatistics]
     * compared to [other].
     */
    fun printExpansion(other: EventBusStatistics): String {
        val sb = StringBuilder()
        sb.appendLine("EventBus $eventBusId expansion")
        registrationCount.entries.forEach { entry ->
            val oldCount = other.registrationCount.getOrElse(entry.key) { 0 }
            if (entry.value > oldCount) {
                sb.appendLine("${entry.key}: $oldCount -> ${entry.value}")
            }
        }
        return sb.toString()
    }
}