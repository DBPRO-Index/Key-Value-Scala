package configuration

object Configuration {
  val defaultHost:String = "localhost"
  val defaultPort:Int = 2000
  
  val defaultThreadCount:Int = 10
  
  val defaultBufferSize:Int = 1000
  
  val blockSize: Int = 64000
  val memtableSize: Int = 1280000
  val dataPath: String = "data/"
}

