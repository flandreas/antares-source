package ch.scorpion.jabbah.io

import ch.scorpion.jabbah.base.geom.Point2D

/**
 * Reads [Storable]s from persistent store.
 */
interface StoreReader {

    fun requestResolution(requester: Storable, reference: Reference)

    fun hasAttribute(name: String): Boolean

    fun hasElement(name: String): Boolean

    fun getStorable(id: Int): Storable

    /** Reads the toplevel [Storable].*/
    fun readStorable(): Storable

    /**
     * Reads the next [Storable] with the specified name.
     * @param name the name of the [Storable].
     * @return the read [Storable]
     */
    fun readStorable(name: String): Storable

	/**
	 * Reads the next [Storable] at the specified path of names.
	 *
	 * Example:
	 * <pre>
	 * <document _id='1'>
	 *     <children>
	 *         <a/>
	 *         <b/>
	 *     </children>
	 * </docment>
     * </pre>
	 *
	 * Calling readStorable(listOf("children", "b")) reads only an instance of "b", but not "a".
	 */
	fun readStorable(names: List<String>): Storable

    /**
     * Reads a group of [Storable]s with the specified name.
     * @param name the name of the [Storable] group.
     * @return an [Iterator] over the read [Storable]s.
     */
    fun readStorables(name: String): List<Storable>

    /** Reads the next `int` attribute with the given name.*/
    fun readInt(name: String): Int

    /** Reads the next `double` attribute with the given name.*/
    fun readDouble(name: String): Double

    /** Reads the next [String] attribute with the given name.*/
    fun readString(name: String): String

    fun readOptionalString(name: String): String?

    /** Reads the next [Boolean] attribute with the given name.*/
    fun readBoolean(name: String): Boolean

    /** Reads the next [Long] attribute with the given name.*/
    fun readLong(name: String): Long

    /** Reads a list of [Point2D] from the attribute with the given name.*/
    fun readPoints(name: String): List<Point2D>

    /**
     * Reads a single [Point2D] from an element and its attribute with the specified name.
     * This method exists for reasons of backward compatibility with older versions in which [Point2D] was [Storable],
     * which is not desired any more due to layering constraints.
     */
    fun readPoints(outerElem: String, innerElem: String, attribute: String): List<Point2D>

    /**
     * Reads a single [Point2D] from an element with the specified name.
     * This method exists for reasons of backward compatibility with older versions in which [Point2D] was [Storable],
     * which is not desired any more due to layering constraints.
     */
    fun readPoint(name: String): Point2D
}