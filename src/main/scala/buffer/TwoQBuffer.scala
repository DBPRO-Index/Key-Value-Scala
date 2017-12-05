package buffer

import scala.collection.mutable.{Queue, Map, Set}

import filemanager.{FMA,FileManager}

class TwoQBuffer(bufferSize:Int) extends Buffer{
  
  private val modifiedMap: Map[String,Boolean] = Map()
  private val theMap: Map[String,String] = Map.empty
  private val fileManager: FMA = new FileManager()
  
  private val Am: LRUQueue[String] = LRUQueue()
  private val A1In: Queue[String] = Queue()
  private val A1Out: Queue[String] = Queue()
  
  private val AmOccurenceMap: Map[String,Integer] = Map() 
  private val A1InOccurenceMap: Map[String,Integer] = Map() 
  private val A1OutOccurenceMap: Map[String,Integer] = Map() 
  
  private val KIn = (bufferSize * 0.25).toInt
  private val KOut = (bufferSize * 0.5).toInt
  private val Km = bufferSize - KIn - KOut
  
  private val DELETED_VALUE = "NULL"
  private val NOT_PRESENT = "-NULL-"
  
  private var pageFaults = 0
  private var references = 0 
  
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
    AmOccurenceMap(key) == 0 && A1InOccurenceMap(key) == 0 && A1OutOccurenceMap(key) == 0 
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
          theMap += oldKey -> NOT_PRESENT
          if(modifiedMap(oldKey) == true){
            fileManager.write(oldKey, oldValue.getOrElse(DELETED_VALUE))
            modifiedMap += oldKey -> false
          }
        }
      }
      case true => {
        
        val oldKey = A1In.dequeue()
        A1Out += oldKey
        A1OutOccurenceMap += oldKey -> (A1OutOccurenceMap(oldKey) + 1)
        A1InOccurenceMap += oldKey -> (A1InOccurenceMap(oldKey) - 1)

        if(A1Out.size > KOut) removeFrom(A1Out, A1OutOccurenceMap, false)
      }
    }
  }
  
  private def reclaimFor(key:String):Unit = {
    if(!putInFreeSlot(key)){
      if(A1In.size > KIn){
        val oldKey = A1In.dequeue()
        A1Out += oldKey
        A1OutOccurenceMap += oldKey -> (A1OutOccurenceMap(oldKey) + 1)
        A1InOccurenceMap += oldKey -> (A1InOccurenceMap(oldKey) - 1)
        if(A1Out.size > KOut) removeFrom(A1Out, A1InOccurenceMap, false)
        key +=: A1In
        A1InOccurenceMap += key -> (A1InOccurenceMap(key) + 1)
      }else{
        removeFrom(Am, AmOccurenceMap, false)
        key +=: Am
        AmOccurenceMap += key -> (AmOccurenceMap(key) + 1)
      }
    }
  }
  
  private def manageQueues(key:String):Unit = {
    if(AmOccurenceMap(key) > 0) Am.pushToHead(key)
    else if(A1OutOccurenceMap(key) > 0){
      reclaimFor(key)
      Am += key
      AmOccurenceMap += key -> (AmOccurenceMap(key) + 1)
      if(Am.size > Km) removeFrom(Am, AmOccurenceMap, false)
    }else if(A1InOccurenceMap(key) == 0){
      reclaimFor(key)
      if(A1In.size > KIn) removeFrom(A1In, A1InOccurenceMap, true)
      A1In += key
      A1InOccurenceMap += key -> (A1InOccurenceMap(key) + 1)
    }
  }
  
  override def set(key:String, value:String):Unit = {
    references+=1
    
    //Entry not contained in buffer
    if(theMap.get(key).isEmpty || (theMap(key) equals NOT_PRESENT)){
      pageFaults+=1
    	modifiedMap += key->true
    	
      AmOccurenceMap += key->0
      A1InOccurenceMap += key->0
      A1OutOccurenceMap += key->0
    	
  	// Entry contained but value changed
    }else if(!(theMap(key) equals value)){
    	modifiedMap += key->true
    }else modifiedMap += key->false
    theMap += key->value
    manageQueues(key)
  }
   
  override def get(key:String): Option[String] = {
    references+=1
    
    // No value found in buffer
    if(theMap.get(key).isEmpty || (theMap(key) equals NOT_PRESENT)){
      pageFaults+=1
      val value = fileManager.read(key).get(key)
      if(!value.isEmpty){
        AmOccurenceMap += key->0
        A1InOccurenceMap += key->0
        A1OutOccurenceMap += key->0
        
        theMap += key->value.get
        manageQueues(key)
      }
      value
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
    if(!theMap.get(key).isEmpty){
      Am.dequeueAll(k => k equals key)
      A1In.dequeueAll(k => k equals key)
      A1Out.dequeueAll(k => k equals key)
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

  override def hitRate():Double = {
    val fr = (pageFaults.toDouble/references)
    if(fr.isNaN()) 0.0 else 1 - fr
  }
  
  override def flushBuffer():Unit = {
    pageFaults = 0
    references = 0
    
    theMap.foreach(x => fileManager.write(x._1, x._2))
    theMap.clear()
    
    Am.clear()
    A1In.clear()
    A1Out.clear()
  }
  
  override def toString():String = {
    var amq = ""
    var a1o = ""
    var a1i = ""
    Am.foreach(x => amq+="(" + x + ":" + theMap(x) + ")->")
    A1Out.foreach(x => a1o+="(" + x + ":" + theMap(x) + ")->")
    A1In.foreach(x => a1i+="(" + x + ":" + theMap(x) + ")->")
    
    if(!(amq equals "")) amq = "Am (" + Km + "): " + amq.substring(0,amq.size-2) + "\n"
    if(!(a1o equals "")) a1o = "A1Out(" + KOut + "):" + a1o.substring(0,a1o.size-2) + "\n"
    if(!(a1i equals "")) a1i = "A1In(" + KIn + "):" + a1i.substring(0,a1i.size-2) + "\n"
    
    val combined = amq + a1o + a1i
    if(combined equals "") "Empty" else combined.substring(0,combined.size-1)
  }
  
}



object TwoQBuffer{
  def apply(size:Int) = new TwoQBuffer(size)
}