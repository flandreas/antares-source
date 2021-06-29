package ch.scorpion.jabbah.io

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.logger
import kotlin.reflect.KClass

/**
 * An implementation of a [StoreReader] that reads a hierarchical [Storable] object tree from an XML document (DOM).
 * Since the XML DOM object is different on each supported platform, access to it is encapsulated by [XmlReader].
 */
class StoreXmlReader(
	private val xmlReader: XmlReader,
	private val typeMap: TypeMap,
	private val storableCreator: StorableCreator,
	private val referenceResolver: ReferenceResolver
) : StoreReader {

	companion object {
		private val LOG by logger(StoreXmlReader::class)
	}

	@Suppress("unused")
	constructor(xmlReader: XmlReader, typeMap: TypeMap, storableCreator: StorableCreator) : this(
		xmlReader,
		typeMap,
		storableCreator,
		ReferenceResolverImpl())

	@Suppress("unused")
	constructor(xmlReader: XmlReader) : this(
		xmlReader,
		IOModule.typeMap,
		IOModule.storableCreator,
		ReferenceResolverImpl())


	/** ---- [StoreReader] interface */

	override fun requestResolution(requester: Storable, reference: Reference) {
		referenceResolver.requestResolution(requester, reference)
	}

	override fun hasAttribute(name: String): Boolean {
		return xmlReader.hasAttribute(name)
	}

	override fun hasElement(name: String): Boolean {
		return xmlReader.hasElement(name)
	}

	override fun <T: Storable> getStorable(id: Int): T {
		val storable: T? = referenceResolver.getStorable(id)
		if (storable == null) {
			LOG.error("no Storable with id '$id' found")
			throw IllegalArgumentException()
		}
		return storable
	}

	override fun getGlobalId(storable: Storable): Int =
		referenceResolver.getGlobalId(storable)

	override fun <T: Storable> readStorable(): T {
		val storable: T = readStorableImpl()
		referenceResolver.resolveReferences()
		return storable
	}

	override fun <T: Storable> readStorable(name: String): T {
		if (!xmlReader.hasElement(name)) {
			LOG.error("no element in '${xmlReader.getName()}' with name '$name'")
			throw IllegalArgumentException()
		}
		xmlReader.descend(name)
		xmlReader.descend() // a named Storable has only one element inside its name element

		val storable: T = readStorableImpl()

		xmlReader.ascend()
		xmlReader.ascend()
		return storable
	}

	override fun <T : Storable> readStorable(names: List<String>): T {
		names.forEach {
			if (!xmlReader.hasElement(it)) {
				LOG.error("readStorable at path: no element in '${xmlReader.getName()}' with name '$it'")
				throw IllegalArgumentException()
			}
			xmlReader.descend(it)
			xmlReader.descend() // a named Storable has only one element inside its name element
		}

		val storable: T = readStorableImpl()

		names.forEach { _ ->
			xmlReader.ascend()
			xmlReader.ascend()
		}

		// Accept a ClassCastException if cast fails
		@Suppress("UNCHECKED_CAST")
		return storable
	}

	override fun <T : Storable> readStorables(name: String): List<T> {
		if (!xmlReader.hasElement(name)) {
			return listOf()
		}
		val storables = mutableListOf<T>()
		xmlReader.descend(name)
		for (i in 1..xmlReader.getElementsCount()) {
			xmlReader.descend(i)

			storables.add(readStorableImpl() as T)

			xmlReader.ascend()
		}
		xmlReader.ascend()
		return storables
	}

	override fun readInt(name: String): Int {
		return xmlReader.getAttributeValue(name).toInt()
	}

	override fun readDouble(name: String): Double {
		return xmlReader.getAttributeValue(name).toDouble()
	}

	override fun readString(name: String): String {
		return xmlReader.getAttributeValue(name)
	}

	override fun readOptionalString(name: String): String? {
		if (hasAttribute(name)) {
			return readString(name)
		}
		return null
	}

	override fun readBoolean(name: String): Boolean {
		val b = xmlReader.getAttributeValue(name)
		return b == "1" || b.toLowerCase() == "true"
	}

	override fun readLong(name: String): Long {
		return xmlReader.getAttributeValue(name).toLong()
	}

	override fun readPoints(name: String): List<Point2D> {
		val points = mutableListOf<Point2D>()
		var list = readString(name)
		try {
			while (list.isNotEmpty()) {
				val i = list.indexOf(',')
				val j = list.indexOf(' ')
				val x = list.substring(0, i).toDouble()
				val y = (if (j != -1) list.substring(i + 1, j) else list.substring(i + 1)).toDouble()
				points.add(Point2D(x, y))
				list = if (j != -1) list.substring(j).trim() else ""
			}
			return points
		} catch (e: Throwable) {
			LOG.error("Parsing of point list '$name' failed with '${e.message}'")
			throw e
		}
	}

	override fun readPoints(outerElem: String, innerElem: String, attribute: String): List<Point2D> {
		xmlReader.descend(outerElem)
		xmlReader.descend(innerElem)
		val list = readPoints(attribute)
		xmlReader.ascend()
		xmlReader.ascend()
		return list
	}

	override fun readPoint(name: String): Point2D {
		xmlReader.descend(name)
		xmlReader.descend("point")
		val p = Point2D(readDouble("x"), readDouble("y"))
		xmlReader.ascend()
		xmlReader.ascend()
		return p
	}

	override fun readIntegers(name: String): List<Int> =
		xmlReader.getAttributeValue(name).split(",").map { it.toInt() }

	/** ---- [StoreXmlReader] */

	private fun <T: Storable> readStorableImpl(): T {
		val storable = instantiate(xmlReader.getName())
		readGlobalId()?.let { referenceResolver.addStorable(it, storable) }
		storable.read(this)

		// Accept a ClassCastException if cast fails
		@Suppress("UNCHECKED_CAST")
		return storable as T
	}

	private fun instantiate(typeName: String): Storable {
		LOG.trace("instantiate '$typeName'")
		return instantiate(typeMap.getClass(typeName))
	}

	private fun instantiate(clazz: KClass<Storable>): Storable {
		LOG.trace("instantiate '${clazz.simpleName}'")
		return storableCreator.create(clazz)
	}

	private fun readGlobalId(): Int? {
		if (!xmlReader.hasAttribute("_id")) {
			return null
		}
		return xmlReader.getAttributeValue("_id").toInt()
	}
}

