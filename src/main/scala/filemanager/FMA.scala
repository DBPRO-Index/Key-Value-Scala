package filemanager

import scala.collection.mutable.ArrayBuffer

trait FMA {
  def write(key: String, value: String): Unit
  def read(key: String): String
  def read(key1: String, key2: String): ArrayBuffer[String]
  def close(): Unit
}
