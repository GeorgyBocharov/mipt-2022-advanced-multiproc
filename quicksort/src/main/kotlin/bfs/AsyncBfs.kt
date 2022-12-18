package bfs

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.atomic.AtomicIntegerArray
import kotlin.math.ceil
import kotlin.math.log2
import kotlin.math.pow


class AsyncBfs(size: Int, private val parallelLimit:Int) {
    private val visitedNodes = AtomicIntegerArray(size * size * size)
    private val childrenProvider = ChildrenProvider(size)
    private val filterScan = IntArray(size * size * 6)

    suspend fun bfs(start: Int, result: IntArray, scope: CoroutineScope) {
        var frontier = IntArray(1)
        frontier[0] = start
        result[start] = 0
        visitedNodes.set(start, 1)
        var frontierSize = frontier.size
        while (frontierSize > 0) {
//            println("CurrentFrontier: ${frontier.contentToString()}, $frontierSize")
            val degreeArr = IntArray(frontierSize)
            scope.launch {
                runSplit(0, frontierSize, 1, parallelLimit, scope) { i ->
                    degreeArr[i] = childrenProvider.getDegree(frontier[i])
                }
            }.join()
//            println("Degrees: ${degreeArr.contentToString()}")
            val nextFrontier = withContext(scope.coroutineContext) { parallelScan(degreeArr, degreeArr.size, scope) }
//            println("nextFrontier: ${nextFrontier.contentToString()}")
//            println("Degrees prefix: ${degreeArr.contentToString()}")
            scope.launch {
                runSplit(0, frontierSize, 1, parallelLimit / 6, scope) { i ->
                    filterChildrenSync(frontier, i, result, nextFrontier, degreeArr)
                }
            }.join()
//            println("children: ${nextFrontier.contentToString()}")
            scope.launch {
                runSplit(0, nextFrontier.size, 1, parallelLimit, scope) { i ->
                    if (nextFrontier[i] != -1) {
                        filterScan[i] = 1
                    }
                }
            }.join()
//            println("mapRes: ${filterScan.contentToString()}")
            frontier = withContext(scope.coroutineContext) { parallelScan(filterScan, nextFrontier.size, scope) }
//            println("filterScan: ${filterScan.contentToString()}")
//            println("newFrontier: ${frontier.contentToString()}")
            frontierSize = frontier.size
            scope.launch {
                runSplit(0, nextFrontier.size - 1, 1, parallelLimit, scope) { i ->
                    if (filterScan[i] != filterScan[i + 1]) {
                        frontier[filterScan[i]] = nextFrontier[i]
                    }
                }
            }.join()
            if (filterScan[nextFrontier.size - 1] != frontierSize) {
                frontier[frontierSize - 1] = nextFrontier[nextFrontier.size - 1]
            }
//            println("frontierAfterFilter: ${frontier.contentToString()}")
            val remapRes = scope.launch {
                runSplit(0, nextFrontier.size, 1, parallelLimit, scope) { i ->
                    filterScan[i] = 0
                }
            }
            remapRes.join()
        }
    }

    private suspend fun parallelScan(x: IntArray, n: Int, scope: CoroutineScope): IntArray {
        val pad = 2.0.pow(ceil(log2(n.toDouble()))).toInt() - n
        scope.launch {
            scanForward(x, n, pad, scope)
        }.join()
        val nextFrontier = IntArray(x[n - 1]) { -1 }
        x[n - 1] = 0
        scope.launch {
            scanBackward(x, n, pad, scope)
        }.join()
        return nextFrontier
    }

    private suspend fun scanForward(x: IntArray, realSize: Int, pad: Int, scope: CoroutineScope) {
        val n = realSize + pad
        val upperBound = log2(n.toDouble()).toInt() - 1
        for (d in 0..upperBound) {
            val job = scope.launch {
//                println("Forward, thread: ${Thread.currentThread().id}")
                runSplit(0, n, 2.0.pow(d + 1).toInt(), parallelLimit, scope) { i ->
                    val exp = 2.0.pow(d).toInt()
                    incrementWithPad(x, i + exp * 2 - 1, pad, getWithPad(x, i + exp - 1, pad))
                }
            }
            job.join()
        }
    }

    private suspend fun scanBackward(x: IntArray, realSize: Int, pad: Int, scope: CoroutineScope) {
        val n = realSize + pad
        val upperBound = log2(n.toDouble()).toInt() - 1
        for (d in upperBound downTo 0) {
            val job = scope.launch {
//                println("Backward, thread: ${Thread.currentThread().id}")
                runSplit(0, n, 2.0.pow(d + 1).toInt(), parallelLimit, scope) { i ->
                    val exp = 2.0.pow(d).toInt()
                    val temp = getWithPad(x, i + exp - 1, pad)
                    setWithPad(x, i + exp - 1, pad, getWithPad(x, i + exp * 2 - 1, pad))
                    incrementWithPad(x, i + exp * 2 - 1, pad, temp)
                }
            }
            job.join()
        }
    }

    private fun getWithPad(x: IntArray, i: Int, pad: Int): Int {
        if (i < pad)
            return 0
        return x[i - pad]
    }

    private fun incrementWithPad(x: IntArray, i: Int, pad: Int, value: Int) {
        if (i >= pad)
            x[i - pad] += value
    }

    private fun setWithPad(x: IntArray, i: Int, pad: Int, value: Int) {
        if (i >= pad)
            x[i - pad] = value
    }

    private suspend fun runSplit(l: Int, r: Int, step: Int, limit: Int, scope: CoroutineScope, syncFun: (i: Int) -> Unit) {
        val actualLimit: Int = if ((r - l) / step.toDouble() < limit) {
            r - l
        } else {
            step * limit
        }
        val jobs = ArrayList<Job>()
        for (start in l until r step actualLimit) {
            val job = scope.launch {
                val end = Integer.min(actualLimit + start, r - l)
//                println("($start, $end), step: $step Thread ${Thread.currentThread().id}")
                for (i in start until end step (step)) {
                    syncFun(i)
                }
            }
            jobs.add(job)
        }
        for (job in jobs) {
            job.join()
        }
    }

    private fun filterChildrenSync(
        frontier: IntArray,
        i: Int,
        result: IntArray,
        nextFrontier: IntArray,
        degreeArr: IntArray
    ) {
        var ctr = 0
        val nodeIndex = frontier[i]
        val nodeChildren = childrenProvider.getNodeChildren(nodeIndex)
//        println("Children of node $nodeIndex: $nodeChildren")
        for (childIndex in nodeChildren) {
            if (visitedNodes.compareAndSet(childIndex, 0, 1)) {
                result[childIndex] = result[nodeIndex] + 1
                nextFrontier[degreeArr[i] + ctr] = childIndex
                ctr++
            }
        }
    }
}