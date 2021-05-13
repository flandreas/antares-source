package ch.scorpion.jabbah.base

/** Implemented by [Enum] classes used as properties for which UI editors are provided.*/
interface EnumProperty<T : Enum<T>> {
	val customName: String
}
