package bfs

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test

private const val size = 100

class SyncBfsTest {

    @Test
    fun `find distances in cube array`() {
        val bfsSync = SyncBfs(size)
        val distances = IntArray(size* size* size)
        bfsSync.bfs(0, distances)
        testDistances(distances)
    }

    @Test
    fun `find distances in cube array async`(): Unit = runBlocking{
        val distances = IntArray(size* size* size)
        val bfsAsync = AsyncBfs(size, 700)
        launch(Dispatchers.Default) {
            bfsAsync.bfs(0, distances, this)
        }.join()
        testDistances(distances)
    }

    private fun testDistances(distances: IntArray) {
        for (i in distances.indices) {
            val z: Int = i / (size * size)
            val y: Int = (i % (size * size)) / size
            val x: Int = (i % (size * size)) % size
            Assertions.assertThat(distances[i]).isEqualTo(x + y + z)
        }
    }
}