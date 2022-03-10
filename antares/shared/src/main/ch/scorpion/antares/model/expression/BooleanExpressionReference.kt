package ch.scorpion.antares.model.expression

import ch.scorpion.jabbah.app.ApplicationDataContentEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule

class BooleanExpressionReference(
	private val item: BooleanExpressionLibraryItem,
	private val eventBus: EventBus = BaseModule.eventBus
) {

	var expressions: BooleanExpressionStorable = item.expressions
		set(value) {
			field = value
			item.expressions = expressions
			BooleanExpressionEvent(this).also { event ->
				dataListeners.forEach { it.dataChanged(event) }
			}
		}

	private val dataListeners = mutableListOf<BooleanExpressionListener>()

	private val applicationDataContentHandler: EventHandler<ApplicationDataContentEvent> = { updateReference() }

	init {
		updateReference()
		eventBus.register(ApplicationDataContentEvent::class, applicationDataContentHandler)
	}

	fun dispose() {
		eventBus.unregister(applicationDataContentHandler)
	}

	fun addListener(l: BooleanExpressionListener) {
		if (!dataListeners.contains(l)) {
			dataListeners.add(l)
		}
	}

	fun removeListener(l: BooleanExpressionListener) {
		dataListeners.remove(l)
	}

	private fun updateReference() {
		expressions = item.expressions
	}
}

data class BooleanExpressionEvent(
	val source: BooleanExpressionReference
)

fun interface BooleanExpressionListener {
	fun dataChanged(event: BooleanExpressionEvent)
}