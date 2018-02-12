package filemanager

import common.TestUtil._
import util.SensordataService


object Benchmark_RANGE extends App {
  println("=========  BENCHMARK: GET RANGE  =========")
  println(s"FILE: $filepath")
  println()

  if (args.length > 0 && args(0) == "-n") {
    SensordataService.generateFile(filename, numberOfEvents)
  }
  val range = getRangeOfTestData()
  val lower = range._1
  val upper = range._2

  val db_filemanager = new FileManager
  println("SOLUTION :")
  db_filemanager.insertFromFile(filepath)
  benchmark(db_filemanager.read(lower, upper), numberOfSamples)
}
