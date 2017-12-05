package ui

import buffer._
import util._
import java.nio.file.{Paths, Files}
import java.nio.charset.StandardCharsets
import scala.collection.mutable.ListBuffer

object UserInterface {
  
  val buffer:Buffer = TwoQBuffer(3000)
//  val buffer:Buffer = LRUBuffer(3000)
  val generatedKeys:ListBuffer[String] = ListBuffer()
  
  private def printInstructions():Unit = {
    print("Available commands:\n")
    print("get key - retrieves the value for the key, NULL if it doesn't exist\n")
    print("set key:value - saves the key/value pair into the database or updates the value if the key exists\n")
    print("set ...dir/fileName.txt - Loads key/value pairs into the database. Format should be key:value followed by new line\n")
    print("setgen ...dir:index - Loads key/value pairs from generated file with the given index.\n")
    print("del key - deletes the key and its value from the database\n")
    print("gen ...dir:num:bound - generates key/value pairs in the specified directory. File contains \"num\" amount of entries. \nBound specifies number of possible unique keys. File name is randomData_#_num \n")
    print("genset ...dir:num:bound - same as \"gen\" but also loads the file into the database\n")
    print("fr - returns the current fault rate of the buffer\n")
    
  }
  
  private def loadFromFile(path:String):Unit = {
    if(!path.endsWith(".txt")){
      print("Only .txt files supported\n")
      return
    }
    val filePath = if(path.lastIndexOf("/") != -1) path.substring(0, path.lastIndexOf("/")) else ""
    val fileName = if(path.lastIndexOf("/") != -1) path.substring(path.lastIndexOf("/") + 1,path.size) else path
    try{
      val lines = Files.readAllLines(Paths.get(filePath,fileName), StandardCharsets.UTF_8)
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
  
  private def loadFromList(list:ListBuffer[BufKeyValPair]):Unit = {
    print("Loading...")
    val start = System.nanoTime()

    list.foreach(k => standardSet(k.toString()))
    val timeElapsed = (System.nanoTime - start) / 1e9d
    print(" done in " + timeElapsed + "s\n")
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
    var message = ""
    
    val firstColonIndex = contents.indexOf(":")
    val secondColonIndex = contents.lastIndexOf(":")
    
    if(firstColonIndex != -1){
      val filePath:String = if(firstColonIndex > 0) contents.substring(0, firstColonIndex) else "."
      try {
        val size = if(firstColonIndex == secondColonIndex) contents.substring(firstColonIndex + 1, contents.size).toInt
                   else contents.substring(firstColonIndex + 1, secondColonIndex).toInt
        val bound = if(firstColonIndex == secondColonIndex) size else contents.substring(secondColonIndex + 1, contents.size).toInt
        DataGenerator.generateData(filePath, size, bound).foreach(x => generatedKeys += x.getKey())
        message = "File randomData_" + DataGenerator.getCurFileID(filePath) + "_" + size + "_" + bound + ".txt created\n"
        if(shouldLoad){ 
          loadFromList(DataGenerator.getNewestFrom(filePath))
          message = message.substring(0,message.size-1) + " and loaded\n"
        }
      }catch {
        case e: NumberFormatException => {
          message = "Wrong format for \"gen\". Type \"help\" for usage info\n"
          print(e.printStackTrace() + "\n")
        }
      }
    }
    print(message)
  }
  
  private def parseSetGen(contents:String):Unit = {
    var message = ""
    val colonIndex = contents.indexOf(":")
    if(colonIndex != -1){
      try{
        val folder = if(colonIndex == 0) "." else contents.substring(0, colonIndex)
        val fileIndex = contents.substring(colonIndex + 1, contents.size).toInt
        val data = DataGenerator.getData(folder, fileIndex)
        loadFromList(data)
        message = if(data.size == 0) "No file with that index found\n" else "Loaded " + DataGenerator.lastAccessedFileName + "\n"
      }catch{
        case e: NumberFormatException => message = "Wrong format for \"setgen\". Type \"help\" for usage info\n"
      }
    }
    print(message)
  }
  
  
  private def parseGet(contents:String):Unit = {
    print(buffer.get(contents).getOrElse("NULL") + "\n")
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
         case "buf" => print(buffer.toString() + "\n")
         case "del" | "delete" => parseDel(params)
         case "flush" => buffer.flushBuffer(); print("Buffer flushed\n")
         case "gen" => parseGen(params,false)
         case "get" => parseGet(params)
         case "genset" => parseGen(params,true)
         case "help" => printInstructions()
         case "hrate" => printf("%.0f".format(buffer.hitRate * 100) + "%%\n");
         case "set" => parseSet(params)
         case "setgen" => parseSetGen(params)
         case "test" => if(generatedKeys.size == 0) print("Empty") else print(generatedKeys.mkString(","))  
         case "" =>
         case _ => print("No such command. Type \"help\" for usage info\n") 
       }
     }
     buffer.flushBuffer()
//   sys.addShutdownHook({ })
   }
}