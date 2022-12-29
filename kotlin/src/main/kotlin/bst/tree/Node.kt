package bst.tree

class Node(var value: Int, var isTransit: Boolean = false) {
    var left: Node? = null
    var right:Node? = null

    fun isLeaf():Boolean {
        return left === null && right === null
    }

    fun hasTwoChildren(): Boolean {
        return left !== null && right !== null
    }

    fun hasOneChild(): Boolean {
        return (left !== null && right === null) || (right !== null && left === null)
    }

    override fun toString(): String {
        return "Node(value=$value, isTransit=$isTransit, leftVal=${left?.value}, rightVal=${right?.value})"
    }

    fun deepToString(): String {
        return "Node(value=$value, isTransit=$isTransit, left=${left?.deepToString()}, right=${right?.deepToString()})"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Node

        if (value != other.value) return false
        if (isTransit != other.isTransit) return false
        if (left != other.left) return false
        if (right != other.right) return false

        return true
    }

    override fun hashCode(): Int {
        var result = value
        result = 31 * result + isTransit.hashCode()
        result = 31 * result + (left?.hashCode() ?: 0)
        result = 31 * result + (right?.hashCode() ?: 0)
        return result
    }
}

