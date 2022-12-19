package quicksort

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class AsyncQuickSorter(private val concurrentLimit: Int) {

    suspend fun sort(array: Array<Int>): Unit = coroutineScope {
        launch {
            sort(array, 0, array.size - 1)
        }
    }

    private suspend fun sort(array: Array<Int>, start: Int, end: Int): Unit = coroutineScope {
        if (array.size == 1) {
            return@coroutineScope
        }
        val left = partition(start, end, array)
        if (start < left) {
            if (left - start <= concurrentLimit) {
                sequentialSort(array, start, left)
            } else {
                launch {
                    sort(array, start, left)
                }
            }
        }
        if (left + 1 < end) {
            if (end - left - 1 <= concurrentLimit) {
                sequentialSort(array, left + 1, end)
            } else {
                launch {
                    sort(array, left + 1, end)
                }
            }
        }
    }

    private fun sequentialSort(array: Array<Int>, leftIndex: Int, rightIndex: Int) {
        val queue = ArrayDeque<Pair<Int, Int>>()
        queue.addLast(Pair(leftIndex, rightIndex))
        quickSort(array, queue)
    }
}