package filemanager

import java.io.RandomAccessFile
import java.nio.ByteBuffer

import configuration._

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
  val fileName: String = s"$id.sst"
  val blocks = new ArrayBuffer[SSTBlock]
  val index = new SSTIndex(fileName)

  def insert(kv: KeyValuePair): Unit = {
    if (blocks.isEmpty ) insertIntoNewBlock(kv)
    else if (blocks.last.hasEnoughSpace(kv)) insertIntoBlock(kv)
    else insertIntoNewBlock(kv)
  }
  private def insertIntoBlock(keyValuePair: KeyValuePair): Unit = {
    blocks.last.put(keyValuePair)
  }
  private def insertIntoNewBlock(keyValuePair: KeyValuePair): Unit = {
    index.insert(keyValuePair.key, blocks.length * Configuration.blockSize)
    blocks += new SSTBlock
    blocks.last.put(keyValuePair)
  }

  def save(): Unit = {
    val sstFile = new RandomAccessFile(s"${Configuration.dataPath}$fileName", "rw")
    val channel = sstFile.getChannel

    for (block <- blocks) {
      block.write(channel)
    }
    channel.close()
    sstFile.close()
  }


}