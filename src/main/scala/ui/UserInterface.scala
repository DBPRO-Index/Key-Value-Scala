package ui

import buffer._
import util._
import java.nio.file.{Paths, Files}
import java.nio.charset.StandardCharsets
import scala.collection.mutable.{ListBuffer,Seq,Queue}
import scala.util.{Random}

object UserInterface {
  
  val bufferSize = 1000
  
  var currentBuffer:Buffer = PassThrough()
  
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
    var progress = 0;
    list.foreach(k => standardSet(k.toString()))
    
    val timeElapsed = (System.nanoTime - start) / 1e9d
    print(" done in " + timeElapsed + "s\n")
  }
  
  private def standardSet(contents:String):Boolean = {
    val key = contents.substring(0, contents.indexOf(":"))
    val value = if(contents.indexOf(":")+1 == contents.size) "" 
                else contents.substring(contents.indexOf(":")+1, contents.size)
    if(!value.trim().isEmpty()){
      currentBuffer.set(key,value)
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
        if(size >= 1000000) print("Loading that will take some time. Are you sure? (y/n)\n")
        if(size < 1000000 || (scala.io.StdIn.readLine() equals "y")) {
          DataGenerator.generateData(filePath, size, bound).foreach(x => generatedKeys += x.getKey())
          message = "File randomData_" + DataGenerator.getCurFileID(filePath) + "_" + size + "_" + bound + ".txt created\n"
          if(shouldLoad){ 
            loadFromList(DataGenerator.getNewestFrom(filePath))
            message = message.substring(0,message.size-1) + " and loaded\n"
          }
        }else message = "Quitting... \n"
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
        if(data.nonEmpty){
          data.foreach(x => generatedKeys += x.getKey())
          loadFromList(data)
        }
        message = if(data.size == 0) "No file with that index found\n" else "Loaded " + DataGenerator.lastAccessedFileName + "\n"
      }catch{
        case e: NumberFormatException => message = "Wrong format for \"setgen\". Type \"help\" for usage info\n"
      }
    }
    print(message)
  }
  
  
  private def parseGet(contents:String):Unit = {
    print(currentBuffer.get(contents).getOrElse("NULL") + "\n")
  }
  
  private def parseRandomGet(contents:String):Unit = {
    var message = ""
    val uniqueKeys = scala.util.Random.shuffle(generatedKeys.toSet).toArray
    val colonIndex = contents.indexOf(":")
    if(uniqueKeys.nonEmpty && colonIndex > 0){
      try{
        val numOfGets = contents.substring(0, colonIndex).toInt
        var range = contents.substring(colonIndex + 1).toInt
        range = if(uniqueKeys.size < range) uniqueKeys.size else range
        val randList:Queue[String] = Queue()
        for(i <- 1 to numOfGets) randList += uniqueKeys(Random.nextInt(range))
        print("Testing...")
        val start = System.nanoTime()
        var returned = 0
        while(randList.nonEmpty){
          if(currentBuffer.get(randList.dequeue()).isDefined) returned +=1
        }
        val timeElapsed = (System.nanoTime - start) / 1e9d
        print(" returned " + returned + " in " + timeElapsed + "s\n")
      }catch{
        case e: NumberFormatException => print("Wrong format for \"getrand\". Type \"help\" for usage info\n")
      }
    }else{
      
    	print("Error in parseRandomGet()\n")
    }
  }
  
  private def parseRange(contents:String):Unit = {
    var message = ""
    val colonIndex = contents.indexOf(":")
    if(colonIndex > 0){
        val lower = contents.substring(0, colonIndex)
        val upper = contents.substring(colonIndex + 1, contents.size)
        print("buffer.range(" + lower + "," + upper + ")\n")
        val returnedList = currentBuffer.range(lower, upper)
        message = if(returnedList.isEmpty) "No values that match the criteria found.\n" else "Found " + returnedList.size + " entries.\n"
    }else message = "Wrong format for \"range\". Type \"help\" for usage info\n"
    print(message)
  }
  
  private def parseDel(contents:String):Unit = {
    if(currentBuffer.delete(contents)) print("Success\n") 
    else print("Key " + "\"" + contents + "\" not found\n")
  }
  
  private def parseSwitch(contents:String):Unit = {
    var message = ""
    val colonIndex = contents.indexOf(":")
    if(colonIndex > 0 || (contents equals "none")){
      try{
        var failed = false
        val bufType = if(colonIndex > 0) contents.substring(0, colonIndex) else "none"
        val size = if(colonIndex > 0) (contents.substring(colonIndex + 1)).toInt else 0
        bufType match{
          case "lru" => currentBuffer.flushBuffer()
                        currentBuffer = LRUBuffer(size) 
          case "arc" => currentBuffer.flushBuffer()
                        currentBuffer = ARCBuffer(size)
          case "2q" => currentBuffer.flushBuffer()
                       currentBuffer = TwoQBuffer(size)
          case "none" => currentBuffer.flushBuffer()
                         currentBuffer = PassThrough()
          case _ => failed = true
        }
        message = if(failed) "Failed. Types are: lru, 2q or arc\n" else "Success\n"
      }catch{
        case e:NumberFormatException => message = "Second parameter must be an integer!\n"
      }
    }else message = "Wrong format for \"range\". Type \"help\" for usage info\n"
    print(message)
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
         case "buf" => print(currentBuffer.toString() + "\n")
         case "del" | "delete" => parseDel(params)
         case "flush" => currentBuffer.flushBuffer(); print("Buffer flushed\n")
         case "gen" => parseGen(params,false)
         case "get" => parseGet(params)
         case "getrand" => parseRandomGet(params)
         case "genset" => parseGen(params,true)
         case "help" => printInstructions()
         case "hrate" => printf("%.0f".format(currentBuffer.hitRate * 100) + "%%\n")
         case "range" => parseRange(params)
         case "set" => parseSet(params)
         case "setgen" => parseSetGen(params)
         case "switch" => parseSwitch(params)
         case "test" => if(generatedKeys.size == 0) print("Empty") else print(generatedKeys.mkString(","))  
         case "" | "quit" =>
         case _ => print("No such command. Type \"help\" for usage info\n") 
       }
     }
     currentBuffer.flushBuffer()
//   sys.addShutdownHook({ })
   }
}