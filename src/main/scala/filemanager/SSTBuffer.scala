package filemanager

import scala.collection.mutable

import configuration._

class SSTBuffer() {
  object Order extends Ordering[KeyValuePair] {
    def compare(kv1:KeyValuePair, kv2:KeyValuePair): Int = kv2.key compare kv1.key
  }
  private val mapOfKeyValuePairs = mutable.Map[String, String]()
  private var freeSpace: Int = Configuration.memtableSize

  def insert(kv: KeyValuePair): Unit = {
    mapOfKeyValuePairs(kv.key) = kv.value
    freeSpace -= kv.size
  }

  def flush(): SSTIndex = {



    val sortedKeyValuePairs =
      for (kv <- mapOfKeyValuePairs.toList.sortBy(_._1))
      yield new KeyValuePair(kv._1, kv._2)
    mapOfKeyValuePairs.clear()
    freeSpace = Configuration.memtableSize

    val out = new SSTFile()
    for (kv <- sortedKeyValuePairs) {
      out.insert(kv)
      //freeSpace += kv.size
    }
    out.save()
    out.index
  }
  def hasEnoughSpace(keyValuePair: KeyValuePair): Boolean = {
    keyValuePair.size <= freeSpace
  }

  def get(key: String): Option[String] = {
    val found = mapOfKeyValuePairs.par.find(kv => kv._1.equals(key))
    found match {
      case Some(keyValuePair) => Some(keyValuePair._2)
      case None => None
    }
  }

  def getRange(lower: String, upper: String): mutable.Map[String, String] = {
    val result = mapOfKeyValuePairs.filter( kv => kv._1 >= lower).filter( kv => kv._1 <= upper)
    result
  }
}
