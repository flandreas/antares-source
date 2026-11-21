package io.antarescircuit.jabbah.graph.poster

import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.base.geom.packer.Packer
import io.antarescircuit.jabbah.draw.drawable.Page
import kotlin.math.abs
import kotlin.math.ceil

/**
 * An adapter of [Packer] that packs [PosterElement] into a [Page].
 */
class PosterPacker(
    private val page: Page,
    private val elements: Collection<PosterElement>,
    private val gap: Int
) {
    companion object {
        private val LOG by logger(PosterPacker::class)

        private const val ZOOM_DELTA_STOP = 0.01
    }

    private inner class PackResult(val packer: Packer, val zoom: Double, val packedCount: Int) {
        val success: Boolean get() = packedCount == elements.size
        val failure: Boolean get() = !success
    }

    fun pack() {
        var result: PackResult? = null

        var lowerLimit = 0.0
        var upperLimit = 1.0 // we don't want to zoom in to fill free page space

        result = doPack(upperLimit)

        if (result.failure) {
            // Binary search
            while (result!!.failure || abs(upperLimit - lowerLimit) > ZOOM_DELTA_STOP) {
                val zoom = (lowerLimit + upperLimit) / 2
                result = doPack(zoom)
                if (result.success) {
                    lowerLimit = zoom
                } else {
                    upperLimit = zoom
                }
            }
        }

        applyPacking(result.packer, result.zoom)
    }

    private fun doPack(zoom: Double): PackResult {
        val packer = createPacker()
        preparePacking(packer, zoom)
        val packedCount = packer.packRectangles()
        LOG.debug("Packing attempt with zoom=$zoom, packed=$packedCount")
        return PackResult(packer, zoom, packedCount)
    }



    private fun createPacker(): Packer =
        Packer(
            page.usableRectangle.dimension.widthInt,
            page.usableRectangle.dimension.heightInt,
            gap)

    private fun preparePacking(packer: Packer, zoom: Double) {
        elements.forEach { elem ->
            val bbox = elem.metaGraph.graph.graphView.boundingBox
            packer.insertRectangle(
                ceil(bbox.widthInt * zoom  + elem.margin.horizontalSum).toInt(),
                ceil(bbox.heightInt * zoom + elem.margin.verticalSum).toInt(),
                elem.metaGraph.uuid.id)
        }
    }

    private fun applyPacking(packer: Packer, zoom: Double) {
        elements.forEach { elem ->
            packer.getRectangle(elem.metaGraph.uuid.id)?.let { rect ->
                elem.zoom = zoom
                elem.setFrame(
                    (page.margin.left + rect.x).toDouble(),
                    (page.margin.top + rect.y).toDouble(),
                    rect.width.toDouble(),
                    rect.height.toDouble())
            }
        }
    }
}