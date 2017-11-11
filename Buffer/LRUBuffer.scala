import scala.collection.mutable.Queue
import scala.collection.mutable

class LRUBuffer[K,V](bufferSize:Long, writeThreshold:Long) extends Buffer[K,V]{
  
  private val theBuffer: Queue[KeyWrapper[K]] = Queue()
  private val theMap: mutable.Map[K,V] = mutable.Map.empty

  private var modified = 0
  private var currentSize = 0
  private var pageFaults = 0
  private var references = 0 
  
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
     
     pageFaults+=1
  }
  
  private def addExisting(key:K, value:V):Unit = {
    
    // Place the referenced value on the head of the queue
    val keyWrapper = theBuffer.dequeueAll(k => k equals key)(0)
    theBuffer.enqueue(keyWrapper)
    
    // Mark as modified if the 
    // value has changed
    if(!(theMap(key) equals value)){
      
      theBuffer.front.modify()
      modified+=1
      
    	if(shouldWrite) writeToDisk() // TODO: Implement as asynchronous operation
    	
    	theMap += key -> value
    }
  }
  
  private def shouldWrite():Boolean = {
    modified >= writeThreshold
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
        case None => None
      }
    }
    else theMap.get(key)  
  }

  override def delete(key:K):Boolean = {
    false
  }
  
  override def faultRate():Double = {
    pageFaults/references
  }
  
}