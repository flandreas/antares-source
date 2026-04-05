package io.antarescircuit.jabbah.execution

import io.antarescircuit.jabbah.base.Action
import io.antarescircuit.jabbah.base.time.SystemSpeed
import io.antarescircuit.jabbah.execution.speed.SystemSpeedCategory
import io.antarescircuit.jabbah.execution.speed.CurrentSystemSpeedCategory
import kotlin.js.JsExport

/**
 * Simple interface that encapsulates [SystemSpeedCategory] and [SystemSpeed] functionality,
 * and provides access to execution control [Actions][Action].
 * Primarily used on the JS platform to avoid exporting the complex inner classes.
 */
@JsExport
interface ExecutionControlOutlet {

    /** Returns the name of the current [SystemSpeedCategory].*/
    val systemSpeedCategoryName: String

    /** The speed in the [CurrentSystemSpeedCategory].*/
    var currentSystemSpeed: Int

    val toggleApplicationModeAction: Action

    val singleStepModeAction: Action

    val pauseOrResumeAction: PauseOrResumeAction
}