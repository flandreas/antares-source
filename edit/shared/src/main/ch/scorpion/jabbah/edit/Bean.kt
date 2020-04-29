package ch.scorpion.jabbah.edit

/**
 * Marker interface to be implemented by classes with an associated bean info class.
 * Used to prevent such classes from obfuscation when used on the JVM platform.
 */
interface Bean {
	// empty
}

typealias BeanProvider = (Editor, Int?) -> Bean

val componentBeanProvider: BeanProvider = { e, id -> e.drawing.getWithId(id!!)!!.propertyOwner as Component }
val drawingBeanProvider: BeanProvider = { e, id -> e.drawing }
