package io.antarescircuit.jabbah.io

import io.antarescircuit.jabbah.base.StringUtils
import io.antarescircuit.jabbah.base.UUID
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.math.formatRounded

/**
 * An implementation of a [StoreWriter] that writes a hierarchical [Storable] object tree to an XML document (DOM).
 * Since the XML DOM object is different on each supported platform, access to it is encapsulated by [XmlWriter].
 */
class StoreXmlWriter(
	private val xmlWriter: XmlWriter,
	private val typeMap: TypeMap,
	private val identityProvider: GlobalIdentityProvider,
	private val filter: (s: Storable, isToplevel: Boolean) -> Boolean = { _, _ -> true }
) : StoreWriter {

	companion object {
		private const val DOUBLE_ROUND_PRECISION = 10_000_000.0
	}

	private var level: Int = 0

	@Suppress("unused")
	constructor(xmlWriter: XmlWriter) : this(
		xmlWriter,
		IOModule.typeMap,
		GlobalIdentityCreator()
	)

	/** ---- [GlobalIdentityProvider] */

	override fun provideIdentity(storable: Storable): Int =
		identityProvider.provideIdentity(storable)

	override fun getIdentity(storable: Storable): Int =
		identityProvider.getIdentity(storable)

	override fun getStorableWithIdentity(globalId: Int): Storable? =
		identityProvider.getStorableWithIdentity(globalId)

	/** ---- [StoreWriter] */

	override fun writeStorable(storable: Storable) {
		val type = typeMap.getTypeName(storable)

		xmlWriter.addElementAndDescend(type)

		if (storable.isReferencable) {
			xmlWriter.setAttributeValue("_id", provideIdentity(storable).toString())
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
		try {
			xmlWriter.addElementAndDescend(name)
			level++
			for (storable in iterator) {
				if (filter.invoke(storable, level <= 1)) {
					writeStorable(storable)
				}
			}
			xmlWriter.ascend()
		} finally {
			level--
		}
	}

	override fun writeMap(name: String, map: Map<String, Storable>) {
		xmlWriter.addElementAndDescend(name)
		map.keys.sorted().forEach {
			writeStorable(it, map[it]!!)
		}
		xmlWriter.ascend()
	}

	override fun writeInt(name: String, value: Int) {
		xmlWriter.setAttributeValue(name, value.toString())
	}

	override fun writeDouble(name: String, value: Double) {
		xmlWriter.setAttributeValue(name, value.formatRounded())
	}

	override fun writePreciseDouble(name: String, value: Double) {
		xmlWriter.setAttributeValue(name, value.formatRounded(DOUBLE_ROUND_PRECISION))
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

	override fun writeULong(name: String, value: ULong) {
		xmlWriter.setAttributeValue(name, value.toString())
	}

	override fun writePoints(name: String, points: List<Point2D>) {
		val list = StringBuilder()
		for (i in points.indices) {
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

	override fun writeUuids(name: String, uuids: Set<UUID>) {
		if (uuids.isNotEmpty()) {
			writeString(name, uuids.joinToString(","))
		}
	}

	override fun writeText(name: String, text: String) {
		xmlWriter.setText(name, "\n$text\n")
	}
}
