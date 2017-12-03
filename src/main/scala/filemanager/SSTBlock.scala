package filemanager

import java.io.RandomAccessFile
import java.nio.ByteBuffer
import java.nio.channels.FileChannel

import configuration.Configuration

import scala.collection.mutable

class SSTBlock() {
  private val block = ByteBuffer.allocate(Configuration.blockSize)
  block.clear()

  def put(keyValuePair: KeyValuePair): Unit = {
    block.put(keyValuePair.data)
  }

  def write(channel: FileChannel): Unit = {
    block.position(block.limit())
    block.flip()
    while(block.hasRemaining) {
      channel.write(block)
    }
  }

  def hasEnoughSpace(keyValuePair: KeyValuePair): Boolean = {
    keyValuePair.size <= block.limit() - block.position()
  }

  def load(fileName: String, offset: Int): Unit = {
    val sstFile = new RandomAccessFile(s"${Configuration.dataPath}$fileName", "r")
    val channel = sstFile.getChannel
    channel.read(block, offset)
    channel.close()
    sstFile.close()
  }
  def getData: Array[Byte] = {
    block.array()
  }
}
