package ch.scorpion.jabbah.io

import ch.scorpion.jabbah.base.geom.Point2D

/**
 * Writes [Storable]s to persistent store.
 *
 * Extends [GlobalIdentityProvider] in order to allow [Storable]s that write themselves to persistent store
 * can retrieve identities of other [Storable] they reference.
 */
interface StoreWriter : GlobalIdentityProvider {

    /**
     * Writes the specified [Storable].
     * @param storable the [Storable] to write.
     */
    fun writeStorable(storable: Storable)

    /**
     * Writes a [Storable] under the specified name.
     * @param name the name of the [Storable].
     * @param storable the [Storable] to store.
     */
    fun writeStorable(name: String, storable: Storable)

    /**
     * Writes the [Storable]s of the specified [Iterator].
     * @param name the name of the attribute.
     * @param iterator the [Iterator] that iterates over the [Storable]s.
     */
    fun writeStorables(name: String, iterator: Iterator<Storable>)

    /**
     * Writes the specified [Int] attribute under the given name.
     * @param name the name of the attribute.
     * @param value the value of the attribute.
     */
    fun writeInt(name: String, value: Int)

    /**
     * Writes the specified `double` attribute under the given name.
     * @param name the name of the attribute.
     * @param value the value of the attribute.
     */
    fun writeDouble(name: String, value: Double)

    /**
     * Writes the specified [String] attribute under the given name.
     * @param name the name of the attribute.
     * @param value the value of the attribute.
     */
    fun writeString(name: String, value: String)

	/**
	 * Writes the specified [String] if it is not `null` or empty.
	 * @param name the name of the attribute.
	 * @param value the value of the attribute.
	 */
	fun writeOptionalString(name: String, value: String?)

    /**
     * Writes the specified [Boolean] attribute under the given name.
     * @param name the name of the attribute.
     * @param value the value of the attribute.
     */
    fun writeBoolean(name: String, value: Boolean)

    /**
     * Writes the specified [Long] attribute under the given name.
     * @param name the name of the attribute.
     * @param value the value of the attribute.
     */
    fun writeLong(name: String, value: Long)

    fun writeULong(name: String, value: ULong)

    /** Writes a list of [Point2D] as an attribute with the given name.*/
    fun writePoints(name: String, points: List<Point2D>)

    /**
     * Writes a list of [Point2D] in a new element as an attribute with the given name.
     * This method exists for reasons of backward compatibility with older versions in which [Point2D] was [Storable],
     * which is not desired any more due to layering constraints.
     */
    fun writePoints(outerElem: String, innerElem: String, attribute: String, points: List<Point2D>)

    /**
     * Writes a single [Point2D] as an element with the specified name.
     * This method exists for reasons of backward compatibility with older versions in which [Point2D] was [Storable],
     * which is not desired any more due to layering constraints.
     */
    fun writePoint(name: String, point: Point2D)

	/** Writes a list of [Int] as an attribute with the given name.*/
	fun writeIntegers(name: String, integers: List<Int>)
}