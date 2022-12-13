package bfs

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicIntegerArray

class AsyncBfs(private val size: Int) {
    private val visitedNodes = AtomicIntegerArray(size)
    private val childrenProvider = ChildrenProvider(size)
    private val result = IntArray(size * size * size) { -1 }

    suspend fun bfs_frontier(frontier: ArrayList<Int>) : Unit = coroutineScope  {


        var total_degree = 0
        val degreeArr = IntArray(frontier.size)
        for (i in frontier.indices) {
            launch {
                addDegree(frontier[i], i, degreeArr)
            }
        }
        // todo implement scan to calculate total_degree
        val nextFrontier = IntArray(total_degree)
        for (i in frontier.indices) {
            launch {
                addChildren(frontier[i], degreeArr[i], nextFrontier)
            }
        }
        // todo implement filter for nextFrontier & updated frontier with elements from nextFrontier
    }

    private suspend fun addDegree(nodeIndex: Int, i: Int, degreeArr: IntArray): Unit = coroutineScope  {
        degreeArr[i] = childrenProvider.getDegree(nodeIndex)
    }

    private suspend fun addChildren(nodeIndex: Int, offset:Int, frontier: IntArray): Unit = coroutineScope {
        var ctr = 0
        for (childIndex in childrenProvider.getNodeChildren(nodeIndex)) {
            if (visitedNodes.compareAndSet(childIndex, 0 , 1)) {
                result[childIndex] = result[nodeIndex] + 1
                frontier[offset + ctr] = childIndex
                ctr++
            }
        }
    }
}