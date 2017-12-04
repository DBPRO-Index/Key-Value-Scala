package buffer

import scala.collection.mutable.{Queue,ListBuffer,Map}
import java.util.concurrent._

class LRUBuffer[K,V](bufferSize:Int) extends Buffer[K,V]{
  
  private val DELETED_VALUE = "NULL"
  
  private val theBuffer: LRUQueue[K] = LRUQueue()
  private val modifiedMap: Map[K,Boolean] = Map()
  private val theMap: Map[K,V] = Map()
  
  private object Locker
  
  private var pageFaults = 0
  private var references = 0 
  
  private def addNonExisting(key:K, value:V, fromSet:Boolean):Unit = {
    // The buffer is not full
    if(theBuffer.size < bufferSize){
       
     // The buffer is full - apply replacement policy
     }else{
       
       val oldKey:K = theBuffer.dequeue()
       val oldValue:Option[V] = theMap.remove(oldKey)
       
       if(modifiedMap.contains(oldKey) && !oldValue.isEmpty){
         // TODO: WRITE
       }
     }
    
     theBuffer += key
     theMap += key -> value
    
     // Mark as modified if the page fault originates from a set query
     if(fromSet) modifiedMap += key -> true
     
     pageFaults+=1
  }
  
  private def addExisting(key:K, value:V):Unit = {
    
    // Place the referenced value on the head of the queue
    theBuffer.pushToHead(key)
    
    // Mark as modified and update if value has changed
    if(!(theMap(key) equals value)){
      modifiedMap += theBuffer.front -> true
    	theMap += key -> value
    }
  }

  def write(key:String, value:String){
    //TODO
  }
  
  def read(key:String):Option[V] = {
    None:Option[V]
  }
  
  override def set(key:K, value:V): Unit = {
    references+=1
    
    if(theMap.contains(key))
      addExisting(key,value)
    else
      addNonExisting(key,value,true)
  }
  
  override def get(key:K): Option[V] = {
    references+=1
    
    // Check the disk if value not found in map 
    // and add to buffer if present on disk
    if(theMap.get(key).isEmpty){
      pageFaults += 1
      read(key.toString()) match{
        case Some(value) => addNonExisting(key,value,false)
                            Some(value)
        case None => None
      }
    }
    else{ 
      theBuffer.pushToHead(key)
      theMap.get(key) 
    }
  }

  override def delete(key:K):Boolean = {
    if(!theMap.get(key).isEmpty){
    	theBuffer.dequeueFirst(k => k equals key)
			val theValue = theMap.remove(key)
			write(key.toString(), DELETED_VALUE)
			true
    }else false //TODO
  }
  
  override def flushBuffer():Unit = {
    theBuffer.foreach(k => write(k.toString(), theMap.get(k).get.toString()))
    theBuffer.clear()
    theMap.clear()
  }
  
  override def faultRate():Double = {
    (pageFaults.toDouble/references)
  }
  
  def getBufferContents():String = {
    theBuffer.mkString("->")
  }
  
}

object LRUBuffer{
  def apply[K,V](size:Int) = new LRUBuffer[K,V](size)
}