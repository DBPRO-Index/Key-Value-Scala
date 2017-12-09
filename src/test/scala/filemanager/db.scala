package filemanager

import scala.io.Source
import java.io.PrintWriter

import scala.collection.mutable
import scala.util.Random

object db extends App {
  val fileManager = new FileManager

  val source = Source.fromFile("test_data/randomData_0_1000000_1000000.txt", "UTF-8")
  val lineIterator = source.getLines

  val set = mutable.Set[String]()

  for (l <- lineIterator) {

    val kv = l.split(":").toList
    set.add(kv.head)
    fileManager.write(kv.head, kv.tail.mkString(""))
  }

  println("Range Query key_0000000 : key_9999999")
  var resultTimeTuple = time(fileManager.read("key_0000000", "key_9999999"))
  println(s"Number of Pairs: ${resultTimeTuple._1.size}")
  println(s"Time: ${resultTimeTuple._2}")
  println(s"avg: ${resultTimeTuple._2 / resultTimeTuple._1.size}")



  var sum = 0L
  val n = 100
  var min = 0L
  var max = 0L
  for (l <- 1 to n) {
    val key = "key_" + Random.nextInt(1000000).toString.reverse.padTo(7, "0").reverse.mkString("")
    val t = time(fileManager.read(key))._2
    sum += t
    if (t > max)
      max = t
    if (t < min)
      min = t
  }

  println(s"$n random GET's")
  println(s"avg: ${sum / n} ms")
  println(s"min: $min ms")
  println(s"max: $max ms")


  Random.nextInt(1000000)
  fileManager.close()


  def time[R](block: => R): (R, Long) = {
    val t0 = System.currentTimeMillis()
    val result = block    // call-by-name
    val t1 = System.currentTimeMillis()
    val dt = t1 - t0
    (result, dt)
  }
}


