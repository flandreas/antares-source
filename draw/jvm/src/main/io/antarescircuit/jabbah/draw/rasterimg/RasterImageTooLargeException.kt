package io.antarescircuit.jabbah.draw.rasterimg

import java.io.IOException

/** An export that exceeds the raster or heap capacity; callers can offer a smaller image. */
class RasterImageTooLargeException(
	val width: Int,
	val height: Int,
	cause: OutOfMemoryError? = null,
) : IOException("Cannot export ${width}x$height pixels. Reduce image dimensions or resolution.", cause)
