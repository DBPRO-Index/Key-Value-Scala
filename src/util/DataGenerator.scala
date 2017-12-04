package util

import java.nio.file.{Paths, Files, Path}
import java.io.File;
import java.nio.charset.StandardCharsets
import scala.collection.mutable.ListBuffer
import scala.collection.immutable.List
import scala.collection.JavaConversions._
import scala.util.{Random}
import buffer.KeyValPair

object DataGenerator {

  var curFolder:String = ""
  
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
               list+=(fileName.substring(fileName.indexOf("_") + 1, fileName.lastIndexOf("_")).toInt)
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
  
  def generateData(folder:String,size:Int):ListBuffer[KeyValPair] = {
    curFolder = folder;
    
    val data = new ListBuffer[KeyValPair]()
    for(i <- 0 to size){
      val key = "key_" + (Random.nextInt(size*10))
      val value = "value_" + (Random.nextInt(size*10))
      data += new KeyValPair(key, value)
    }
    if (!Files.exists(Paths.get(folder))) Files.createDirectories(Paths.get(folder));
    Files.write(Paths.get(folder, "randomData_" + getNextFileID(folder) + "_" + size + ".txt"), data.mkString("\n").getBytes(StandardCharsets.UTF_8))
    data
  }
  
  def getNewest():ListBuffer[KeyValPair] = {
    getData(curFolder,getCurFileID(curFolder))
  }
  
  def getNewestFrom(folder:String):ListBuffer[KeyValPair] = {
    getData(folder,getCurFileID(folder))
  }
  
  def getData(folder:String, fileIndex:Int):ListBuffer[KeyValPair] = {
    val data = new ListBuffer[KeyValPair]()
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
    }catch{
      case e:java.nio.file.NoSuchFileException => {
        data+=new KeyValPair("Error","FileNotFound")
        data
      }
    }
    val linesIterator = lines.iterator
    while(linesIterator.hasNext){
      val current = linesIterator.next();
      val index = current.indexOf(":")
      data+=new KeyValPair(current.substring(0, index), current.substring(index+1, current.size))
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