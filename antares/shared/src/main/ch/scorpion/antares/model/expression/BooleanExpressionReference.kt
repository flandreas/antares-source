package ch.scorpion.antares.model.expression

import ch.scorpion.jabbah.app.ApplicationData
import ch.scorpion.jabbah.app.ApplicationDataContentEvent
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule

class BooleanExpressionReference(
	storable: BooleanExpressionStorable,
	private val eventBus: EventBus = BaseModule.eventBus
) {

	var expressions: BooleanExpressionStorable = storable
		private set(value) {
			field = value
			notifyUpdate()
		}

	private val dataListeners = mutableListOf<BooleanExpressionListener>()

	private val applicationDataContentHandler: EventHandler<ApplicationDataContentEvent> = { updateReference(it.data) }

	init {
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

	fun updateExpressions(e: String) {
		expressions.expressions = e
		notifyUpdate()
	}

	fun updateSingleChar(b: Boolean) {
		expressions.singleCharIdentifier = b
		notifyUpdate()
	}

	private fun updateReference(appData: ApplicationData) {
		expressions = appData.content as BooleanExpressionStorable
	}

	private fun notifyUpdate() {
		BooleanExpressionEvent(this).also { event ->
			dataListeners.forEach { it.dataChanged(event) }
		}
	}
}

data class BooleanExpressionEvent(
	val source: BooleanExpressionReference
)

fun interface BooleanExpressionListener {
	fun dataChanged(event: BooleanExpressionEvent)
}