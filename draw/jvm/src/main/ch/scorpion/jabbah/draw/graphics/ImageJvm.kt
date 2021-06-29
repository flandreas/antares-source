package ch.scorpion.jabbah.draw.graphics

import ch.scorpion.jabbah.base.ui.UI
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.swing.UiUtil
import javax.swing.ImageIcon

class ImageJvm(override val path: String) : Image {

    companion object {
        private val LOG by logger(ImageJvm::class)

	    fun themedImage(path: String): Image {
		    if (UI.isDark) {
			    return try {
				    ImageJvm(UiUtil.darkImagePath(path))
			    } catch (t: Throwable) {
				    ImageJvm(path)
			    }
		    }
		    return ImageJvm(path)
	    }
    }

    val imageIcon: ImageIcon

    init {
        val resource = ImageJvm::class.java.getResource(path)
        if (resource == null) {
            LOG.error("Image '$path' not found")
            throw IllegalArgumentException("Image '$path' not found")
        }
        imageIcon = ImageIcon(resource)
    }

    override val width: Int get() = imageIcon.iconWidth

    override val height: Int get() = imageIcon.iconHeight
}
