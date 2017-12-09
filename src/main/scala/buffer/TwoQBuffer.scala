package buffer

import scala.collection.mutable.{Queue, Map, Set}

import filemanager.{FMA,FileManager}

class TwoQBuffer(bufferSize:Int) extends Buffer{
  
  private val Am: LRUQueue[String] = LRUQueue()
  private val A1In: Queue[String] = Queue()
  private val A1Out: Queue[String] = Queue()
  
  private val AmOccurrenceMap: Map[String,Integer] = Map() 
  private val A1InOccurrenceMap: Map[String,Integer] = Map() 
  private val A1OutOccurrenceMap: Map[String,Integer] = Map() 
  
  private val KIn = (bufferSize * 0.25).toInt
  private val KOut = (bufferSize * 0.5).toInt
  private val Km = bufferSize - KIn - KOut
  
  private def putInFreeSlot(key:String):Boolean = {
    var spotFound = false
    if(A1In.size <= KIn){
      A1In += key
      spotFound = true
    }else if(A1Out.size <= KOut){
      A1Out += key
      spotFound = true
    }else if(Am.size <= Km){
      Am += key
      spotFound = true
    }
    spotFound
  }
  
  private def notInQueues(key:String):Boolean = {
    AmOccurrenceMap(key) == 0 && A1InOccurrenceMap(key) == 0 && A1OutOccurrenceMap(key) == 0 
  }
  
  // Dequeues the specified queue and sends the   
  // removed entry to the fileManager if required
  private def removeFrom(queue:Queue[String], occurenceMap:Map[String,Integer], isA1In:Boolean):Unit = {
    val oldKey = queue.dequeue()
    occurenceMap += oldKey -> (occurenceMap(oldKey) - 1)
    
    isA1In match{
      case false => {
        if(notInQueues(oldKey)){
          val oldValue:Option[String] = theMap.get(oldKey)
          theMap.remove(oldKey)
          if(modifiedMap.contains(oldKey)){
            fileManager.write(oldKey, oldValue.getOrElse(DELETED_VALUE))
            modifiedMap.remove(oldKey)
          }
        }
      }
      case true => {
        
        val oldKey = A1In.dequeue()
        A1Out += oldKey
        A1OutOccurrenceMap += oldKey -> (A1OutOccurrenceMap(oldKey) + 1)
        A1InOccurrenceMap += oldKey -> (A1InOccurrenceMap(oldKey) - 1)

        if(A1Out.size > KOut) removeFrom(A1Out, A1OutOccurrenceMap, false)
      }
    }
  }
  
  private def reclaimFor(key:String):Unit = {
    if(!putInFreeSlot(key)){
      if(A1In.size > KIn){
        val oldKey = A1In.dequeue()
        A1Out += oldKey
        A1OutOccurrenceMap += oldKey -> (A1OutOccurrenceMap(oldKey) + 1)
        A1InOccurrenceMap += oldKey -> (A1InOccurrenceMap(oldKey) - 1)
        if(A1Out.size > KOut) removeFrom(A1Out, A1InOccurrenceMap, false)
        key +=: A1In
        A1InOccurrenceMap += key -> (A1InOccurrenceMap(key) + 1)
      }else{
        removeFrom(Am, AmOccurrenceMap, false)
        key +=: Am
        AmOccurrenceMap += key -> (AmOccurrenceMap(key) + 1)
      }
    }
  }
  
  private def manageQueues(key:String):Unit = {
    if(AmOccurrenceMap(key) > 0) Am.pushToHead(key)
    else if(A1OutOccurrenceMap(key) > 0){
      reclaimFor(key)
      Am += key
      AmOccurrenceMap += key -> (AmOccurrenceMap(key) + 1)
      if(Am.size > Km) removeFrom(Am, AmOccurrenceMap, false)
    }else if(A1InOccurrenceMap(key) == 0){
      reclaimFor(key)
      if(A1In.size > KIn) removeFrom(A1In, A1InOccurrenceMap, true)
      A1In += key
      A1InOccurrenceMap += key -> (A1InOccurrenceMap(key) + 1)
    }
  }
  
  override def set(key:String, value:String):Unit = {
    references+=1
    
    //Entry not contained in buffer
    if(theMap.get(key).isEmpty){
      pageFaults+=1
    	modifiedMap += key->true
    	
      AmOccurrenceMap += key->0
      A1InOccurrenceMap += key->0
      A1OutOccurrenceMap += key->0
    	
  	// Entry contained but value changed
    }else if(!(theMap(key) equals value)){
    	modifiedMap += key->true
    }else modifiedMap.remove(key)
    
    theMap += key->value
    manageQueues(key)
  }
   
  override def get(key:String): Option[String] = {
    references+=1
    
    // No value found in buffer
    if(theMap.get(key).isEmpty){
      pageFaults+=1
      
      val value = fileManager.read(key).get(key)
      if(value.isDefined && !(value.get equals DELETED_VALUE)){
      
        AmOccurrenceMap += key->0
        A1InOccurrenceMap += key->0
        A1OutOccurrenceMap += key->0
        
        theMap += key->value.get
        manageQueues(key)
      }
      if(value.isDefined && (value.get equals DELETED_VALUE)) None else value
    }else{
      val valueOpt:Option[String] = theMap.get(key)
      manageQueues(key)
      valueOpt
    }
  }
  
  override def delete(key:String): Boolean = {
    var listIsEmpty = false
    var valueAlreadyDeleted = false
    var successful = false;
    if(theMap.contains(key)){
      Am.dequeueAll(k => k equals key)
      A1In.dequeueAll(k => k equals key)
      A1Out.dequeueAll(k => k equals key)
      modifiedMap.remove(key)
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
    Am.clear()
    A1In.clear()
    A1Out.clear()
  }
  
  override def toString():String = {
    var amq = ""
    var a1o = ""
    var a1i = ""
    
    Am.slice(0, 10).foreach(x => amq+="(" + x + ":" + theMap(x) + ")->")
    A1Out.slice(0, 10).foreach(x => a1o+="(" + x + ":" + theMap(x) + ")->")
    A1In.slice(0, 10).foreach(x => a1i+="(" + x + ":" + theMap(x) + ")->")
    
    if(!(amq equals "")) amq = "Am (" + Km + "): " + amq.substring(0,amq.size-2) + (if(Am.size > 10) "...\n" else "\n")
    if(!(a1o equals "")) a1o = "A1Out(" + KOut + "):" + a1o.substring(0,a1o.size-2) + (if(A1Out.size > 10) "...\n" else "\n")
    if(!(a1i equals "")) a1i = "A1In(" + KIn + "):" + a1i.substring(0,a1i.size-2) + (if(A1In.size > 10) "...\n" else "\n")
    
    val combined = amq + a1o + a1i
    if(combined equals "") "Empty" else combined.substring(0,combined.size-1)
  }
}

object TwoQBuffer{
  def apply(size:Int) = new TwoQBuffer(size)
}