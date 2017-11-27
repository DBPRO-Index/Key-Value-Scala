package filemanager


class Block(val maxBlockSize: Int) {
  val data = new Array[Byte](maxBlockSize)
  var space: Int = maxBlockSize
  var offset = 0
  var firstElement: String = _

  def insert(kv: KeyValuePair): Boolean = {
    if (firstElement == null) firstElement = kv.key
    if (kv.size <= space) {
      kv.data.copyToArray(data, offset, kv.size)
      space -= kv.size
      offset += kv.size
      true
    } else {
      false
    }
  }

  def debug(): Unit = {
    println(s"data: ${data.map( x => if (x!=10) x.toChar else "|" ).mkString}; space: $space: offset $offset; firstElement: $firstElement")
  }

}