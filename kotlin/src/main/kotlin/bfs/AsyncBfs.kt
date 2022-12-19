package bfs

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicIntegerArray
import kotlin.math.ceil
import kotlin.math.log2
import kotlin.math.pow


class AsyncBfs(size: Int, private val parallelLimit: Int, private val parallelScanLimit: Int) {
    private val visitedNodes = AtomicIntegerArray(size * size * size)
    private val childrenProvider = ChildrenProvider(size)

    suspend fun bfs(start: Int, result: IntArray, scope: CoroutineScope) {
        var frontier = IntArray(1)
        frontier[0] = start
        result[start] = 0
        visitedNodes.set(start, 1)
        var frontierSize = frontier.size
        while (frontierSize > 0) {
            val degreeArr = IntArray(frontierSize)
            scope.launch {
                runSplit(0, frontierSize, 1, parallelLimit, scope) { i ->
                    degreeArr[i] = childrenProvider.getDegree(frontier[i])
                }
            }.join()
            if (degreeArr.size < parallelScanLimit) {
                scan(degreeArr)
            } else {
                scope.launch {
                    parallelScanV2(degreeArr, parallelScanLimit, scope)
                }.join()
            }

            val nextFrontier = IntArray(degreeArr[degreeArr.size - 1]) { -1 }
            val nextFrontierSize = nextFrontier.size
            val filterScan = IntArray(nextFrontierSize)
            scope.launch {
                runSplit(0, frontierSize, 1, parallelLimit / 6, scope) { i ->
                    val degree = if (i == 0) {
                        0
                    } else {
                        degreeArr[i - 1]
                    }
                    filterChildrenSync(frontier[i], result, nextFrontier, degree)
                }
            }.join()
            scope.launch {
                runSplit(0, nextFrontierSize, 1, parallelLimit, scope) { i ->
                    if (nextFrontier[i] != -1) {
                        filterScan[i] = 1
                    }
                }
            }.join()
            if (filterScan.size < parallelScanLimit) {
                scan(filterScan)
            } else {
                scope.launch {
                    parallelScanV2(filterScan, parallelScanLimit, scope)
                }.join()
            }
            frontier = IntArray(filterScan[filterScan.size - 1]) { -1 }
            frontierSize = frontier.size
            scope.launch {
                runSplit(0, nextFrontier.size - 1, 1, parallelLimit, scope) { i ->
                    val value = if (i == 0) {
                        0
                    } else {
                        filterScan[i - 1]
                    }
                    if (value != filterScan[i]) {
                        frontier[value] = nextFrontier[i]
                    }
                }
            }.join()
            if (filterScan[nextFrontier.size - 2] != frontierSize) {
                frontier[frontierSize - 1] = nextFrontier[nextFrontier.size - 1]
            }
        }
    }

    private suspend fun parallelScan(x: IntArray, scope: CoroutineScope): IntArray {
        val n = x.size
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

    private suspend fun parallelScanV2(x: IntArray, limit: Int, scope: CoroutineScope) {
        val endIndices = ArrayList<Int>()
        val jobs = ArrayList<Job>()
        for (start in x.indices step limit) {
            val end = start + Integer.min(limit, x.size - start)
            endIndices.add(end)
            val job = scope.launch {
                for (i in start until end - 1) {
                    x[i + 1] += x[i]
                }
            }
            jobs.add(job)
        }
        for (job in jobs) {
            job.join()
        }
        val additions = IntArray(endIndices.size - 1)
        var currentAddition = 0
        for (i in additions.indices) {
            currentAddition += x[endIndices[i] - 1]
            additions[i] = currentAddition
        }
        val restJobs = ArrayList<Job>()
        for (start in additions.indices) {
            val job = scope.launch {
                val addition = additions[start]
                for (i in endIndices[start] until endIndices[start + 1]) {
                    x[i] += addition
                }
            }
            restJobs.add(job)
        }
        for (job in restJobs) {
            job.join()
        }
    }

    private fun scan(x: IntArray) {
        for (i in 0 until x.size - 1) {
            x[i + 1] += x[i]
        }
    }

    private suspend fun scanForward(x: IntArray, realSize: Int, pad: Int, scope: CoroutineScope) {
        val n = realSize + pad
        val upperBound = log2(n.toDouble()).toInt() - 1
        for (d in 0..upperBound) {
            val job = scope.launch {
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

    private suspend fun runSplit(
        l: Int,
        r: Int,
        step: Int,
        limit: Int,
        scope: CoroutineScope,
        syncFun: (i: Int) -> Unit
    ) {
        val actualLimit: Int = if ((r - l) / step.toDouble() < limit) {
            r - l
        } else {
            step * limit
        }
        val jobs = ArrayList<Job>()
        for (start in l until r step actualLimit) {
            val job = scope.launch {
                val end = Integer.min(actualLimit + start, r - l)
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
        nodeIndex: Int,
        result: IntArray,
        nextFrontier: IntArray,
        degree: Int
    ) {
        var ctr = 0
        val nodeChildren = childrenProvider.getNodeChildren(nodeIndex)
        val currentDistance = result[nodeIndex]
        for (childIndex in nodeChildren) {
            if (visitedNodes.compareAndSet(childIndex, 0, 1)) {
                result[childIndex] = currentDistance + 1
                nextFrontier[degree + ctr] = childIndex
                ctr++
            }
        }
    }
}