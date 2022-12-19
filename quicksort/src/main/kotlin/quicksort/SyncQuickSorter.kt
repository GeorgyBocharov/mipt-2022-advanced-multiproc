package quicksort

import kotlin.collections.ArrayDeque

fun quickSort(array: Array<Int>, queue: ArrayDeque<Pair<Int, Int>>) {
    while (queue.isNotEmpty()) {
        val pair = queue.removeFirst()
        val start = pair.first
        val end = pair.second
        val left = partition(start, end, array)
        if (start < left) {
            queue.addLast(Pair(start, left))
        }
        if (left + 1 < end) {
            queue.addLast(Pair(left + 1, end))
        }
    }
}

fun partition(start: Int, end: Int, array: Array<Int>): Int {
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
    return left
}

fun swap(array: Array<Int>, left: Int, right: Int) {
    if (left != right) {
        val temp = array[left]
        array[left] = array[right]
        array[right] = temp
    }
}

open class SyncQuickSorter {

    private val queue = ArrayDeque<Pair<Int, Int>>()

    fun sort(array: Array<Int>) {
        if (array.size == 1) {
            return
        }
        queue.addLast(Pair(0, array.size - 1))
        quickSort(array, queue)
    }
}