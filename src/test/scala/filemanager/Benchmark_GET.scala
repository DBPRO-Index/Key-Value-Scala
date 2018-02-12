package filemanager

import common.TestUtil._
import util.SensordataService



object Benchmark_GET extends App {
  println("=========  BENCHMARK: GET RANDOM KEY  =========")
  println(s"FILE: $filepath")
  println()

  if (args.length > 0 && args(0) == "-n") {
    SensordataService.generateFile(filename, numberOfEvents)
  }
  val keys = generateRandomKeys(numberOfKeys = 10)

/*  val db_baseline = new ScriptDB
  println("BASELINE:")
  db_baseline.insertFromFile(filepath)
  benchmark(multipleGets(db_baseline, keys), numberOfSamples)*/

  val db_filemanager = new FileManager
  println("SOLUTION:")
  db_filemanager.insertFromFile(filepath)
  benchmark(multipleGets(db_filemanager, keys), numberOfSamples)
}
