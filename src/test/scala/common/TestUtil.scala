package common

import filemanager.FMA
import util.{GeneratorConfiguration, SensordataService}

import scala.collection.mutable.ArrayBuffer
import scala.io.Source
import scala.util.Random

object TestUtil {
  val filename = "benchmark.txt"
  val filepath = s"${GeneratorConfiguration.datapath}$filename"
  val numberOfEvents = 1000000
  val numberOfSamples = 10

  def time[R](block: => R): (R, Long) = {
    val t0 = System.currentTimeMillis()
    val result = block
    val t1 = System.currentTimeMillis()
    (result, t1 - t0)
  }

  def benchmark[R](block: => R, numberOfSamples: Int): Unit = {
    println("[START] Benchmark")
    var resultDtPair: (Any, Long) = (0, 0L)
    var listOfExecutionTimes: List[Long] = Nil
    var listOfResults: List[Any] = Nil

    for (i <- 1 to numberOfSamples) {
      println(s"[START] Sample $i")
      resultDtPair = time(block)
      listOfResults = resultDtPair._1 :: listOfResults
      listOfExecutionTimes = resultDtPair._2 :: listOfExecutionTimes
    }

    val sum = listOfExecutionTimes.sum
    val avg = sum / listOfExecutionTimes.length
    val min = listOfExecutionTimes.min
    val max = listOfExecutionTimes.max
    val dev = deviation(listOfExecutionTimes, avg)

    println(s"${format("average", 10, " ", reverse = true)}: ${format(avg.toString, 9, " ")}")
    println(s"${format("deviation", 10, " ", reverse = true)}: ${format(dev.toString, 9, " ")}")
    println(s"${format("minimum", 10, " ", reverse = true)}: ${format(min.toString, 9, " ")}")
    println(s"${format("maximum", 10, " ", reverse = true)}: ${format(max.toString, 9, " ")}")
    println()
    println("  SAMPLE  |   TIME   ")
    println("---------------------")
    for (x <- listOfExecutionTimes) {
      println(s"${format(listOfExecutionTimes.indexOf(x).toString, 9, " ")} | ${format(x.toString, 9, " ")}")
    }
    println()
    for (i <- listOfResults) {
      i
    }
    println( )
  }

  def generateRandomKeys(numberOfKeys: Int = 10): ArrayBuffer[String] = {
    val keys = ArrayBuffer[String]()

    for (_ <- 1 to numberOfKeys) {
      val id = GeneratorConfiguration.maxSensorId
      val offset = GeneratorConfiguration.maxTimeOffsetInSeconds
      keys += SensordataService.generateKey(offset, id)
    }
    keys
  }

  def generateExistingKeys(numberOfKeys: Int = 10): ArrayBuffer[String] = {
    val keys = ArrayBuffer[String]()
    val allKeys = ArrayBuffer[String]()
    val source = Source.fromFile(filepath, "UTF-8")
    val lineIterator = source.getLines

    for (l <- lineIterator) {
      val kv = l.split(":").toList
      allKeys += kv.head
    }

    val maxIndex = allKeys.length
    while (keys.size < numberOfKeys ) {
      keys += allKeys(Random.nextInt(maxIndex))
    }
    keys
  }

  def getRangeOfTestData(numberOfKeys: Int = 1000): (String, String) = {
    var keys = List[String]()
    val allKeys = ArrayBuffer[String]()
    val source = Source.fromFile(filepath, "UTF-8")
    val lineIterator = source.getLines

    for (l <- lineIterator) {
      val kv = l.split(":").toList
      allKeys += kv.head
    }

    val maxIndex = allKeys.length
    while (keys.size < numberOfKeys ) {
      keys = allKeys(Random.nextInt(maxIndex)) :: keys
    }
    (keys.min, keys(Random.nextInt(numberOfKeys)))
  }

  def multipleGets(db: FMA, keys: ArrayBuffer[String]): List[(String, String)] = {
    var values: List[(String, String)] = Nil
    for (k <- keys) {
      val kv = db.read(k)
      values = (k, kv.getOrElse(k, "")) :: values
    }
    values
  }

  def deviation(list: List[Long], avg: Long): Long = {
    val n = list.length
    val variance = list.map(x => Math.pow(x - avg, 2)).sum / n
    Math.round(Math.sqrt(variance))
  }

  def format(value: String, width: Int, padElement: String = "0", reverse: Boolean = false): String = {
    if (reverse) value.padTo(width, padElement).mkString("")
    else value.reverse.padTo(width, padElement).reverse.mkString("")

  }
}
