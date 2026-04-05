package io.antarescircuit.jabbah.app

import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.event.PropertyOwner
import io.antarescircuit.jabbah.base.event.PropertyOwnerImpl
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.edit.UndoableDataHolder
import io.antarescircuit.jabbah.io.Storable

/**
 * The data being held by an [Application] and possibly being edited by the user.
 *
 * @property focusItem the optional item withing [content] to be focused (i.e. bring to the user's attention)
 * when [content] is opened.
 */
class ApplicationData(
	content: Storable,
	val savable: Savable,
	private val eventBus: EventBus = BaseModule.eventBus,
	private val propertyOwner: PropertyOwner<Storable> = PropertyOwnerImpl(),
	val focusItem: Any? = null
): PropertyOwner<Storable> by propertyOwner {

	companion object {
		const val PROP_CONTENT = "content"
	}

	override fun toString(): String = savable.description

	/**
	 * The effective data content to be stored in [savable].
	 * Note that this content can be exchanged while undo/redo actions requiring replaying from a snapshot.
	 */
	var content: Storable = content
		set(value) {
			if (field !== value) {
				val oldContent = field
				field = value

				// TODO Remove, not used
				fire(PROP_CONTENT, oldContent, field)

				eventBus.post(ApplicationDataContentEvent(this, oldContent))
			}
		}

	init {
		source = this
	}

	fun withSavable(savable: Savable): ApplicationData {
		return ApplicationData(content, savable, eventBus)
	}
}

/**
 * The object holding the current [ApplicationData].
 * Posts an [ApplicationDataEvent] and a [CurrentSavableEvent] on [EventBus] when it has changed.
 */
interface ApplicationDataHolder : UndoableDataHolder {
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

/**
 * Posted by client classes on [EventBus] to request immediate closing of the current [ApplicationData].
 * Handled by [ApplicationDataHolder] unconditionally. It is up to the requester to make sure
 * that unsaved data is saved before issuing this request.
 */
class CloseApplicationDataRequest

/**
 * Posted on an [EventBus] after an [ApplicationData]'s content has been completely established.
 */
data class ApplicationDataContentEstablishedEvent(
	val data: ApplicationData
)
