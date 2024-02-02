package ch.scorpion.jabbah.execution.speed

import ch.scorpion.jabbah.base.time.SystemSpeed
import kotlin.js.JsExport

/**
 * Simple interface that encapsulates [SystemSpeedCategory] and [SystemSpeed] functionality.
 * Primarily used on the JS platform to avoid exporting the complex inner classes.
 */
@JsExport
interface SystemSpeedOutlet {

    /** Returns the name of the current [SystemSpeedCategory].*/
    val systemSpeedCategoryName: String

    /** The speed in the [CurrentSystemSpeedCategory].*/
    var currentSystemSpeed: Int
}