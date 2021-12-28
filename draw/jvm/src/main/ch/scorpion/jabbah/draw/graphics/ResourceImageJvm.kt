package ch.scorpion.jabbah.draw.graphics

import ch.scorpion.jabbah.base.ui.UI
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.swing.UiUtil
import javax.swing.ImageIcon

/**
 * An [Image] implementation whose data is loaded from  resources.
 * @param path the path from which the image data are loaded
 */
class ResourceImageJvm(val path: String) : Image {

    companion object {
        private val LOG by logger(ResourceImageJvm::class)

	    fun themedImage(path: String): Image {
		    if (UI.isDark) {
			    return try {
				    ResourceImageJvm(UiUtil.darkImagePath(path))
			    } catch (t: Throwable) {
				    ResourceImageJvm(path)
			    }
		    }
		    return ResourceImageJvm(path)
	    }
    }

    val imageIcon: ImageIcon

    init {
        val resource = ResourceImageJvm::class.java.getResource(path)
        if (resource == null) {
            LOG.error("Image '$path' not found")
            throw IllegalArgumentException("Image '$path' not found")
        }
        imageIcon = ImageIcon(resource)
    }

    override val width: Int get() = imageIcon.iconWidth

    override val height: Int get() = imageIcon.iconHeight
}
