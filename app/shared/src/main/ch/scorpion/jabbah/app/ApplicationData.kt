package ch.scorpion.jabbah.app

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.io.Storable

/**
 * The data being held by an [Application] and possibly being edited by the user.
 */
class ApplicationData(
	content: Storable,
	val savable: Savable,
	private val eventBus: EventBus = BaseModule.eventBus
) {

	override fun toString(): String = savable.description

	var content: Storable = content
		set(value) {
			if (field !== value) {
				val oldContent = field
				field = value
				eventBus.post(ApplicationDataContentEvent(this, oldContent))
			}
		}

	fun withSavable(savable: Savable): ApplicationData {
		return ApplicationData(content, savable, eventBus)
	}
}

/**
 * The object holding the current [ApplicationData].
 * Posts an [ApplicationDataEvent] and a [CurrentSavableEvent] on [EventBus] when it has changed.
 */
interface ApplicationDataHolder {

	var data: ApplicationData?
}

/**
 * Posted on an [EventBus] when the current application data in an [Application] has changed.
 */
data class ApplicationDataEvent(
	val oldData: ApplicationData?,
	val newData: ApplicationData?
)

data class ApplicationDataContentEvent(
	val data: ApplicationData,
	val oldContent: Storable
)
