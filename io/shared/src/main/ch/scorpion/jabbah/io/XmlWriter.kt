package ch.scorpion.jabbah.io

/** A writer for XML DOM objects. */
interface XmlWriter {

    /** Determines whether the current element is the root element.*/
    fun isRoot(): Boolean

    /** Adds an element with the specified name and descend to it, i.e. makes it the current element.*/
    fun addElementAndDescend(name: String)

    /** Ascends from the current element, i.e. makes its parent the current element.*/
    fun ascend()

    /** Writes the DOM tree to the external storage.*/
    fun flush()

    fun setAttributeValue(name: String, value: String)
//
//
//    fun writeString(name: String, value: String)
//
//    fun writeInt(name: String, value: Int)
//
//    fun writeDouble(name: String, value: Double)
//
//    fun writeBoolean(name: String, value: Boolean)
//
//    fun writeLong(name: String, value: Long)
}