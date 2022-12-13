package bfs

import java.util.Optional

class SyncBfs(private val size: Int) {
    private val visitedNodes = BooleanArray(size * size * size)
    private val childrenProvider = ChildrenProvider(size)

    fun allVisited(): Boolean {
        return Optional.ofNullable(visitedNodes.find { false }).orElse(false)
    }

    fun bfs(startIndex: Int): IntArray {
        val result = IntArray(size * size * size) { -1 }
        val queue = ArrayDeque<Int>()
        result[startIndex] = 0
        queue.addLast(startIndex)
        visitedNodes[startIndex] = true
        while (queue.isNotEmpty()) {
            val nodeIndex = queue.removeFirst()
            val currentDistance = result[nodeIndex]
            val nodeNeighbors = childrenProvider.getNodeChildren(nodeIndex)
            for (childIndex in nodeNeighbors) {
                if (!visitedNodes[childIndex]) {
                    queue.add(childIndex)
                    visitedNodes[childIndex] = true
                    result[childIndex] = currentDistance + 1
                }
            }
        }
        return result
    }
}