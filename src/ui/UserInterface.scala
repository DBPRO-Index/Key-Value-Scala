package ui

import buffer._

import java.nio.file.{Paths, Files}
import java.nio.charset.StandardCharsets

object UserInterface {
  
  val buffer:Buffer[String,String] = new LRUBuffer(100,100)
  
  def printInstructions():Unit = {
    print("Below you can see an overview of the available commands:\n")
    print("get key\n")
    print("set key:value\n")
    print("set filepath/fileName.txt\n")
    
  }
  
  def loadBatch(path:String):Unit = {
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
  
  def standardSet(contents:String):Unit = {
    buffer.set(contents.substring(0, contents.indexOf(":")), 
               contents.substring(contents.indexOf(":")+1, contents.size))
  }
  
  def parseSet(contents:String){
    val colonIndex = contents.indexOf(":")
    colonIndex match{
      case -1 => loadBatch(contents)
      case _ => standardSet(contents)
    }
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
             case "get" => returnedVal = buffer.get(command.substring(spacePosition+1, command.size))
             case "set" => parseSet(command.substring(spacePosition+1, command.size))
             case _ => 
           }
           if(!returnedVal.isEmpty) print(returnedVal.get + "\n")
         }
//         sys.addShutdownHook({
//         })
   }
}