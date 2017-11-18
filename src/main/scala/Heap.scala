import scala.io.Source

class Heap(maxSize: Int) {
  object Order extends Ordering[KeyValuePair] {
    def compare(x:KeyValuePair, y:KeyValuePair): Int = y.key compare x.key
  }

  private val minHeap: scala.collection.mutable.PriorityQueue[KeyValuePair] = scala.collection.mutable.PriorityQueue.empty(Order)
  var size = 0

  def enqueue(kv: KeyValuePair): Unit = {
    if (size + kv.size < maxSize) {
      minHeap += kv
      size += kv.size
    } else {
      val segment = new SortedStringTable()
      while (minHeap.nonEmpty) {
        val next = minHeap.dequeue()
        size -= next.size
        segment.insert(next)
      }
      segment.save()
      minHeap += kv
      size += kv.size
    }
  }
  def save(): Unit = {
    val segment = new SortedStringTable()
    while (minHeap.nonEmpty) {
      val next = minHeap.dequeue()
      size -= next.size
      segment.insert(next)
    }
    segment.save()
  }

  def get(key: String): String = {
    val found = minHeap.par.filter(p => p.key == key)
    if (found.nonEmpty) found.head.value else null
  }

  def loadSST(id: Int): Unit = {

  }
  def load(file: String): Unit = {
    val in = Source.fromFile(file, "UTF-8")
    val lineIterator = in.getLines()
    var i = 0

    for (l <- lineIterator) {
      i += 1
      val x = l.replace("{", "")
        .replace("}", "")
        .replace("\"", "")
        .replace(" ", "")
      val pairs = x.split(",")
      var key = ""
      for (i <- 0 to 1) {
        key += pairs(i).split(":")(1)
        if (i == 0) key += "-"
      }
      enqueue(new KeyValuePair(key, l))
    }
  }
}