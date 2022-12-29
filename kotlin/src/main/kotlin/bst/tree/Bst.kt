package bst.tree

interface Bst {
    fun insert(value: Int): Boolean
    fun remove(value: Int): Boolean
    fun contains(value: Int): Boolean
}