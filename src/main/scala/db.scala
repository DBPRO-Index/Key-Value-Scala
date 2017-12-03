import filemanager.FileManager

object db extends App {
  val fileManager = new FileManager

  for (i <- 1 to 999) {
    val key = i.toString.reverse.padTo(3, "0").reverse.mkString("")
    fileManager.write(key, s"value of $key")
  }
  var result = fileManager.read("001")
  println(result)

  result = fileManager.read("015")
  println(result)

  result = fileManager.read("021")
  println(result)

  result = fileManager.read("001", "021")
  println(result.toList.sorted)
  println(result.size)

  result = fileManager.read("021", "021")
  println(result.toList.sorted)
  println(result.size)

  result = fileManager.read("001", "005")
  println(result.toList.sorted)
  println(result.size)

  result = fileManager.read("005", "005")
  println(result.toList.sorted)
  println(result.size)

  result = fileManager.read("005", "004")
  println(result.toList.sorted)
  println(result.size)

  result = fileManager.read("999")
  println(result)

  result = fileManager.read("000", "999")
  println(result.toList.sorted)
  println(result.size)

  fileManager.close()
}
