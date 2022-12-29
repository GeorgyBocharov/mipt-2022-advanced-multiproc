package bst.tree

class SeqBst(val root:Node = Node(-1, false)):Bst {

    override fun contains(value: Int): Boolean {
        val nodes = traverse(value)
        val curr = nodes.third
        return curr !== null && !curr.isTransit
    }

    override fun insert(value: Int): Boolean {
        val nodes = traverse(value)
        val curr = nodes.third
        if (curr !== null) {
            return if (!curr.isTransit) {
                false
            } else {
                curr.isTransit = false
                true
            }
        }
        val newNode = Node(value, false)
        val prev = nodes.second!!
        if (value < prev.value) {
            prev.left = newNode
        } else {
            prev.right = newNode
        }
        return true
    }

    override fun remove(value: Int): Boolean {
        val nodes = traverse(value)
        val curr = nodes.third
        if (curr === null || curr.isTransit) {
            return false
        }

        val prev = nodes.second!!
        if (curr.hasTwoChildren()) {
            curr.isTransit = true
        } else if (curr.hasOneChild()) {
            deleteIfOneChild(curr, prev)
        } else {
            deleteIfLeaf(nodes.first, prev, curr)
        }
        return true
    }

    fun traverse(value: Int): Triple<Node?, Node?, Node?> {
        var gprev:Node? = null
        var prev:Node? = null
        var curr:Node? = root

        while (curr !== null && curr.value != value) {
            gprev = prev
            prev = curr
            if (value < curr.value) {
                curr = curr.left
            } else {
                curr = curr.right
            }
        }
        return Triple(gprev, prev, curr)
    }



    private fun deleteIfLeaf(
        gprev: Node?,
        prev: Node,
        curr: Node,
    ) {
        if (prev.isTransit) {
            val child = if (prev.left == curr) {
                prev.right
            } else {
                prev.left
            }
            if (gprev!!.left == prev) {
                gprev.left = child
            } else {
                gprev.right = child
            }
        } else {
            if (prev.left == curr) {
                prev.left = null
            } else {
                prev.right = null
            }
        }
    }

    private fun deleteIfOneChild(curr: Node, prev: Node) {
        val child = if (curr.left !== null) {
            curr.left!!
        } else {
            curr.right!!
        }
        if (curr.value < prev.value) {
            prev.left = child
        } else {
            prev.right = child
        }
    }

    override fun toString(): String {
        return "SeqBst(root=${root.deepToString()})"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SeqBst

        if (root != other.root) return false

        return true
    }

    override fun hashCode(): Int {
        return root.hashCode()
    }

}