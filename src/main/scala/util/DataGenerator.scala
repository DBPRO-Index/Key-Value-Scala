package util

import java.nio.file.{Paths, Files}
import java.io.File
import java.nio.charset.StandardCharsets
import scala.collection.mutable.ListBuffer
import scala.collection.JavaConversions._
import scala.util.{Random}
import buffer.BufKeyValPair

object DataGenerator {

  var curFolder:String = ""
  var lastAccessedFile = ""
  
  def lastAccessedFileName:String = {
    lastAccessedFile
  }
  
  def getCurFileID(folder:String):Int = {
    val list = new ListBuffer[Int]()
    val dir = new File(folder)
    for(file <- dir.listFiles()){
      if(!file.isDirectory() && 
         file.getName.contains("randomData_") &&
         file.getName.endsWith(".txt")){
           val fileName = file.getName
           val indexOfUnderscore = fileName.indexOf("_")
           if(indexOfUnderscore != -1){
             try{
               var id = fileName.substring(fileName.indexOf("_"), fileName.lastIndexOf("_"))
               id = id.substring(id.indexOf("_") + 1, id.lastIndexOf("_"))
               list+=id.toInt
             }catch {
               case e: Exception => 
             }
           }
       } 
    }
    if(list.size > 0) list.max else -1
  }
  
  private def getNextFileID(folder:String):Int = {
    getCurFileID(folder) + 1
  }
  
  def generateData(folder:String,size:Int,bound:Int):ListBuffer[BufKeyValPair] = {
    curFolder = folder;
    
    val data = new ListBuffer[BufKeyValPair]()
    for(i <- 0 to size){
      val key = "key_" + (Random.nextInt(bound)).toString.reverse.padTo((bound).toString.length, "0").reverse.mkString("")
      val value = "value_" + (Random.nextInt(bound))
      data += new BufKeyValPair(key, value)
    }
    if (!Files.exists(Paths.get(folder))) Files.createDirectories(Paths.get(folder));
    Files.write(Paths.get(folder, "randomData_" + getNextFileID(folder) + "_" + size + "_" + bound + ".txt"), data.mkString("\n").getBytes(StandardCharsets.UTF_8))
    data
  }
  
  def getNewest():ListBuffer[BufKeyValPair] = {
    getData(curFolder,getCurFileID(curFolder))
  }
  
  def getNewestFrom(folder:String):ListBuffer[BufKeyValPair] = {
    getData(folder,getCurFileID(folder))
  }
  
  def getData(folder:String, fileIndex:Int):ListBuffer[BufKeyValPair] = {
    val data = new ListBuffer[BufKeyValPair]()
    var lines:java.util.List[String] = List()
    try{
      var fileName = ""
      val dir = new File(folder)
      for(file <- dir.listFiles()){
        if (!file.isDirectory() && 
            file.getName.contains("randomData_" + fileIndex) &&
            file.getName.endsWith(".txt"))
          
          fileName = file.getName
      }
      lines = Files.readAllLines(Paths.get(folder,fileName), StandardCharsets.UTF_8)
      lastAccessedFile = fileName
    }catch{
      case e:java.nio.file.NoSuchFileException => {
        data+=new BufKeyValPair("Error","FileNotFound")
        data
      }
    }
    val linesIterator = lines.iterator
    while(linesIterator.hasNext){
      val current = linesIterator.next();
      val index = current.indexOf(":")
      data+=new BufKeyValPair(current.substring(0, index), current.substring(index+1, current.size))
    }
    data
  }
  
  def clearDir(folder:String,safetyPrompt:Boolean):Unit = {
    if(Files.exists(Paths.get(folder))){
      var answer:String = ""
      if(safetyPrompt){
        print("Contents of \"" + folder + "\" are going to be deleted are you sure? (yes/no)")
        answer = scala.io.StdIn.readLine()
      }
      if(!safetyPrompt || answer.equals("yes")){
        val dir = new File(folder)
        for(file <- dir.listFiles()){
          if (!file.isDirectory()) 
            file.delete();
        }
      }
    }
  }
  
}