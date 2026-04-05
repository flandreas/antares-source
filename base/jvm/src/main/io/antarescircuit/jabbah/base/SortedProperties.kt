package io.antarescircuit.jabbah.base

import java.io.OutputStream
import java.util.*

/**
 * An implementation of [java.util.Properties] that sorts keys when storing the properties.
 * Source: https://stackoverflow.com/questions/10275862/how-to-sort-properties-in-java
 */
class SortedProperties : java.util.Properties() {

	override fun store(out: OutputStream?, comments: String?) {
		val sortedProperties = object : java.util.Properties() {
			override val entries: MutableSet<MutableMap.MutableEntry<Any, Any>> get() {
				val sortedSet = TreeSet<MutableMap.MutableEntry<Any, Any>> { o1, o2 ->
					o1.key.toString().compareTo(o2.key.toString())
				}
				sortedSet.addAll(super.entries)
				return sortedSet
			}
		}

		sortedProperties.putAll(this)
		sortedProperties.store(out, comments)
	}
}