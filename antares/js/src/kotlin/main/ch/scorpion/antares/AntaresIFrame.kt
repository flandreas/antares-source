package ch.scorpion.antares

import ch.scorpion.jabbah.graph.project.AkrabApiError
import ch.scorpion.jabbah.graph.project.AkrabApiException
import kotlinx.browser.window
import org.w3c.dom.url.URLSearchParams

/**
 * Enables displaying an [AntaresSingleCircuitViewerJs] within an HTML iframe.
 * Extracts project and circuit UUIDs from URL query parameters.
 */
@Suppress("MemberVisibilityCanBePrivate", "unused") // JS library
@JsExport
object AntaresIFrame {

    private const val PROJECT_UUID_PARAM = "project"
    private const val OWNER_UUID_PARAM = "owner"
    private const val CIRCUIT_UUID_PARAM = "circuit"
    private const val THEME_PARAM = "theme"

    /**
     * Initializes an Antares JS application and prepares displaying a circuit of a particular project within
     * the canvas with the given ID.
     * Should only be called once during the lifetime of the Antares JS application.
     *
     * @throws AkrabApiException if parameters are missing, or if bootstrapping the application failed,
     * or if the referenced circuit doesn't exist
     */
    fun initialize(canvasId: String = "canvas"): AntaresSingleCircuitViewerJs {
        val params = URLSearchParams(window.location.search)
        val projectUuid = params.get(PROJECT_UUID_PARAM)
        val ownerUuid = params.get(OWNER_UUID_PARAM)
        val metaGraphUuid = params.get(CIRCUIT_UUID_PARAM)
        val themeName = params.get(THEME_PARAM)

        if (projectUuid == null) {
            throw AkrabApiException(AkrabApiError(AkrabApiError.TYPE_ERROR, "Missing parameter '$PROJECT_UUID_PARAM'"))
        }
        if (ownerUuid == null) {
            throw AkrabApiException(AkrabApiError(AkrabApiError.TYPE_ERROR, "Missing parameter '$$OWNER_UUID_PARAM'"))
        }
        if (metaGraphUuid == null) {
            throw AkrabApiException(AkrabApiError(AkrabApiError.TYPE_ERROR, "Missing parameter '$$$CIRCUIT_UUID_PARAM'"))
        }

        TODO()
        //return AntaresSingleCircuitViewerJs(ownerUuid, projectUuid, metaGraphUuid, null, themeName = themeName)
    }
}