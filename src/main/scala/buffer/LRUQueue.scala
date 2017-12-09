package buffer

import scala.collection.mutable.Queue

class LRUQueue[E] extends Queue[E]{
  def pushToHead(element:E):Unit = {
    val value = dequeueFirst(x => x equals element)
    if(value.isDefined) enqueue(value.get)
  }
}

object LRUQueue{
  def apply[E]() = new LRUQueue[E]
}