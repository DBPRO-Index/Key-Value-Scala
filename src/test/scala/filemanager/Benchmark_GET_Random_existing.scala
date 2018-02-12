package filemanager

import common.TestUtil._
import util.SensordataService

object Benchmark_GET_Random_existing extends App {
  println("=========  BENCHMARK: GET RANDOM KEY  =========")
  println(s"FILE: $filepath")
  println()

  if (args.length > 0 && args(0) == "-n") {
    SensordataService.generateFile(filename, numberOfEvents)
  }
  val keys_random = generateRandomKeys(numberOfKeys = 100)
  val keys_existing = generateExistingKeys(numberOfKeys = 100)

  val db_filemanager = new FileManager
  println("Random:")
  db_filemanager.insertFromFile(filepath)
  benchmark(multipleGets(db_filemanager, keys_random), numberOfSamples)

  val db_filemanager_ex  = new FileManager
  println("Existing:")
  db_filemanager_ex.insertFromFile(filepath)
  benchmark(multipleGets(db_filemanager_ex, keys_existing), numberOfSamples)

}
