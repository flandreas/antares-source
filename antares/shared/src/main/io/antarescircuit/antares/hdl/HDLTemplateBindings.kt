package io.antarescircuit.antares.hdl

import io.antarescircuit.antares.hdl.vhdl.HDLException
import io.antarescircuit.antares.model.addressable.AbstractAddressable
import io.antarescircuit.antares.model.addressable.RAM
import io.antarescircuit.antares.model.gate.TriStateBufferGate
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.jabbah.graph.model.Vertice
import korlibs.template.AutoEscapeMode
import korlibs.template.Template
import korlibs.template.TemplateConfig
import korlibs.template.dynamic.DynamicType
import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine

/*
 * Korte accesses template variables by reflection on the JVM, which is not available on JS.
 * Objects passed to HDL templates therefore declare their properties as korte [DynamicType]s.
 */

/**
 * Runs [block] synchronously. Korte templates without includes never actually suspend,
 * so this works on all platforms without `runBlocking`.
 */
internal fun <T> runTemplateSync(block: suspend () -> T): T {
	var result: Result<T>? = null
	block.startCoroutine(Continuation(EmptyCoroutineContext) { result = it })
	return (result ?: throw IllegalStateException("Template evaluation suspended")).getOrThrow()
}

/** Parses the template file [fileName] from [HDLTemplates], throwing an [HDLException] if there is none. */
internal fun loadHDLTemplate(fileName: String): Template {
	val text = HDLTemplates.get(fileName) ?: throw HDLException("HDL template $fileName not found")
	return runTemplateSync { Template(text, TemplateConfig(autoEscapeMode = AutoEscapeMode.RAW)) }
}

/** Unwraps a [BitWidth] passed from a template to a helper method. */
internal fun templateBitWidth(arg: Any?): BitWidth = when (arg) {
	is HDLTemplateBitWidth -> arg.bitWidth
	is BitWidth -> arg
	else -> throw IllegalArgumentException("Not a bit width: $arg")
}

internal fun templateInt(arg: Any?): Int = (arg as Number).toInt()

/** A [BitWidth] as seen by templates ("vertice.dataWidth.width"). */
class HDLTemplateBitWidth(val bitWidth: BitWidth) : DynamicType<HDLTemplateBitWidth> by DynamicType({
	register(HDLTemplateBitWidth::width)
}) {
	val width: Int get() = bitWidth.width
}

/** The memory of an addressable as seen by templates ("vertice.memory.nonZeroLength"). */
class HDLTemplateMemory(private val vertice: AbstractAddressable<*>) : DynamicType<HDLTemplateMemory> by DynamicType({
	register(HDLTemplateMemory::nonZeroLength)
}) {
	val nonZeroLength: Int get() = vertice.memory.nonZeroLength
}

/**
 * The [Vertice] of a [BuiltInNode] as seen by templates ("vertice"), exposing the properties
 * used by the templates of the supported component types.
 */
class HDLTemplateVertice(val vertice: Vertice) : DynamicType<HDLTemplateVertice> by DynamicType({
	register(
		HDLTemplateVertice::name,
		HDLTemplateVertice::addressWidth,
		HDLTemplateVertice::dataWidth,
		HDLTemplateVertice::memory,
		HDLTemplateVertice::hasClock,
		HDLTemplateVertice::separateDataPorts,
		HDLTemplateVertice::bitWidth,
		HDLTemplateVertice::isNegative,
	)
	register("dataAsPaddedBinaryAt") { args -> addressable.dataAsPaddedBinaryAt(templateInt(args[0])) }
}) {
	private val addressable: AbstractAddressable<*> get() = vertice as AbstractAddressable<*>

	val name: String? get() = vertice.name
	val addressWidth: HDLTemplateBitWidth get() = HDLTemplateBitWidth(addressable.addressWidth)
	val dataWidth: HDLTemplateBitWidth get() = HDLTemplateBitWidth(addressable.dataWidth)
	val memory: HDLTemplateMemory get() = HDLTemplateMemory(addressable)
	val hasClock: Boolean get() = (vertice as RAM).hasClock
	val separateDataPorts: Boolean get() = (vertice as RAM).separateDataPorts
	val bitWidth: HDLTemplateBitWidth get() = HDLTemplateBitWidth((vertice as TriStateBufferGate).bitWidth)
	val isNegative: Boolean get() = (vertice as TriStateBufferGate).isNegative
}
