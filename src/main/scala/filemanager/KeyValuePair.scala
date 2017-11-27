package filemanager

class KeyValuePair(newPair: (String, String)) {
  val key: String = newPair._1
  val value: String = newPair._2
  val data:Array[Byte] = s"$key:$value\n".getBytes( "UTF-8")
  val size: Int = data.length

  override def toString: String = s"$key:$value"
}
