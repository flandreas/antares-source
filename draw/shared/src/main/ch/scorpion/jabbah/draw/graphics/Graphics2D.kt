package ch.scorpion.jabbah.draw.graphics

import ch.scorpion.jabbah.base.geom.AffineTransform
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.Shape

/**
 * Represents a target-specific context for graphical operations.
 */
interface Graphics2D {

    /** The property for accessing the current [AffineTransform]. The getter returns a copy of it.*/
    var transform: AffineTransform

    var color: Color

    var stroke: Stroke

    var font: Font

    var antialiasing: Boolean

    val supportClipping: Boolean

	/** Returns the accumulated rotation angle.*/
	val rotationAngle: Double

    /* Saves the current state of this [Graphics2D] onto a stack.*/
    fun save()

    /** Restores the previously saved state of this [Graphics2D] from the stack.*/
    fun restore()

    /** Concatenates the current [AffineTransform] with a scaling transformation.*/
    fun scale(sx: Double, sy: Double)

    /** Concatenates the current [AffineTransform] with a translation transformation.*/
    fun translate(tx: Double, ty: Double)

    /** Concatenates the current [AffineTransform] with a rotation transformation according to the given angle in radians.*/
    fun rotate(theta: Double)

    /** Draws a line between the points (x1, y1) and (x2, y2) in this [Graphics2D]'s current coordinate system.*/
    fun drawLine(x1: Int, y1: Int, x2: Int, y2: Int)

    /** Draws a line between the points (x1, y1) and (x2, y2) in this [Graphics2D]'s current coordinate system.*/
    fun drawLine(x1: Double, y1: Double, x2: Double, y2: Double)

    fun drawRect(x: Int, y: Int, w: Int, h: Int)

    fun drawRect(x: Double, y: Double, w: Double, h: Double)

    fun drawRoundRect(x: Int, y: Int, w: Int, h: Int, arcW: Int, arcH: Int)

    fun fillRect(x: Int, y: Int, w: Int, h: Int)

    fun fillRect(x: Double, y: Double, w: Double, h: Double)

    fun fillRoundRect(x: Int, y: Int, w: Int, h: Int, arcW: Int, arcH: Int)

    /** Draws an oval within the rectangle defined by the upper-left corner (x,y) and the given width and height.*/
    fun drawOval(x: Int, y: Int, w: Int, h: Int)

    /** Draws an oval within the rectangle defined by the upper-left corner (x,y) and the given width and height.*/
    fun drawOval(x: Double, y: Double, w: Double, h: Double)

    fun fillOval(x: Int, y: Int, w: Int, h: Int)

    fun fillOval(x: Double, y: Double, w: Double, h: Double)

    fun drawDot(x: Int, y: Int)

    fun drawString(s: String, x: Int, y: Int)

    /** Draws a text that can even contain HTML in fixed width block. */
    fun drawText(s: String, x: Int, y: Int, w: Int)

    /** Draws an [Image] at the upper-left corner (x,y).*/
    fun drawImage(image: Image, x: Int, y: Int)

    /** Draws the specified [Shape] with the current [Color].*/
    fun draw(shape: Shape)

    /** Fills the specified [Shape] with the current [Color].*/
    fun fill(shape: Shape)

    /** Returns the bounding rectangle of the current clipping area. */
    fun getClipBounds(): Rectangle2D

    /**
     * Returns the bounding rectangle of the current clipping area and writes it into the specified [Rectangle2D].
     * @return [r]
     */
    fun getClipBounds(r: Rectangle2D): Rectangle2D

}

