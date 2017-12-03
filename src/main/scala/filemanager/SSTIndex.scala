package filemanager

import scala.collection.mutable
import scala.collection.Searching._
import scala.collection.mutable.ArrayBuffer

class SSTIndex(val sstFileName: String) {
  val index: mutable.Map[String, Int] = mutable.Map[String, Int]()

  def insert(key: String, offset: Int): Unit = {
    index += (key -> offset)
  }

  def findOffset(key: String): Option[Int] = {
    val keys = index.keys.toList.sorted
    val found = keys.search(key)
    found match {
      case Found(n) => Some(index(keys(Found(n).foundIndex)))
      case InsertionPoint(n) =>
        if (n > 0) Some(index(keys(InsertionPoint(n).insertionPoint -1)))
        else None
    }
  }

  def findOffset(lower: String, upper: String): ArrayBuffer[Int] = {
    val result = ArrayBuffer[Int]()
    val keys = index.keys.toList.sorted

    val found = keys.search(lower) match {
      case Found(n) => Some(index(keys(Found(n).foundIndex)))
      case InsertionPoint(n) =>
        if (n > 0) Some(index(keys(InsertionPoint(n).insertionPoint -1)))
        else  Some(index(keys(InsertionPoint(n).insertionPoint)))
    }
    val min = found.getOrElse( return result).toString
    for (key <- keys if key >= min && key <= upper) {
      result += index(key)
    }
    result
  }
}
