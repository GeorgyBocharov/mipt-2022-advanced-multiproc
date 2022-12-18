package bfs

class ChildrenProvider(private val size: Int) {
    fun getDegree(i: Int): Int {
        val z: Int = i / (size * size)
        val y: Int = (i % (size * size)) / size
        val x: Int = (i % (size * size)) % size
        var degree = 0
        if (x > 0) {
            degree++
        }
        if (y > 0) {
            degree++
        }
        if (z > 0) {
            degree++
        }
        if (x < size - 1) {
            degree++
        }
        if (y < size - 1) {
            degree++
        }
        if (z < size - 1) {
            degree++
        }
        return degree
    }

    fun getNodeChildren(nodeIndex: Int): ArrayList<Int> {
        val list = ArrayList<Int>(6)
        val z: Int = nodeIndex / (size * size)
        val y: Int = (nodeIndex % (size * size)) / size
        val x: Int = (nodeIndex % (size * size)) % size
        if (x > 0) {
            list.add(nodeIndex - 1)
        }
        if (y > 0) {
            list.add(nodeIndex - size)
        }
        if (z > 0) {
            list.add(nodeIndex - size * size)
        }
        if (x < size - 1) {
            list.add(nodeIndex + 1)
        }
        if (y < size - 1) {
            list.add(nodeIndex + size)
        }
        if (z < size - 1) {
            list.add(nodeIndex + size * size)
        }
        return list
    }
}

