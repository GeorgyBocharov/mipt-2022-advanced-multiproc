package quicksort

import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.util.*

class AsyncQuickSorter {

    suspend fun sort(array: Array<Int>, start: Int, end: Int): Unit = coroutineScope {
        if (array.size == 1) {
            return@coroutineScope
        }
        var pivotIndex = (start + end) / 2
        val pivot = array[pivotIndex]
        var left = start
        var right = end

        while (left < right) {
            while (array[left] < pivot && left < right) {
                left++
            }
            while (array[right] >= pivot && right > left) {
                right--
            }
            if (left != right) {
                if (left == pivotIndex) {
                    pivotIndex = right
                } else if (right == pivotIndex) {
                    pivotIndex = left
                }
                swap(array, left, right)
                left++
                right--
            }
        }
        if (array[left] > pivot) {
            swap(array, left, pivotIndex)
        }
        if (start < left) {
            if (left - start <= 1000) {
                sequentialSort(array, start, left)
            } else {
                launch {
                    sort(array, start, left)
                }
            }
        }
        if (left + 1 < end) {
            if (end - left - 1 <= 1000) {
                sequentialSort(array, left + 1, end)
            } else {
                launch {
                    sort(array, left + 1, end)
                }
            }
        }
    }

    private fun sequentialSort(array: Array<Int>, leftIndex: Int, rightIndex: Int) {
        val queue = LinkedList<Pair<Int, Int>>()
        queue.addLast(Pair(leftIndex, rightIndex))
        while (queue.isNotEmpty()) {
            val pair = queue.poll()
            val start = pair.first
            val end = pair.second
            var pivotIndex = (start + end) / 2
            val pivot = array[pivotIndex]
            var left = start
            var right = end

            while (left < right) {
                while (array[left] < pivot && left < right) {
                    left++
                }
                while (array[right] >= pivot && right > left) {
                    right--
                }
                if (left != right) {
                    if (left == pivotIndex) {
                        pivotIndex = right
                    } else if (right == pivotIndex) {
                        pivotIndex = left
                    }
                    swap(array, left, right)
                    left++
                    right--
                }
            }
            if (array[left] > pivot) {
                swap(array, left, pivotIndex)
            }
            if (start < left) {
                queue.addLast(Pair(start, left))
            }
            if (left + 1 < end) {
                queue.addLast(Pair(left + 1, end))
            }
        }
    }

    private fun swap(array: Array<Int>, left: Int, right: Int) {
        if (left != right) {
            val temp = array[left]
            array[left] = array[right]
            array[right] = temp
        }
    }
}