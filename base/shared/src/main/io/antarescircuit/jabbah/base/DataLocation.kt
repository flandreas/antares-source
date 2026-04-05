package io.antarescircuit.jabbah.base

/** Determines the location where an application stores its data.*/
enum class DataLocation(override val customName: String): EnumProperty<DataLocation> {
	Local("local"),
	Remote("remote");

	companion object {
		const val PROP_DATA_LOCATION = "base.dataLocation"
		const val PROP_SERVER_URL = "base.serverUrl"

		fun withName(customName: String): DataLocation =
			values().firstOrNull { it.customName == customName }
				?: throw IllegalArgumentException("unknown DataLocation '$customName'")
	}

	override fun toString(): String {
		return when (this) {
			Local -> Translations.getString("base.dataLocation.local.name")
			Remote -> Translations.getString("base.dataLocation.remote.name")
		}
	}
}