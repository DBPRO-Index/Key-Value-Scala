package ui

import buffer._
import util._

import java.nio.file.{Paths, Files}
import java.nio.charset.StandardCharsets

import scala.collection.mutable.ListBuffer

object UserInterface {
  
//  val buffer:Buffer[String,String] = LRUBuffer(100)
  val buffer:Buffer[String,String] = TwoQBuffer(100)
  val generatedKeys:ListBuffer[String] = ListBuffer()
  
  private def printInstructions():Unit = {
    print("Available commands:\n")
    print("get key - retrieves the value for the key, NULL if it doesn't exist\n")
    print("set key:value - saves the key/value pair into the database or updates the value if the key exists\n")
    print("del key - deletes the key and its value from the database\n")
    print("set ...dir/fileName.txt - Loads key/value pairs into the database. Format should be key:value followed by new line\n")
    print("gen ...dir:num - generates key/value pairs in the specified directory. File contains \"num\" amount of entries. File name is randomData_#_num \n")
    print("genset ...dir:num - same as \"gen\" but also loads the file into the database\n")
  }
  
  private def loadFromFile(path:String):Unit = {
    if(!path.endsWith(".txt")){
      print("Only .txt files supported\n")
      return
    }
    
    var filePath = ""
    var fileName = path
    if(path.lastIndexOf("/") != -1){
      filePath = path.substring(0, path.lastIndexOf("/"))
      fileName = path.substring(path.lastIndexOf("/"),path.size)
    }
    var lines:java.util.List[String] = new java.util.LinkedList()
    try{
      lines = Files.readAllLines(Paths.get(filePath,fileName), StandardCharsets.UTF_8)
      lines.forEach(x => standardSet(x))
    }catch{
      case e:java.nio.file.NoSuchFileException => {
         if(fileName.equals(path)) print("No such file\n")
         else print("No such file or directory\n")
         return
      }
    }
    print("Success\n")
  }
  
  private def loadFromList(list:ListBuffer[KeyValPair]):Unit = {
    list.foreach(k => standardSet(k.toString()))
  }
  
  private def standardSet(contents:String):Boolean = {
    val key = contents.substring(0, contents.indexOf(":"))
    val value = if(contents.indexOf(":")+1 == contents.size) "" 
                else contents.substring(contents.indexOf(":")+1, contents.size)
    if(!value.trim().isEmpty()){
      buffer.set(key,value)
      true
    }else false
  }
  
  
  private def parseSet(contents:String){
  	contents.indexOf(":") match{
    	case -1 => loadFromFile(contents)
    	case _ => if(standardSet(contents)) print("Success\n") else print("No value provided\n")
  	}
  }
  
  private def parseGen(contents:String,shouldLoad:Boolean):Unit = {
    var error = true
    val colonIndex = contents.indexOf(":")
    if(colonIndex != -1){
      error = false;
      val filePath:String = if(colonIndex > 0) contents.substring(0, colonIndex) else "."
      var size:Int = -1
      try {
        size = contents.substring(colonIndex + 1, contents.size).toInt
        DataGenerator.generateData(filePath, size).foreach(x => generatedKeys += x.getKey())
        if(shouldLoad){ 
          loadFromList(DataGenerator.getNewestFrom(filePath))
          print("File randomData_" + DataGenerator.getCurFileID(filePath) + "_" + size + ".txt created and loaded\n")
        }else print("File randomData_" + DataGenerator.getCurFileID(filePath) + "_" + size + ".txt created\n")
           
      }catch {
        case e: Exception => error = true
      }
    }
    if(error) print("Wrong format for gen. Type \"help\" for usage info\n")
  }
  
  
  private def parseGet(contents:String):Unit = {
    val returnedVal = buffer.get(contents)
    if(!returnedVal.isEmpty) print(returnedVal.get + "\n")
    else print("NULL\n")
  }
  
  private def parseDel(contents:String):Unit = {
    if(buffer.delete(contents)) print("Success\n") 
    else print("Key " + "\"" + contents + "\" not found\n")
  }
  
  def main(args: Array[String]): Unit = {
     var command = ""
     print("For usage type \"help\"\n")
     while(!command.equals("quit")){
       command = scala.io.StdIn.readLine()
       val spacePosition = command.indexOf(" ")
       val action = if(spacePosition == -1) command else command.substring(0, spacePosition)
       val params = if(spacePosition == -1) "" else command.substring(spacePosition+1, command.size)
       action match{
         case "help" => printInstructions()
         case "get" => parseGet(params)
         case "set" => parseSet(params)
         case "del" | "delete" => parseDel(params)
         case "gen" => parseGen(params,false)
         case "genset" => parseGen(params,true)
         case "test" => if(generatedKeys.size == 0) print("Empty") else print(generatedKeys.mkString(","))  
         case "" =>
         case _ => print("No such command. Type \"help\" for usage info\n") 
       }
     }
     buffer.flushBuffer()
//   sys.addShutdownHook({ })
   }
}