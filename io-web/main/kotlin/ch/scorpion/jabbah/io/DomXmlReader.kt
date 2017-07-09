package ch.scorpion.jabbah.io

import ch.scorpion.jabbah.base.collection.Stack
import ch.scorpion.jabbah.base.loggerFor
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.get
import org.w3c.dom.parsing.DOMParser

/**
 * An [XmlReader] for reading W3C XML DOM documents.
 */
class DomXmlReader(document: Document) : XmlReader {

    constructor(str: String): this(DOMParser().parseFromString(str, "application/xml"))

    private val LOG by loggerFor(this)
    private val stack = Stack<Element>()

    init {
        stack.push(document.documentElement!!)
    }


    /** ---- [XmlReader] interface */

    override fun getName(): String {
        LOG.debug("getName '${stack.peek().tagName}'")
        return stack.peek().tagName
    }

    override fun getAttributeValue(name: String): String {
        return stack.peek().getAttribute(name)!!
    }

    override fun hasAttribute(name: String): Boolean {
        return stack.peek().getAttribute(name) != null
    }

    override fun hasElement(name: String): Boolean {
        return stack.peek().getElementsByTagName(name).length > 0
    }

    override fun getElementsCount(): Int {
        return stack.peek().childElementCount
    }

    override fun descend(name: String) {
        LOG.debug("descend to '$name'")
        stack.push(stack.peek().getElementsByTagName(name)[0]!!)
    }

    override fun descend(index: Int) {
        LOG.debug("descend to index $index of '${stack.peek().tagName}'")
        stack.push(stack.peek().children.get(index - 1)!!)
    }

    override fun ascend() {
        LOG.debug("ascend from '${stack.peek().tagName}'")
        stack.pop()
    }
}