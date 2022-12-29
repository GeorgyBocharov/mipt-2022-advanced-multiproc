package bst

import bst.tree.Bst

class Operation(private val key: Int, private val operation: OperationType) {
    fun execute(bst: Bst):Boolean {
        return operation.apply(bst, key)
    }

    override fun toString(): String {
        return "Operation(key=$key, operation=$operation)"
    }
}

enum class OperationType(private val operation: (bst: Bst, key: Int) -> Boolean) {
    INSERT((Bst::insert)),
    REMOVE(Bst::remove),
    CONTAINS(Bst::contains);

    fun apply(bst: Bst, key: Int):Boolean {
        return operation(bst, key)
    }
}