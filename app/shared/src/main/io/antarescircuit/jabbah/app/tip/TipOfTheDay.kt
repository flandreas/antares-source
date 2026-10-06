package io.antarescircuit.jabbah.app.tip

/**
 * Data object representing a single "tip of the day".
 *
 * @property id the unique ID of the tip. Used for displaying only those the user has not seen yet.
 * @property title the (translated) text to be displayed as the title of the tip
 * @property content the (translated) text to be displayed as the content of the tip
 * @property imagePath the optional path to an image to be displayed alongside the tip
 */
data class TipOfTheDay(
    val id: Int,
    val title: String,
    val content: String,
    val imagePath: String? = null
)