package io.antarescircuit.antares.model.net

import io.antarescircuit.antares.model.PortCount
import io.antarescircuit.antares.model.signal.BitWidth
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.io.*

class WireTapConfig(
	wideSideBitWidth: BitWidth = BitWidth.BW_2,
	narrowSideBitWidth: BitWidth = BitWidth.BW_1,
	narrowPortCount: PortCount = PortCount.ONE,
	tapPositions: List<Int> = defaultTapPositions(narrowSideBitWidth, narrowPortCount)
) : AbstractStorable() {

	companion object {

		private const val MAX_TAP_COUNT = 8

		private fun defaultTapPositions(narrowSideBitWidth: BitWidth, narrowPortCount: PortCount): List<Int> =
			List(narrowPortCount.count) { it * narrowSideBitWidth.width }

		fun withDefaultTapPositions(
			wideSideBitWidth: BitWidth,
			narrowSideBitWidth: BitWidth,
			narrowPortCount: PortCount
		): WireTapConfig = WireTapConfig(
			wideSideBitWidth,
			narrowSideBitWidth,
			narrowPortCount,
			defaultTapPositions(narrowSideBitWidth, narrowPortCount))
	}

	var wideSideBitWidth: BitWidth = wideSideBitWidth
		private set

	var narrowSideBitWidth = narrowSideBitWidth
		private set

	var narrowPortCount: PortCount = narrowPortCount
		private set

	var tapPositions: List<Int> = tapPositions
		private set

	init {
		validate()
	}

	/** ---- [Storable] */

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) { }

	override fun write(writer: StoreWriter) {
		writer.writeInt("narrowPortCount", narrowPortCount.count)
		writer.writeInt("wideBitWidth", wideSideBitWidth.width)
		writer.writeInt("narrowBitWidth", narrowSideBitWidth.width)
		writer.writeIntegers("positions", tapPositions)

	}

	override fun read(reader: StoreReader) {
		narrowPortCount = PortCount.of(reader.readInt("narrowPortCount"))
		wideSideBitWidth = BitWidth.read("wideBitWidth", reader)
		narrowSideBitWidth = BitWidth.read("narrowBitWidth", reader)
		tapPositions = reader.readIntegers("positions")
	}

	/** ---- [WireTapConfig] */

	fun withWideSideBitWidth(bw: BitWidth): WireTapConfig =
		if (bw.width > narrowSideBitWidth.width * narrowPortCount.count) {
			withDefaultTapPositions(bw, narrowSideBitWidth, narrowPortCount)
		} else {
			withDefaultTapPositions(bw, BitWidth.BW_1, PortCount.ONE)
		}

	fun withNarrowSideBitWidth(bw: BitWidth): WireTapConfig =
		withDefaultTapPositions(wideSideBitWidth, bw, narrowPortCount)

	fun withTapPosition(index: Int, pos: Int): WireTapConfig =
		WireTapConfig(wideSideBitWidth, narrowSideBitWidth, narrowPortCount,
			tapPositions.mapIndexed { i: Int, p: Int -> if (i == index) pos else p})

	fun withAddedNarrowPorts(count: Int): WireTapConfig {
		if (narrowPortCount.count + count > MAX_TAP_COUNT) {
			throw IllegalArgumentException("Max. $MAX_TAP_COUNT output ports allowed in WireTap")
		}
		return withDefaultTapPositions(wideSideBitWidth, narrowSideBitWidth, PortCount.of(narrowPortCount.count + count))
	}

	fun withRemovedNarrowPort(): WireTapConfig =
		WireTapConfig(wideSideBitWidth, narrowSideBitWidth, PortCount.of(narrowPortCount.count - 1),
			tapPositions.dropLast(1))

	private fun validate() {
		rejectExceedingTaps()
		rejectExceedingTapPosition()
		rejectTapIntersections()
	}

	private fun rejectExceedingTaps() {
		if (narrowSideBitWidth.width * narrowPortCount.count > wideSideBitWidth.width) {
			throw IllegalArgumentException(Translations.getString("library.element.WireTap.tooManyTaps.msg"))
		}
	}

	private fun rejectExceedingTapPosition() {
		for (portId in 2 until 2 + narrowPortCount.count) {
			if (tapPositions[portId - 2] + narrowSideBitWidth.width > wideSideBitWidth.width) {
				throw IllegalArgumentException(Translations.getString("library.element.WireTap.position.msg", 0, wideSideBitWidth.width - narrowSideBitWidth.width - 1))
			}
		}
	}

	private fun rejectTapIntersections() {
		val tapped = MutableList(wideSideBitWidth.width) { false }
		for (portId in 2 until 2 + narrowPortCount.count) {
			for (bitIndex in 0 until narrowSideBitWidth.width) {
				val i = tapPositions[portId - 2] + bitIndex
				if (tapped[i]) {
					throw IllegalArgumentException(Translations.getString("library.element.WireTap.tapIntersection.msg", i))
				}
				tapped[i] = true
			}
		}
	}
}