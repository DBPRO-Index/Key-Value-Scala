package filemanager

import java.io.RandomAccessFile
import java.nio.ByteBuffer

import common._

import scala.collection.mutable.ArrayBuffer


object SSTFile {
  var lastNumber = 0
  def newUniqueNumber(): Int = {
    lastNumber += 1
    lastNumber
  }

}

class SSTFile() {
  val id: Int = SSTFile.newUniqueNumber()
  val blocks =  new ArrayBuffer[Block]()
  val index: scala.collection.mutable.Map[String, Int] = scala.collection.mutable.Map[String, Int]()

  def insert(kv: KeyValuePair): Unit = {
    if (blocks.isEmpty || !blocks.last.insert(kv)) {
      val newBlock = new Block(Configuration.blockSize)
      newBlock.insert(kv)
      index(newBlock.firstElement) = Configuration.blockSize * blocks.length
      blocks += newBlock
    }
  }

  def save(): Unit = {
    val out =new RandomAccessFile(s"data/$id-SST.txt", "rw")
    val outChannel = out.getChannel
    val buf = ByteBuffer.allocate(Configuration.blockSize)

    for(b <- blocks) {
      buf.clear()
      buf.put(b.data)
      buf.flip()
      while (buf.hasRemaining) {
        outChannel.write(buf)
      }
    }
    out.close()
  }

  def findOffset(key: String): Int = {
    val keys = index.keys.toList.sorted
    var max = keys.length
    var min = 0

    if (key < keys(min)) -1
    else {
      while (max > (min + 1)) {
        val mid = (min + max) / 2
        if (keys(mid) <= key) min = mid
        else max = mid
      }
      index(keys(min))
    }
  }
}