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
 * Posted on an [EventBus] when the current application data in an [Application] has changed.
 */
data class ApplicationDataEvent(
	val application: Application,
	val oldData: ApplicationData?,
	val newData: ApplicationData?
)

data class ApplicationDataContentEvent(
	val data: ApplicationData,
	val oldContent: Storable
)

/**
 * A request to close the specified application data [Storable]. The class that centrally manages application state
 * has to decide whether this is possible and allowed, in which case it clears the application state and
 * sends an [ApplicationDataEvent] with an empty `newData` value. All classes that display the current application
 * state then react to that and display an `empty` state.
 */
data class CloseApplicationDataRequest(val data: Storable)