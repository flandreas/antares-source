package ch.scorpion.jabbah.io

class ReferenceResolverProxy(
    val proxy: ReferenceResolver,
    val backend: ReferenceResolver
) : ReferenceResolver {

    override fun addStorable(globalId: Int, storable: Storable) {
        backend.addStorable(globalId, storable)
    }

    override fun getStorable(globalId: Int): Storable? {
        return proxy.getStorable(globalId) ?: backend.getStorable(globalId)
    }

    override fun requestResolution(requester: Storable, reference: Reference) {
        backend.requestResolution(requester, reference)
    }

    override fun resolveReferences() {
        resolveReferences(this)
    }

    override fun resolveReferences(referenceResolver: ReferenceResolver) {
        backend.resolveReferences(referenceResolver)
    }
}