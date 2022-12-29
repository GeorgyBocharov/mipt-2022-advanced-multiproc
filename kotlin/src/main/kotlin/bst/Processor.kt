package bst

import bst.tree.Bst
import java.util.concurrent.LinkedBlockingQueue

class Processor(val resultList: IntArray,
                val elementsNumber: Int,
                val x: Int,
                val tree: Bst,
                val end: Long,
                val runWithCheck: Boolean,
                val operationsQueue: LinkedBlockingQueue<Operation>):Runnable {



    override fun run() {

    }
}