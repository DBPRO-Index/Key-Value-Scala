package filemanager

import common.Configuration

import scala.collection.mutable.ArrayBuffer

class FileManager extends FMA  {
  val sst = new SSTHeap(Configuration.blockSize)

  override def write(key: String, value: String): Unit = ???

  override def read(key: String): String = ???

  override def read(key1: String, key2: String): ArrayBuffer[String] = ???

  override def close(): Unit = ???
}
