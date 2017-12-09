package buffer

import scala.collection.mutable.{Map}
import java.util.concurrent._

import filemanager.{FileManager, FMA}

class LRUBuffer(bufferSize:Int) extends Buffer{
  
  private val theBuffer: LRUQueue[String] = LRUQueue()
  
  private def addNonExisting(key:String, value:String, fromSet:Boolean):Unit = {
     
    theBuffer += key
    theMap += key -> value
     
    // The buffer is not full
    if(theBuffer.size > bufferSize){
       
       val oldKey = theBuffer.dequeue()
       val oldValue = theMap.remove(oldKey)
       
       if(modifiedMap.contains(oldKey)){
         fileManager.write(oldKey, oldValue.getOrElse(DELETED_VALUE))
         modifiedMap.remove(oldKey)
       }
     }
    
     // Mark as modified if the page fault originates from a set query,
     // meaning it's either new or modified key/value pair 
     // that the db doesn't know about
     if(fromSet) modifiedMap += key -> true
     
  }
  
  private def addExisting(key:String, value:String):Unit = {
    
    // Place the referenced value on the head of the queue
    theBuffer.pushToHead(key)
    
    // Mark as modified and update if value has changed
    if(!(theMap(key) equals value)){
      modifiedMap += key -> true
    	theMap += key -> value
    }
  }
  
  override def set(key:String, value:String): Unit = {
    references+=1
    
    if(theMap.contains(key))
      addExisting(key,value)
    else
      pageFaults+=1
      addNonExisting(key,value,true)
  }
  
  override def get(key:String): Option[String] = {
    references+=1
    
    // Check the disk if value not found in map 
    // and add to buffer if present on disk
    
    if(theMap.contains(key)){
      theBuffer.pushToHead(key)
      theMap.get(key) 
    }else{ 
      val value = fileManager.read(key).get(key)
      if(value.isDefined){
        if(value.get equals DELETED_VALUE) None
        else{
        	addNonExisting(key,value.get,false) 
        	value
        }
      }else None
    }
  }

  override def delete(key:String):Boolean = {
    var listIsEmpty = false
    var valueAlreadyDeleted = false
    var successful = false;
    if(theMap.contains(key)){
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
    super.flushBuffer()
    theBuffer.clear()
  }
  
  override def toString():String = {
    var theString = ""
    theBuffer.foreach(x => theString+="(" + x + ":" + theMap(x) + ")->")
    if(theString.isEmpty) "Empty" else theString.substring(0, theString.size-2)
  }
  
}

object LRUBuffer{
  def apply(size:Int) = new LRUBuffer(size)
}