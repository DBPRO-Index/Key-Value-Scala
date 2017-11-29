package ui

import buffer._
import util._

import java.nio.file.{Paths, Files}
import java.nio.charset.StandardCharsets

import scala.collection.mutable.ListBuffer

object UserInterface {
  
  val buffer:Buffer[String,String] = new LRUBuffer(100)
  
  private def printInstructions():Unit = {
    print("Below you can see an overview of the available commands:\n")
    print("get key\n")
    print("set key:value\n")
    print("set ...filepath/fileName.txt\n")
    print("gen ...filepath/fileName.txt:numEntries\n")
    print("genSet ...filepath/fileName.txt:numEntries\n")
  }
  
  private def loadFromFile(path:String):Unit = {
    val filePath = path.substring(0, path.lastIndexOf("/"))
    val fileName = path.substring(path.lastIndexOf("/"),path.size)
    if(!fileName.endsWith(".txt")){
      print("Only .txt files supported\n")
      return
    }
    var lines:java.util.List[String] = new java.util.LinkedList()
    try{
      lines = Files.readAllLines(Paths.get(filePath,fileName), StandardCharsets.UTF_8)
      lines.forEach(x => standardSet(x))
    }catch{
      case e:java.nio.file.NoSuchFileException => {
         print("No such file\n")
         return
      }
    }
    print("Done\n")
  }
  
  private def loadFromList(list:ListBuffer[KeyValPair]):Unit = {
    list.foreach(k => standardSet(k.toString()))
    print("Done\n")
  }
  
  private def standardSet(contents:String):Unit = {
    buffer.set(contents.substring(0, contents.indexOf(":")), 
               contents.substring(contents.indexOf(":")+1, contents.size))
  }
  
  private def parseSet(contents:String){
    val colonIndex = contents.indexOf(":")
    val slashIndex = contents.indexOf("/")
    if(colonIndex + slashIndex != -2){
    	colonIndex match{
    	case -1 => loadFromFile(contents)
    	case _ => { 
    	            standardSet(contents) 
    	            print("Done\n")
    	          }
    	}
    }else print("Wrong format for set. Type \"help\" for usage info\n")
  }
  
  private def parseGen(contents:String,shouldLoad:Boolean){
    var error = true
    val colonIndex = contents.indexOf(":")
    if(colonIndex != -1){
      error = false;
      val filePath:String = contents.substring(0, colonIndex)
      var size:Int = -1
      try {
        size = contents.substring(colonIndex + 1, contents.size).toInt
        DataGenerator.generateData(filePath, size)
        if(shouldLoad) loadFromList(DataGenerator.getNewestFrom(filePath))
      }catch {
        case e: Exception => error = true
      }
    }
    if(error) print("Wrong format for gen. Type \"help\" for usage info\n")
  }
  
  
  
  def main(args: Array[String]): Unit = {
     var command = ""
     print("For usage type \"help\"\n")
     while(!command.equals("quit")){
       command = scala.io.StdIn.readLine()
       val spacePosition = command.indexOf(" ")
       val action = if(spacePosition == -1) command else command.substring(0, spacePosition)
       
       var returnedVal:Option[String] = Option.empty
       action match{
         case "help" => printInstructions()
         case "get" => {
           returnedVal = buffer.get(command.substring(spacePosition+1, command.size))
           if(!returnedVal.isEmpty) print(returnedVal.get + "\n")
           else print("NULL\n")
         }
         case "set" => parseSet(command.substring(spacePosition+1, command.size))
         case "gen" => parseGen(command.substring(spacePosition+1, command.size),false)
         case "genSet" => parseGen(command.substring(spacePosition+1, command.size),true)
         case _ => 
       }
     }
     buffer.flushBuffer()
//   sys.addShutdownHook({ })
   }
}