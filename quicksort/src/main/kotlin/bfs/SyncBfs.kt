package bfs


class SyncBfs(size: Int) {
    private val visitedNodes = BooleanArray(size * size * size) {false}
    private val childrenProvider = ChildrenProvider(size)

    fun bfs(startIndex: Int, result:IntArray) {
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
                    queue.addLast(childIndex)
                    visitedNodes[childIndex] = true
                    result[childIndex] = currentDistance + 1
                }
            }
        }
    }
}