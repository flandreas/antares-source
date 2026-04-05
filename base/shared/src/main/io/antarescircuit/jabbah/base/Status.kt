package io.antarescircuit.jabbah.base

import io.antarescircuit.jabbah.base.module.BaseModule

enum class StatusType {
	Large,
	Small,
	Tool
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

	operator fun get(type: StatusType): String? = values[type]

	fun replace(type: StatusType, value: String?): String? {
		val oldValue = values[type]
		set(type, value)
		return oldValue
	}
}

data class StatusEvent(val type: StatusType, val status: String?)