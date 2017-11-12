import scala.collection.mutable.Queue
import scala.collection.mutable.ListBuffer
import scala.collection.mutable

class LRUBuffer[K,V](bufferSize:Int, writeThreshold:Int, deleteThreshold:Int) extends Buffer[K,V]{
  
  private val theBuffer: Queue[KeyWrapper[K]] = Queue()
  private val theMap: mutable.Map[K,V] = mutable.Map.empty
  private val deleteList = new ListBuffer[K]()
  
  private var modified = 0
  private var currentSize = 0
  private var pageFaults = 0
  private var references = 0 
  
  def getBufferContents():String = {
    theBuffer.mkString("->")
  }
  
  private def pushToHead(key:K):Unit = {
    theBuffer.enqueue(theBuffer.dequeueAll(k => k.getKey() equals key)(0))
  }
  
  private def addNonExisting(key:K, value:V):Unit = {
    val keyWrapper = new KeyWrapper(key)
    
    // The case when the buffer 
    // is still not full 
    if(currentSize < bufferSize){
       theMap += key -> value
       theBuffer += keyWrapper
       currentSize += 1
       
     // The buffer is full 
     // Apply replacement policy
     }else{
       theMap.remove(theBuffer.dequeue().getKey())
       theBuffer += keyWrapper
       theMap += key -> value
     }
    
     // Always mark as modified, since a check should be made to
     // see if the pair exists and if so, if the value has changed
     keyWrapper.modify()
     modified+=1
     
     if(shouldWrite) writeToDisk()
     
     pageFaults+=1
  }
  
  private def addExisting(key:K, value:V):Unit = {
    
    // Place the referenced value on the head of the queue
    pushToHead(key)
    
    // Mark as modified if the 
    // value has changed
    if(!(theMap(key) equals value)){
      
      theBuffer.front.modify()
      modified+=1
      
    	if(shouldWrite) writeToDisk() // TODO: Implement as asynchronous operation
    	
    	theMap += key -> value
    }
  }
  
  private def shouldDelete():Boolean = {
    deleteList.size >= deleteThreshold
  }
  
  private def shouldWrite():Boolean = {
    modified >= writeThreshold
  }
  
  private def deleteFromDisk():Unit = {
    deleteList.foreach(K => {}) // TODO: Actually access the disk
    deleteList.clear()
  }
  
  private def writeToDisk():Unit = {
    // DO STUFF
  }
  
  private def readFromDisk(key:K):Option[V] = {
    None
  }
  
  override def set(key:K, value:V): Unit = {
    if(theMap.contains(key))
      addExisting(key,value)
    else
      addNonExisting(key,value)
      
    references+=1
  }
  
  override def get(key:K): Option[V] = {
    references+=1
    
    // Check the disk if value not found in map 
    // and add to buffer if present on disk
    if(theMap.get(key).isEmpty){
      readFromDisk(key) match{
        case Some(value) => {
          addNonExisting(key,value)
          Some(value)
        }
        case None =>{
          pageFaults += 1
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
    val keyWrapper = theBuffer.dequeueAll(k => k.getKey() equals key)(0)
    theMap.remove(keyWrapper.getKey())
    deleteList+=keyWrapper.getKey()
    if(shouldDelete) deleteFromDisk()
    false //Boolean value should be based on the success of deletion
  }
  
  override def faultRate():Double = {
    (pageFaults.toDouble/references)
  }
  
}