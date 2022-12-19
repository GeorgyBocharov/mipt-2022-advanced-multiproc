package quicksort

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import kotlin.random.Random

class SyncQuickSorterTest {
    private val sorter = SyncQuickSorter()
    private val asyncSorter = AsyncQuickSorter(1)

    @Test
    fun `sort short array`() {
        val unsorted = arrayOf(9, 4, 2, 5, 1, 3, 7, 6, 8, 0)
        sorter.sort(unsorted)
        assertThat(unsorted).isSorted
    }

    @Test
    fun `sort random array`() {
        val unsorted = Array(30) { Random.nextInt() }
        sorter.sort(unsorted)
        assertThat(unsorted).isSorted
    }

    @Test
    fun `sort short array async`(): Unit = runBlocking {
        val unsorted = arrayOf(9, 4, 2, 5, 1, 3, 7, 6, 8, 0)
        val job = launch(Dispatchers.Default) {
            asyncSorter.sort(unsorted)
        }
        job.join()
        assertThat(unsorted).isSorted
    }

    @Test
    fun `sort random array async`(): Unit = runBlocking {
        val unsorted = Array(100_000) { Random.nextInt() }
        val job = launch(Dispatchers.Default) {
            asyncSorter.sort(unsorted)
        }
        job.join()
        assertThat(unsorted).isSorted
    }
}