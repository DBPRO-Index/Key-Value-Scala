package filemanager

class KeyValuePair(val key: String, val value : String) {
  val data:Array[Byte] = s"$key:$value\n".getBytes( "UTF-8")
  val size: Int = data.length
  override def toString: String = s"$key:$value\n"
}

object KeyValuePair {
  def getSize(key: String, value: String): Int = {
    s"$key:$value\n".getBytes( "UTF-8").length
  }
}