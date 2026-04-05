package io.antarescircuit.jabbah.base

import kotlin.reflect.KClass

object Inheritance {

	/**
	 * Finds the nearest common superclass of all [classes]. Interfaces are not respected.
	 * Source: https://stackoverflow.com/questions/9797212/finding-the-nearest-common-superclass-or-superinterface-of-a-collection-of-cla
	 */
	fun commonSuperClass(classes: Collection<KClass<*>>): KClass<*>? {
		if (classes.isEmpty()) {
			return null
		}
		val rollingIntersect = mutableSetOf<KClass<*>>()

		// start off with set from first hierarchy
		rollingIntersect.addAll(getClassesBfs(classes.first()))

		// intersect with next
		for (i in 1 until classes.size) {
			rollingIntersect.retainAll(getClassesBfs(classes.elementAt(i)))
		}

		return if (rollingIntersect.isEmpty()) null else rollingIntersect.first()
	}

	private fun getClassesBfs(clazz: KClass<*>): Set<KClass<*>> {
		val classes = mutableSetOf<KClass<*>>()
		val nextLevel = mutableSetOf<KClass<*>>()
		nextLevel.add(clazz)

		do {
			classes.addAll(nextLevel)
			val thisLevel = mutableSetOf<KClass<*>>()
			thisLevel.addAll(nextLevel)
			nextLevel.clear()

			for (each in thisLevel) {
				val superClass = each.java.superclass
				if (superClass != null && superClass != Any::class.java) {
					nextLevel.add(superClass.kotlin)
				}
			}

		} while (nextLevel.isNotEmpty())

		return classes
	}
}