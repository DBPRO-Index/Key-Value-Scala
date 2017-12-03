package filemanager

import scala.collection.mutable

trait FMA {
  def write(key: String, value: String): Unit
  def read(key: String): mutable.Map[String, String]
  def read(key1: String, key2: String): mutable.Map[String, String]
  def close(): Unit
}
