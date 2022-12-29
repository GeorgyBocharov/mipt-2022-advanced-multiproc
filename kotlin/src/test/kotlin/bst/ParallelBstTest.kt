package bst

import bst.OperationType.*
import org.junit.jupiter.api.Test

import bst.tree.LockedNode
import bst.tree.ParallelBst
import bst.tree.SeqBst
import org.assertj.core.api.Assertions.assertThat

class ParallelBstTest {
    private val executor = OperationExecutor()

    @Test
    fun traverseWithoutTransitTest() {
        val tree = nonTransitTree()

        assertThat(tree.traverse(4)).isEqualTo(Triple(null, tree.root, tree.root.right))
        assertThat(tree.traverse(2)).isEqualTo(Triple( tree.root, tree.root.right, tree.root.right?.left))
        assertThat(tree.traverse(6)).isEqualTo(Triple( tree.root, tree.root.right, tree.root.right?.right))
        assertThat(tree.traverse(1)).isEqualTo(Triple(tree.root.right, tree.root.right?.left, tree.root.right?.left?.left))
    }

    @Test
    fun traverseWithTransitTest() {
        val tree = transitTree()

        assertThat(tree.traverse(4)).isEqualTo(Triple(null, tree.root, tree.root.right))
        assertThat(tree.traverse(2)).isEqualTo(Triple(tree.root, tree.root.right, tree.root.right?.left))
        assertThat(tree.traverse(6)).isEqualTo(Triple(tree.root, tree.root.right, tree.root.right?.right))
        assertThat(tree.traverse(1)).isEqualTo(Triple(tree.root.right, tree.root.right?.left, tree.root.right?.left?.left))
    }

    @Test
    fun multipleInserts() {
        val operations = arrayListOf(
            Operation(4, INSERT),
            Operation(2, INSERT),
            Operation(6, INSERT),
            Operation(6, INSERT),
            Operation(7, INSERT),
            Operation(1, INSERT),
            Operation(3, INSERT)
        )
        val res = executor.execute(ParallelBst(), operations)

        assertThat(res.first).isEqualTo(nonTransitTree())
        assertThat(res.second).containsExactly(arrayOf(true, true, true, false, true, true, true))
    }

    @Test
    fun multipleContainsForNonTransit() {
        val operations = arrayListOf(
            Operation(4, CONTAINS),
            Operation(2, CONTAINS),
            Operation(10, CONTAINS),
            Operation(100, CONTAINS),
            Operation(1, CONTAINS)
        )
        val res = executor.execute(nonTransitTree(), operations)

        assertThat(res.first).isEqualTo(nonTransitTree())
        assertThat(res.second).containsExactly(arrayOf(true, true, false, false, true))
    }

    @Test
    fun multipleContainsForTransit() {
        val operations = arrayListOf(
            Operation(4, CONTAINS),
            Operation(2, CONTAINS),
            Operation(6, CONTAINS),
            Operation(7, CONTAINS),
            Operation(1, CONTAINS)
        )
        val res = executor.execute(transitTree(), operations)

        assertThat(res.first).isEqualTo(transitTree())
        assertThat(res.second).containsExactly(arrayOf(true, false, false, true, true))
    }

    @Test
    fun removeLeaves() {
        val operations = arrayListOf(
            Operation(1, REMOVE),
            Operation(3, REMOVE),
            Operation(2, REMOVE),
            Operation(100, REMOVE),
            Operation(7, REMOVE),
            Operation(10, REMOVE),
            Operation(6, REMOVE),
            Operation(4, REMOVE)
        )
        val res = executor.execute(nonTransitTree(), operations)

        assertThat(res.first).isEqualTo(ParallelBst(LockedNode(-1)))
        assertThat(res.second).containsExactly(arrayOf(true, true, true, false, true, false, true, true))
    }

    @Test
    fun removeLockedNodeWithTwoChildren() {
        val operations = arrayListOf(
            Operation(2, REMOVE)
        )
        val res = executor.execute(nonTransitTree(), operations)

        val expectedTree = nonTransitTree()
        expectedTree.root.right?.left?.isTransit = true
        assertThat(res.first).isEqualTo(expectedTree)
        assertThat(res.second).containsExactly(arrayOf(true))
    }

    @Test
    fun removeLockedNodeWithSingleChild() {
        val operations = arrayListOf(
            Operation(6, REMOVE)
        )
        val res = executor.execute(nonTransitTree(), operations)

        val expectedTree = nonTransitTree()
        val orphan = expectedTree.root.right?.right?.right
        expectedTree.root.right?.right = orphan
        assertThat(res.first).isEqualTo(expectedTree)
        assertThat(res.second).containsExactly(arrayOf(true))
    }

    @Test
    fun removeLeavesOfTransitTree() {
        val operations = arrayListOf(
            Operation(7, REMOVE),
            Operation(1, REMOVE),
            Operation(4, REMOVE)
        )
        val res = executor.execute(transitTree(), operations)

        assertThat(res.first).isEqualTo(ParallelBst(LockedNode(-1)))
        assertThat(res.second).containsExactly(arrayOf(true, true, true))
    }

    private fun nonTransitTree(): ParallelBst {
        val fakeRoot = LockedNode(-1)
        val root = LockedNode(4)
        val left = LockedNode(2)
        val right = LockedNode(6)
        val leftLeft = LockedNode(1)
        val leftRight = LockedNode(3)
        val rightRight = LockedNode(7)
        fakeRoot.right = root
        root.left = left
        root.right = right
        left.left = leftLeft
        left.right = leftRight
        right.right = rightRight
        return ParallelBst(fakeRoot)
    }

    private fun transitTree(): ParallelBst {
        val fakeRoot = LockedNode(-1)
        val root = LockedNode(4)
        val left = LockedNode(2, true)
        val right = LockedNode(6, true)
        val leftLeft = LockedNode(1)
        val rightRight = LockedNode(7)
        fakeRoot.right = root
        root.left = left
        root.right = right
        left.left = leftLeft
        right.right = rightRight
        return ParallelBst(fakeRoot)
    }
}