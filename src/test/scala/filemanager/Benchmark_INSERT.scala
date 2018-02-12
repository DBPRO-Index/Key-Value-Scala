package filemanager

import common.TestUtil
import util.SensordataService

object Benchmark_INSERT extends App {
  if (args.length > 0 && args(0) == "-n") {
    SensordataService.generateFile(TestUtil.filename, TestUtil.numberOfEvents)
  }

  println("=========  BENCHMARK: INSERTION FROM FILE  =========")
  println(s"FILE: ${TestUtil.filepath}")
  println()

  println("BASELINE:")
  val scriptDB = new ScriptDB
  TestUtil.benchmark( scriptDB.insertFromFile(TestUtil.filepath), TestUtil.numberOfSamples )

  println("SOLUTION:")
  val fileManager = new FileManager
  TestUtil.benchmark( fileManager.insertFromFile(TestUtil.filepath), TestUtil.numberOfSamples )
}
