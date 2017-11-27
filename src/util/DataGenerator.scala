package util

import java.nio.file.{Paths, Files}
import java.nio.charset.StandardCharsets
import scala.collection.mutable.ListBuffer
import scala.collection.immutable.List
import scala.util.{Random}
import buffer.KeyValPair

object DataGenerator {

  val numberStream = Stream.iterate(0)(_ + 1).iterator
  
  def generateData(size:Int):ListBuffer[KeyValPair] = {
    val data = new ListBuffer[KeyValPair]()
    for(i <- 0 to size){
      val key = "key_" + (Random.nextInt(size*10))
      val value = "value_" + (Random.nextInt(size*10))
      data += new KeyValPair(key, value)
    }
    Files.write(Paths.get("RandomData","randomData_" + numberStream.next() + ".txt"), data.mkString("\n").getBytes(StandardCharsets.UTF_8))
    data
  }
  
  def getData(fileIndex:Int) = {
    var lines = Files.readAllLines(Paths.get("RandomData","randomData_" + fileIndex + ".txt"), StandardCharsets.UTF_8)
    val data = new ListBuffer[KeyValPair]()
    val linesIterator = lines.iterator()
    while(linesIterator.hasNext()){
      val current = linesIterator.next();
      val index = current.indexOf(":")
      data+=new KeyValPair(current.substring(0, index), current.substring(index+1, current.size))
    }
    data
  }
  
}