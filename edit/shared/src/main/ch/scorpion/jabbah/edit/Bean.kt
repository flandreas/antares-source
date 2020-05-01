package ch.scorpion.jabbah.edit

/**
 * Marker interface to be implemented by classes with an associated bean info class.
 * Used to prevent such classes from obfuscation when used on the JVM platform.
 */
interface Bean {
	// empty
}

/**
 * Used by [Command]s to access a bean only by its ID. The provided [List] of IDs represents the chain
 * from the topmost object in the [Drawing] held by the [Editor], down to the bean that is to be provided,
 * thereby allowing to reference chains of composed objects.
 */
typealias BeanProvider = (Editor, List<Int>) -> Bean

/** Provides the [Component] of an [Editor]'s [Drawing] with a particular ID. */
val componentBeanProvider: BeanProvider = { e, ids -> e.drawing.getWithId(ids[0])!!.propertyOwner as Component }

/** Provides the current [Drawing] of an [Editor].*/
val drawingBeanProvider: BeanProvider = { e, _ -> e.drawing }
