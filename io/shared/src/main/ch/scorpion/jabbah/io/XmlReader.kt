package ch.scorpion.jabbah.io

/** A reader for XML DOM objects.*/
interface XmlReader {

    /** Returns the name of the current XML element.*/
    fun getName(): String

    /** Returns the value of the current element's attribute with the specified name.*/
    fun getAttributeValue(name: String): String

    /** Checks whether the current element has an attribute with the specified name.*/
    fun hasAttribute(name: String): Boolean

    /** Checks whether the current element has a child element with the specified name.*/
    fun hasElement(name: String): Boolean

    /** Returns the number of child elements of the current element.*/
    fun getElementsCount(): Int

    /** Descends to the first element with the specified name and makes it the current one. */
    fun descend(name: String)

    /**
     * Descends to the child element with the specified index, starting with 1
     * @param index the index of the element to descend to. If missing, descend to the first (and often only) element
     */
    fun descend(index: Int = 1)

    /** Ascends from the current element and makes its parent the new current element.*/
    fun ascend()
}