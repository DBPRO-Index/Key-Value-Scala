package buffer

import scala.collection.mutable.{Map}
import java.util.concurrent._

import filemanager.{FileManager, FMA}

class LRUBuffer(bufferSize:Int) extends Buffer{
  
  private val DELETED_VALUE = "NULL"
  
  private val theBuffer: LRUQueue[String] = LRUQueue()
  private val modifiedMap: Map[String,Boolean] = Map()
  private val theMap: Map[String,String] = Map()
  private val fileManager: FMA = new FileManager()
  
  private object Locker
  
  private var pageFaults = 0
  private var references = 0 
  
  private def addNonExisting(key:String, value:String, fromSet:Boolean):Unit = {
    pageFaults+=1
     
    // The buffer is not full
    if(theBuffer.size < bufferSize){
       
     // The buffer is full - apply replacement policy
     }else{
       
       val oldKey = theBuffer.dequeue()
       val oldValue:Option[String] = theMap.remove(oldKey)
       
       if(modifiedMap.contains(oldKey)){
         fileManager.write(oldKey.toString, oldValue.getOrElse(DELETED_VALUE))
         modifiedMap.remove(oldKey)
       }
     }
    
     theBuffer += key
     theMap += key -> value
    
     // Mark as modified if the page fault originates from a set query
     if(fromSet) modifiedMap += key -> true
     
  }
  
  private def addExisting(key:String, value:String):Unit = {
    
    // Place the referenced value on the head of the queue
    theBuffer.pushToHead(key)
    
    // Mark as modified and update if value has changed
    if(!(theMap(key) equals value)){
      modifiedMap += theBuffer.front -> true
    	theMap += key -> value
    }
  }
  
  override def set(key:String, value:String): Unit = {
    references+=1
    
    if(theMap.contains(key))
      addExisting(key,value)
    else
      addNonExisting(key,value,true)
  }
  
  override def get(key:String): Option[String] = {
    references+=1
    
    // Check the disk if value not found in map 
    // and add to buffer if present on disk
    if(theMap.get(key).isEmpty){
      val value = fileManager.read(key).get(key)
      if(!value.isEmpty) addNonExisting(key,value.get,false)
      value
    }
    else{ 
      theBuffer.pushToHead(key)
      theMap.get(key) 
    }
  }

  override def delete(key:String):Boolean = {
    var listIsEmpty = false
    var valueAlreadyDeleted = false
    var successful = false;
    if(!theMap.get(key).isEmpty){
    	theBuffer.dequeueFirst(k => k equals key)
			theMap.remove(key)
			successful = true
    }else{
      val keyFromDisk = fileManager.read(key) 
      listIsEmpty = keyFromDisk.size == 0
      if(!listIsEmpty) valueAlreadyDeleted = (keyFromDisk(key) equals DELETED_VALUE)
      successful = !(listIsEmpty || valueAlreadyDeleted)
    }
    if(!valueAlreadyDeleted) fileManager.write(key.toString(), DELETED_VALUE)
    successful
  }
  
  override def flushBuffer():Unit = {
    pageFaults = 0
    references = 0
    theMap.foreach(x => fileManager.write(x._1, x._2))
    theBuffer.clear()
    theMap.clear()
  }
  
  override def hitRate():Double = {
    val fr = (pageFaults.toDouble/references)
    if(fr.isNaN()) 0.0 else 1 - fr
  }
  
  override def toString():String = {
    var test = ""
    theBuffer.foreach(x => test+="(" + x + ":" + theMap(x) + ")->")
    if(test equals "") "Empty" else test.substring(0, test.size-2)
  }
  
}

object LRUBuffer{
  def apply(size:Int) = new LRUBuffer(size)
}