package quicksort

import java.util.*


open class SyncQuickSorter {

    private val queue = LinkedList<Pair<Int, Int>>()

    fun sort(array: Array<Int>): Array<Int> {
        if (array.size == 1) {
            return array
        }
        queue.addLast(Pair(0, array.size - 1))
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
        return array
    }

    private fun swap(array: Array<Int>, left: Int, right: Int) {
        if (left != right) {
            val temp = array[left]
            array[left] = array[right]
            array[right] = temp
        }
    }
}