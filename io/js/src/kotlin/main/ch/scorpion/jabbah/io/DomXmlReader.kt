package ch.scorpion.jabbah.io

import ch.scorpion.jabbah.base.collection.Stack
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.get
import org.w3c.dom.parsing.DOMParser

/**
 * An [XmlReader] for reading W3C XML DOM documents.
 */
class DomXmlReader(document: Document) : XmlReader {

    constructor(str: String): this(DOMParser().parseFromString(str, "application/xml"))

    private val stack = Stack<Element>()

    init {
        stack.push(document.documentElement!!)
    }

    /** ---- [XmlReader] interface */

    override fun getName(): String {
        return stack.peek().tagName
    }

    override fun getAttributeValue(name: String): String {
        return stack.peek().getAttribute(name)!!
    }

    override fun hasAttribute(name: String): Boolean =
        stack.peek().getAttribute(name) != null

    override fun hasElement(name: String): Boolean =
        getChildElement(name) != null

    private fun getChildElement(name: String): Element? {
        val children = stack.peek().childNodes
        for (i in 0 until children.length) {
            if (children[i]!!.nodeName.equals(name, ignoreCase = true)) {
                return children[i] as Element?
            }
        }
        return null
    }

    override fun getElementNames(): Set<String> {
        val names = mutableSetOf<String>()
        val children = stack.peek().childNodes
        for (i in 0 until children.length) {
            names.add(children[i]!!.nodeName)
        }
        return names
    }

    override fun getElementsCount(): Int {
        return stack.peek().childElementCount
    }

    override fun descend(name: String) {
        stack.push(getChildElement(name)!!)
    }

    override fun descend(index: Int) {
        stack.push(stack.peek().children[index - 1]!!)
    }

    override fun ascend() {
        stack.pop()
    }

    override fun getText(name: String): String = getChildElement(name)!!.textContent!!
}