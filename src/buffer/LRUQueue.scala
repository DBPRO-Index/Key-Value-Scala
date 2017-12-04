package buffer

import scala.collection.mutable.Queue

class LRUQueue[E] extends Queue[E]{
  def pushToHead(element:E):Unit = {
    val list = dequeueAll(x => x equals element)
    if(list.size > 0) enqueue(list(0))
  }
}

object LRUQueue{
  def apply[E]() = new LRUQueue[E]
}