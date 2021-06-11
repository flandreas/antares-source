package ch.scorpion.jabbah.io

import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.formatRounded

/**
 * An implementation of a [StoreReader] that writes a hierarchical [Storable] object tree to an XML document (DOM).
 * Since the XML DOM object is different on each supported platform, access to it is encapsulated by [XmlReader].
 */
class StoreXmlWriter(
	private val xmlWriter: XmlWriter,
	private val typeMap: TypeMap,
	private val identityProvider: GlobalIdentityProvider,
	private val filter: (Storable) -> Boolean = { true }
) : StoreWriter {

	@Suppress("unused")
	constructor(xmlWriter: XmlWriter) : this(
		xmlWriter,
		IOModule.typeMap,
		GlobalIdentityCreator()
	)

	/** ---- [GlobalIdentityProvider] */

	override fun register(storable: Storable) {
		identityProvider.register(storable)
	}

	override fun provideIdentity(storable: Storable): Int =
		identityProvider.provideIdentity(storable)

	/** ---- [StoreWriter] */

	override fun writeStorable(storable: Storable) {
		val type = typeMap.getTypeName(System.getClass(storable))

		if (xmlWriter.isRoot()) {
			StorableHierarchy.collect(storable) { identityProvider.register(it) }
		}
		xmlWriter.addElementAndDescend(type)

		val globalId = provideIdentity(storable)
		if (globalId != -1) {
			if (storable.storableId <= 0) {
				// Setting storableId not only when reading Storables, but already when writing them,
				// allows to duplicate Storables using GlobalIdentityReflector without the need to
				// first write and read them back.
				storable.storableId = globalId
			}
			xmlWriter.setAttributeValue("_id", globalId.toString())
		}

		storable.write(this)
		xmlWriter.ascend()

		if (xmlWriter.isRoot()) {
			xmlWriter.flush()
		}
	}

	override fun writeStorable(name: String, storable: Storable) {
		xmlWriter.addElementAndDescend(name)
		writeStorable(storable)
		xmlWriter.ascend()
	}

	override fun writeStorables(name: String, iterator: Iterator<Storable>) {
		xmlWriter.addElementAndDescend(name)
		for (storable in iterator) {
			if (filter.invoke(storable)) {
				writeStorable(storable)
			}
		}
		xmlWriter.ascend()
	}

	override fun writeInt(name: String, value: Int) {
		xmlWriter.setAttributeValue(name, value.toString())
	}

	override fun writeDouble(name: String, value: Double) {
		xmlWriter.setAttributeValue(name, value.formatRounded())
	}

	override fun writeString(name: String, value: String) {
		xmlWriter.setAttributeValue(name, value)
	}

	override fun writeOptionalString(name: String, value: String?) {
		if (StringUtils.isNotEmpty(value)) {
			writeString(name, value!!)
		}
	}

	override fun writeBoolean(name: String, value: Boolean) {
		xmlWriter.setAttributeValue(name, if (value) "true" else "false")
	}

	override fun writeLong(name: String, value: Long) {
		xmlWriter.setAttributeValue(name, value.toString())
	}

	override fun writePoints(name: String, points: List<Point2D>) {
		val list = StringBuilder()
		for (i in 0 until points.size) {
			list.append("${points[i].x.formatRounded()},${points[i].y.formatRounded()} ")
		}
		writeString(name, list.toString().trim())
	}

	override fun writePoints(outerElem: String, innerElem: String, attribute: String, points: List<Point2D>) {
		xmlWriter.addElementAndDescend(outerElem)
		xmlWriter.addElementAndDescend(innerElem)
		writePoints(attribute, points)
		xmlWriter.ascend()
		xmlWriter.ascend()
	}

	override fun writePoint(name: String, point: Point2D) {
		xmlWriter.addElementAndDescend(name)
		xmlWriter.addElementAndDescend("point")
		writeDouble("x", point.x)
		writeDouble("y", point.y)
		xmlWriter.ascend()
		xmlWriter.ascend()
	}

	override fun writeIntegers(name: String, integers: List<Int>) {
		if (integers.isNotEmpty()) {
			writeString(name, integers.joinToString(","))
		}
	}
}
