package quicksort

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.OffsetDateTime
import kotlin.random.Random

class SyncQuickSorterTest {
    private val sorter = SyncQuickSorter()

    @Test
    fun `sort short array`() {
        val unsorted = arrayOf(9, 4, 2, 5, 1, 3, 7, 6, 8, 0)
        val sorted = sorter.sort(unsorted)
        assertThat(sorted).isSorted
    }

    @Test
    fun `sort random array`() {
        val unsorted = Array(30) { Random.nextInt() }
        val sorted = sorter.sort(unsorted)
        assertThat(sorted).isSorted
    }
}