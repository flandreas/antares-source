package ch.scorpion.jabbah.base

import ch.scorpion.jabbah.base.module.BaseModule

enum class StatusType {
	Large,
	Small
}

object Status {

	private val values = mutableMapOf<StatusType,String?>()

	fun set(type: StatusType, value: String?) {
		val oldValue = values[type]
		if (value?.equals(oldValue) != true) {
			BaseModule.eventBus.post(StatusEvent(type, value))
			values[type] = value
		}
	}

	fun get(type: StatusType): String? {
		return values[type]
	}
}

data class StatusEvent(val type: StatusType, val status: String?)