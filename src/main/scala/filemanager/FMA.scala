package filemanager

import scala.collection.mutable

trait FMA {
  def write(key: String, value: String): Unit
  def read(key: String): mutable.Map[String, String]
  def read(lower: String, upper: String): mutable.Map[String, String]
  def close(): Unit
}
