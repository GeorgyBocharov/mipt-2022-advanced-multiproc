package quicksort

import java.util.LinkedList

class SynchronousQuickSorter {

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
            var left = start
            var right = end

            val pivot = array[(right + left) / 2]

            while (left <= right) {
                while (array[left] < pivot && left < right) {
                    left++
                }
                while (array[right] >= pivot && right > left) {
                    right--
                }
                if (left <= right) {
                    val temp = array[left]
                    array[left] = array[right]
                    array[right] = temp
                    left++
                    right--
                }
            }
            if (start < left - 1) {
                queue.addLast(Pair(start, left - 1))
            }
            if (left < end) {
                queue.addLast(Pair(left, end))
            }
        }
        return array
    }
}