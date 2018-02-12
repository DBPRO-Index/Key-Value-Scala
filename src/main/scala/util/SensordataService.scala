package util

import java.io.PrintWriter
import java.time.{Instant, LocalDateTime, ZoneId}

import scala.util.Random

object SensordataService {
  def generateFile(file: String, numberOfEvents: Int): Unit = {
    val out = new PrintWriter(s"${GeneratorConfiguration.datapath}$file")
    val datapath = GeneratorConfiguration.datapath
    val numberOfDatapoints = GeneratorConfiguration.numberOfDatapoints
    val maxValueOfDatapoint = GeneratorConfiguration.maxValueOfDatapoint
    val maxTimeOffsetInSeconds = GeneratorConfiguration.maxTimeOffsetInSeconds
    val maxSensorId = GeneratorConfiguration.maxSensorId

    print("[GENERATING] ")
    for (n <- 1 to numberOfEvents) {
      if (n % (numberOfEvents / 20) == 0) print("#")
      val key = generateKey(maxTimeOffsetInSeconds, maxSensorId)
      val value = generateValue(numberOfDatapoints, maxValueOfDatapoint)
      out.println(s"$key:$value")

    }
    println()
    println(s"[ COMPLETE ] $numberOfEvents lines written")
    println()
    out.close()
  }


  def generateValue(numberOfDatapoints: Int, maxValueOfDatapoint: Int): String = {
    var result = "{"
    for (n <- 1 to numberOfDatapoints) {
      val width = maxValueOfDatapoint.toString.length
      val valueOfDatapoint = Random.nextInt(maxValueOfDatapoint)
      result = s"$result x$n: ${valueToString(valueOfDatapoint, width)}"
      if (n != numberOfDatapoints) {
        result = s"$result,"
      }
    }
    result = s"$result }"
    result
  }

  def generateKey(maxTimeOffsetInSeconds: Int, maxSensorId: Int):String = {
    val widthForTimestamp = 2
    val ldt = LocalDateTime.ofInstant(
      Instant.now.minusSeconds(Random.nextInt(maxTimeOffsetInSeconds)),
      ZoneId.systemDefault())
    val year = ldt.getYear
    val month = valueToString(ldt.getMonthValue, widthForTimestamp)
    val day = valueToString(ldt.getDayOfMonth, widthForTimestamp)
    val hour = valueToString(ldt.getHour, widthForTimestamp)
    val minute = valueToString(ldt.getMinute, widthForTimestamp)
    val second = valueToString(ldt.getSecond, widthForTimestamp)
    val id = valueToString(Random.nextInt(maxSensorId), maxSensorId.toString.length)
    s"$year-$month-${day}T$hour-$minute-$second|$id"
  }

  def format(value: String, width: Int, padElement: String = "0" ): String = {
    value.reverse.padTo(width, padElement).reverse.mkString("")
  }

  def valueToString(value: Int, width: Int): String = {
    format(value.toString, width)
  }
}
