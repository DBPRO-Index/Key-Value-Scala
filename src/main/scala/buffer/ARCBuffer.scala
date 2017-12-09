package buffer

import scala.collection.mutable.{Map,Queue}

class ARCBuffer(bufferSize:Int) extends Buffer{
  
  private val L1: LRUQueue[String] = LRUQueue()
  private val L2: LRUQueue[String] = LRUQueue()
  
  // No occurrence map for L2 needed, since if the key is contained 
  // in the key/value map and not contained here
  // it has to be in L2. ghostL1 and ghostL2 hold only
  // entries which are NOT in the key/value map
  private val L1_OccurrenceMap: Map[String,Boolean] = Map() 
  
  private val ghostL1:Queue[String] = Queue()
  private val ghostL2:Queue[String] = Queue()
  private val ghostL1M:Map[String,Boolean] = Map()
  private val ghostL2M:Map[String,Boolean] = Map()
  
  private var L1Size = (bufferSize * 0.5).toInt
  private var L2Size = bufferSize - L1Size
  
  private var ghostL1Size = L1Size * 2
  private var ghostL2Size = L2Size * 2
  
  // The difference in size between L1 and L2
  // can be at most a factor of 3 (or some other number?!)
  def thresholdReached(forL1:Boolean):Boolean = {
    if(forL1){
      L1Size == L2Size * 3 
    }else{
      L2Size == L1Size * 3
    }
  }
  
  def addToL1(key:String):Unit = {
    
    L1_OccurrenceMap += key -> true
    L1.enqueue(key)
    
    if(L1.size > L1Size){
      val oldKey = L1.dequeue()
      
      ghostL1 += oldKey
      ghostL1M += oldKey -> true
      if(ghostL1.size > ghostL1Size){
        ghostL1M.remove(ghostL1.dequeue())
      }
      
      if(modifiedMap.contains(oldKey)){
        modifiedMap.remove(oldKey)
        fileManager.write(oldKey, theMap.remove(oldKey).getOrElse("DELETED_VALUE"))
      }
    }
  }
  
  def addToL2(key:String):Unit = {
    L1_OccurrenceMap.remove(key)
    L1.dequeueFirst(k => k equals key)
    L2.enqueue(key)
    if(L2.size > L2Size){
      val oldKey = L2.dequeue()
      
      ghostL2 += oldKey
      ghostL2M += oldKey -> true
      if(ghostL2.size > ghostL2Size){
        ghostL2M.remove(ghostL2.dequeue())
      }
      
      if(modifiedMap.contains(oldKey)){
        modifiedMap.remove(oldKey)
        fileManager.write(oldKey, theMap.remove(oldKey).getOrElse("DELETED_VALUE"))
      }
    }
  }
  
  // If the key is found in the ghost map for L1, then 
  // the total available space of L1 is increased
  // and the total space of L2 is decreased and vice versa
  def updateSizeIfNecessary(key:String):Unit = {
    if(ghostL1M.get(key).isDefined && !thresholdReached(true)){
      L1Size+=1
      L2Size-=1
    }
    else if(ghostL2M.get(key).isDefined && !thresholdReached(false)){
  	  L2Size+=1
      L1Size-=1
    }
  }
  
  override def set(key:String, value:String):Unit = {
    references+=1
    
    //Entry not contained in buffer
    if(theMap.get(key).isEmpty){
      pageFaults+=1
      
    	modifiedMap += key->true
      theMap += key -> value
      
      addToL1(key)
      updateSizeIfNecessary(key)
      
    // Entry contained but new value
    // If second reference, then move to L2
    // If already in L2, push to head
    }else{
      if(theMap(key) equals value){
        modifiedMap.remove(key)
      }else{
      	modifiedMap += key->true
        theMap += key -> value
      }
      if(L1_OccurrenceMap.contains(key)) addToL2(L1.dequeueFirst(k => k equals key).get)
      
      // We are sure that the key is in L1 hence ".get"
      else L2.pushToHead(key)
    }
  }
  
  override def get(key:String): Option[String] = {
    references+=1
    
    // No value found in buffer
    if(theMap.get(key).isEmpty){
      pageFaults+=1 
      
      val value = fileManager.read(key).get(key)
      if(value.isDefined && !(value.get equals DELETED_VALUE)){
        theMap += key -> value.get
        addToL1(key)
      }
      updateSizeIfNecessary(key)
      if(value.isDefined && (value.get equals DELETED_VALUE)) None 
      value
    }else{
      val valueOpt:Option[String] = theMap.get(key)
      
      if(L1_OccurrenceMap.contains(key)) addToL2(key)
      else L2.pushToHead(key)
      
      valueOpt
    }
  }
  
  override def delete(key:String): Boolean = {
    var listIsEmpty = false
    var valueAlreadyDeleted = false
    var successful = false;
    if(theMap.contains(key)){
      if(L1_OccurrenceMap.contains(key)){
        L1.dequeueFirst(k => k equals key)
        L1_OccurrenceMap.remove(key)
        
        if(ghostL1M.get(key).isEmpty){
        	ghostL1 += key
    			ghostL1M += key -> true
        }
        
      }else{
        L2.dequeueFirst(k => k equals key)
        if(ghostL2M.get(key).isEmpty){
        	ghostL2 += key
    			ghostL2M += key -> true
        }
      }
      
      /*val resGhostL1 = ghostL1.dequeueFirst(k => k equals key) 
      if(resGhostL1.isDefined) ghostL1M.remove(resGhostL1.get)
      
      val resGhostL2 = ghostL2.dequeueFirst(k => k equals key) 
      if(resGhostL2.isDefined) ghostL2M.remove(resGhostL2.get)*/
      
      modifiedMap.remove(key)
			theMap.remove(key)
			
      successful = true
    }else{
      val keyFromDisk = fileManager.read(key) 
      listIsEmpty = keyFromDisk.size == 0
      if(!listIsEmpty) valueAlreadyDeleted = (keyFromDisk(key) equals DELETED_VALUE)
      successful = !(listIsEmpty || valueAlreadyDeleted)
    }
    if(!valueAlreadyDeleted) fileManager.write(key, DELETED_VALUE)
    successful
  }
  
  override def flushBuffer():Unit = {
    super.flushBuffer()
    L1.clear()
    L2.clear()
    L1_OccurrenceMap.clear()
    ghostL1.clear()
    ghostL2.clear()
    ghostL1M.clear()
    ghostL2M.clear()
    L1Size = (bufferSize * 0.5).toInt
    L2Size = bufferSize - L1Size
  }
  
  override def toString():String = {
    var L1String = ""
    var L2String = ""
    L1.slice(0, 10).foreach(e => L1String += "(" + e + ":" + theMap(e) + ")->")
    L2.slice(0, 10).foreach(e => L2String += "(" + e + ":" + theMap(e) + ")->")
    if(!L1String.isEmpty()) L1String = "L1 (" + L1Size + "): " + L1String.substring(0,L1String.size-2) + (if(L1.size > 10) "...\n" else "\n")
    if(!L2String.isEmpty()) L2String = "L2 (" + L2Size + "): " + L2String.substring(0,L2String.size-2) + (if(L2.size > 10) "...\n" else "\n")
    
    val combined = L1String + L2String
    if(combined.isEmpty()) "Empty" else combined.substring(0,combined.size-1)
  }
  
}

object ARCBuffer{
  def apply(size:Int) = new ARCBuffer(size)
}