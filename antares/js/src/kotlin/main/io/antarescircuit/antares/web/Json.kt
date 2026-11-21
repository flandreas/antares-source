package io.antarescircuit.antares.web

/**
 * Creates a plain JS object configured by [init], for passing structured data to the web application.
 * Within [init], names resolve against the `dynamic` receiver first: compute values that need class
 * qualifiers (e.g. `SomeEnum.Value`) outside the block, and assign with explicit `this.name = ...`.
 */
internal fun obj(init: dynamic.() -> Unit): dynamic {
	val o: dynamic = js("({})")
	init(o)
	return o
}

internal fun <T> List<T>.toJsArray(): Array<T> = toTypedArray()

internal fun toJson(value: dynamic): String = JSON.stringify(value)
