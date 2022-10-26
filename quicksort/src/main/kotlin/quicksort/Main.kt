package quicksort

fun main() {
    val array = arrayOf(25, 38, 72, 79, 81)
    println(array.contentToString())
    val sorter = SynchronousQuickSorter()
    val arr = sorter.sort(array)
    println(arr.contentToString())
}