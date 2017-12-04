package buffer

import scala.collection.mutable.Queue
import scala.collection.mutable.ListBuffer
import scala.collection.mutable
import java.util.concurrent._

class LRUBuffer[K,V](bufferSize:Int) extends Buffer[K,V]{
  
  private val DELETED_VALUE = "NULL"
  
  private val theBuffer: Queue[KeyWrapper[K]] = Queue()
  private val theMap: mutable.Map[K,V] = mutable.Map.empty
  
  private object Locker
  
  private var pageFaults = 0
  private var references = 0 
  
  private def pushToHead(key:K):Unit = {
    theBuffer.enqueue(theBuffer.dequeueAll(k => k.getKey() equals key)(0))
  }
  
  private def addNonExisting(key:K, value:V, fromSet:Boolean):Unit = {
    val keyWrapper = new KeyWrapper(key)
    
    // The buffer is not full
    if(theBuffer.size < bufferSize){
       
     // The buffer is full - apply replacement policy
     }else{
       
       val oldKey:KeyWrapper[K] = theBuffer.dequeue()
       val oldValue:Option[V] = theMap.remove(oldKey.getKey())
       
       if(oldKey.isModified() && !oldValue.isEmpty){
         // TODO: WRITE
       }
     }
    
     theBuffer += keyWrapper
     theMap += key -> value
    
     // Mark as modified if the page fault originates from a set query
     if(fromSet) keyWrapper.modify()
     
     pageFaults+=1
  }
  
  private def addExisting(key:K, value:V):Unit = {
    
    // Place the referenced value on the head of the queue
    pushToHead(key)
    
    // Mark as modified and update if value has changed
    if(!(theMap(key) equals value)){
      theBuffer.front.modify()
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
    if(theMap.contains(key))
      addExisting(key,value)
    else
      addNonExisting(key,value,true)
      
    references+=1
  }
  
  override def get(key:K): Option[V] = {
    references+=1
    
    // Check the disk if value not found in map 
    // and add to buffer if present on disk
    if(theMap.get(key).isEmpty){
      pageFaults += 1
      read(key.toString()) match{
        case Some(value) => {
          addNonExisting(key,value,false)
          Some(value)
        }
        case None =>{
          None
        }
      }
    }
    else{ 
      pushToHead(key)
      theMap.get(key) 
    }
  }

  override def delete(key:K):Boolean = {
    if(!theMap.get(key).isEmpty){
    	theBuffer.dequeueAll(k => k.getKey() equals key)(0)
			val theValue = theMap.remove(key)
			write(key.toString(), DELETED_VALUE)
			true
    }
    false
  }
  
  override def flushBuffer():Unit = {
    theBuffer.foreach(k => write(k.getKey().toString(), theMap.get(k.getKey()).get.toString()))
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