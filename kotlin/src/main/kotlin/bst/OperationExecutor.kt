package bst

import bst.tree.Bst

class OperationExecutor {
    fun execute(bst: Bst, operations: List<Operation>):Pair<Bst, BooleanArray> {
        val res = BooleanArray(operations.size)
        for (i in operations.indices) {
            res[i] = operations[i].execute(bst)
        }
        return Pair(bst, res)
    }
}