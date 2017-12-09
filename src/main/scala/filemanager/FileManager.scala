package filemanager

import configuration.Configuration
import scala.collection.mutable
import scala.collection.mutable.ArrayBuffer
import scala.io.Source

class FileManager extends FMA {
  
  private val debug = false;
  
  private val sst = new SSTBuffer()
  private val sstIndex = new ArrayBuffer[SSTIndex]()

  override def write(key: String, value: String): Unit = {
    val keyValuePair = new KeyValuePair(key, value)
    if (keyValuePair.size > Configuration.blockSize) throw new IllegalArgumentException()
    if (sst.hasEnoughSpace(keyValuePair)) {
      sst.insert(keyValuePair)
    } else {
      sstIndex += sst.flush()
      sst.insert(keyValuePair)
    }
  }
  override def read(key: String): mutable.Map[String, String] = {
    if(debug) println(s"[GET] Key=$key")
    sst.get(key) match {
      case Some(v) => mutable.Map[String, String](key -> Some(v).get)
      case None => searchFiles(key)
    }
  }

  override def read(lower: String, upper: String): mutable.Map[String, String] = {
    if(debug) println(s"[GET] Key=[$lower:$upper]")
    val range = mutable.Map[String, String]()

    for (index <- sstIndex) {
      range ++= getRangeFromFile(lower, upper, index.sstFileName, index)
    }
    range ++= sst.getRange(lower, upper)
    range
  }

  override def close(): Unit = {
    sstIndex += sst.flush()
  }
  private def searchFiles(key: String): mutable.Map[String, String] = {
    val iterator = sstIndex.reverseIterator
    for (index <- iterator) {
      getFromFile(key, index.sstFileName, index) match {
        case Some(v) => return mutable.Map(key -> Some(v).get)
        case None => ()
      }
    }
    mutable.Map[String, String]()
  }
  private def getFromFile(key: String, filename: String, index: SSTIndex): Option[String] = {
    val offset = index.findOffset(key).getOrElse(return None)
    val block = new SSTBlock
    block.load(filename, offset)

    val source = Source.fromBytes(block.getData, "UTF-8")
    for (line <- source.getLines()) {
      if (line.contains(":")) {
        val kv = line.split(":").toList
        if (kv.head.equals(key)) return Some(kv.tail.mkString(":"))
      }
    }
    None
  }
  private def getRangeFromFile(lower: String,
                               upper: String,
                               filename: String,
                               index: SSTIndex): mutable.Map[String, String] = {
    val offsets = index.findOffset(lower, upper)
    val result = mutable.Map[String, String]()

    for (offset <- offsets) {
      val block = new SSTBlock
      block.load(filename, offset)
      val source = Source.fromBytes(block.getData, "UTF-8")
      for (line <- source.getLines()) {
        if (line.contains(":")) {
          val kv = line.split(":").toList
          if (kv.head >= lower && kv.head <= upper) result += (kv.head -> kv.tail.mkString(":"))
        }
      }
    }
    result
  }
}

