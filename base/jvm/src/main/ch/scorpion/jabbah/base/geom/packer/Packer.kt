package ch.scorpion.jabbah.base.geom.packer

/**
 * Packs rectangles within a container rectangle with close to optimal solution.
 */
class Packer(width: Int, height: Int, padding: Int = DEF_PADDING) {

    companion object {
        private const val DEF_PADDING = 8
    }

    private var width: Int = 0
    private var height: Int = 0

    private var padding: Int = padding

    private var packedWidth: Int = 0
    private var packedHeight: Int = 0

    private val outsideRectangle = IntegerRectangle(width + 1, height + 1, 0, 0)

    private val insertedRectangles = mutableListOf<IntegerRectangle>()

    private val rectangleStack = mutableListOf<IntegerRectangle>()

    private val freeAreas = mutableListOf<IntegerRectangle>()

    private val newFreeAreas = mutableListOf<IntegerRectangle>()

    private var insertList = mutableListOf<SortableSize>()

    private var sortableSizeStack = mutableListOf<SortableSize>()

    val rectangleCount: Int get() = insertedRectangles.size

    init {
        reset(width, height, padding)
    }

    private fun reset(width: Int, height: Int, padding: Int = DEF_PADDING) {
        while (insertedRectangles.isNotEmpty()) {
            freeRectangle(insertedRectangles.removeLast())
        }

        while (freeAreas.isNotEmpty()) {
            freeRectangle(freeAreas.removeLast())
        }

        this.width = width
        this.height = height

        packedWidth = 0
        packedHeight = 0

        freeAreas.add(allocateRectangle(0, 0, width, height))

        while (insertList.isNotEmpty()) {
            freeSize(insertList.removeLast())
        }

        this.padding = padding
    }

    /**
     * Adds a rectangle to be packed into the Packer.
     */
    fun insertRectangle(width: Int, height: Int, id: String): Packer {
        insertList.add(allocateSize(width, height, id))
        return this
    }

    fun getRectangle(id: String): IntegerRectangle? =
        insertedRectangles.firstOrNull { it.id == id }

    /**
     * Packs the inserted rectangles.
     * @param sort `true` if the inserted rectangles are to be sorted before packing
     * @return the number of packed rectangles
     */
    fun packRectangles(sort: Boolean = true): Int {
        if (sort) {
            insertList.sortBy { it.width }
        }

        while (insertList.isNotEmpty()) {
            val sortableSize = insertList.removeLast()
            val width = sortableSize.width
            val height = sortableSize.height

            val index = getFreeAreaIndex(width, height)
            if (index >= 0) {
                val freeArea = freeAreas[index]
                val target = allocateRectangle(freeArea.x, freeArea.y, width, height)
                target.id = sortableSize.id

                // Generate the new free areas, these are part of the old ones intersected or touched by the target
                generateNewFreeAreas(target, freeAreas, newFreeAreas)

                while (newFreeAreas.isNotEmpty()) {
                    freeAreas.add(newFreeAreas.removeLast())
                }

                insertedRectangles.add(target)
                if (target.right > packedWidth) {
                    packedWidth = target.right
                }
                if (target.bottom > packedHeight) {
                    packedHeight = target.bottom
                }
            }

            freeSize(sortableSize)
        }

        return rectangleCount
    }

    /**
     * Gets the index of the best free are for the given rectangle.
     * @return the index of the best free area or -1 if no suitable free area is available
     */
    private fun getFreeAreaIndex(width: Int, height: Int): Int {
        var best: IntegerRectangle = outsideRectangle
        var index = -1

        val paddedWidth = width + padding
        val paddedHeight = height + padding

        val count = freeAreas.size
        for (i in count - 1 downTo  0) {
            val free = freeAreas[i]
            if (free.x < packedWidth || free.y < packedHeight) {
                // Within the packed area, padding required
                if (free.x < best.x && paddedWidth <= free.width && paddedHeight <= free.height) {
                    index = i
                    if ((paddedWidth == free.width && free.width <= free.height && free.right < width) || (paddedHeight == free.height && free.height <= free.width)) {
                        break
                    }
                    best = free
                }
            } else {
                // Outside the current packed area, no padding required
                if (free.x < best.x && width <= free.width && height <= free.height) {
                    index = i
                    if ((width == free.width && free.width <= free.height && free.right < width) || (height == free.height && free.height <= free.width)) {
                        break
                    }
                    best = free
                }
            }
        }

        return index
    }

    /**
     * Checks what areas the given rectangle intersects, removes those areas and returns
     * the list of new areas those areas are divided into
     * @param target the new rectangle that is dividing the areas
     * @param areas the areas to be divided
     */
    private fun generateNewFreeAreas(target: IntegerRectangle, areas: MutableList<IntegerRectangle>, results: MutableList<IntegerRectangle>) {
        // Increase dimensions by one to get the areas on right / bottom this rectangle touches
        // Also add the padding here
        val x = target.x
        val y = target.y
        val right = target.right + 1 + padding
        val bottom = target.bottom + 1 + padding

        var targetWithPadding: IntegerRectangle? = null
        if (padding == 0) {
            targetWithPadding = target
        }

        for (i in areas.size - 1 downTo 0) {
            val area = areas[i]
            if (!(x >= area.right || right <= area.x || y >= area.bottom || bottom <= area.y)) {
                if (targetWithPadding == null) {
                    targetWithPadding = allocateRectangle(target.x, target.y, target.width + padding, target.height + padding)
                }

                generateDividedAreas(targetWithPadding, area, results)
                val topOfStack = areas.removeLast()
                if (i < areas.size) {
                    // Move the one on the top to the freed position
                    areas[i] = topOfStack
                }
            }
        }

        if (targetWithPadding != null && targetWithPadding !== target) {
            freeRectangle(targetWithPadding)
        }

        filterSelfSubAreas(results)
    }

    /**
     * Divides the area into new sub areas around the divider.
     * @param divider rectangle that intersects the area
     * @param area rectangle to be divided into sub areas around the divider
     * @param results list for the new sub areas around the divider
     */
    private fun generateDividedAreas(divider: IntegerRectangle, area: IntegerRectangle, results: MutableList<IntegerRectangle>) {
        var count = 0

        val rightDelta = area.right - divider.right
        if (rightDelta > 0) {
            results.add(allocateRectangle(divider.right, area.y, rightDelta, area.height))
            count++
        }

        val leftDelta = divider.x - area.x
        if (leftDelta > 0) {
            results.add(allocateRectangle(area.x, area.y, leftDelta, area.height))
            count++
        }

        val bottomDelta = area.bottom - divider.bottom
        if (bottomDelta > 0) {
            results.add(allocateRectangle(area.x, divider.bottom, area.width, bottomDelta))
            count++
        }

        val topDelta = divider.y - area.y
        if (topDelta > 0) {
            results.add(allocateRectangle(area.x, area.y, area.width, topDelta))
            count++
        }

        if (count == 0 && (divider.width < area.width || divider.height < area.height)) {
            // Only touching the area, store the area itself
            results.add(area)
        } else {
            freeRectangle(area)
        }
    }

    /**
     * Removes rectangles from filteredAreas that are sub rectangles of any rectangle in areas.
     * @param areas rectangles from which the filtering is performed
     */
    private fun filterSelfSubAreas(areas: MutableList<IntegerRectangle>) {
        for (i in areas.size -1 downTo 0) {
            val filtered = areas[i]
            for (j in areas.size - 1 downTo 0) {
                if (i != j) {
                    val area = areas[j]
                    if (filtered.x >= area.x && filtered.y >= area.y && filtered.right <= area.right && filtered.bottom <= area.bottom) {
                        freeRectangle(filtered)
                        val topOfStack = areas.removeLast()
                        if (i < areas.size) {
                            // Move the one on the top to the freed position
                            areas[i] = topOfStack
                        }
                        break
                    }
                }
            }
        }
    }

    private fun allocateRectangle(x: Int, y: Int, width: Int, height: Int): IntegerRectangle {
        if (rectangleStack.isNotEmpty()) {
            val rect = rectangleStack.removeLast()
            rect.x = x
            rect.y = y
            rect.width = width
            rect.height = height
            return rect
        }
        return IntegerRectangle(x, y, width, height)
    }

    private fun allocateSize(width: Int, height: Int, id: String): SortableSize {
        if (sortableSizeStack.isNotEmpty()) {
            val size = sortableSizeStack.removeLast()
            size.width = width
            size.height = height
            size.id = id
            return size
        }
        return SortableSize(width, height, id)
    }

    private fun freeRectangle(rect: IntegerRectangle) {
        rectangleStack.add(rect)
    }

    private fun freeSize(size: SortableSize) {
        sortableSizeStack.add(size)
    }
}