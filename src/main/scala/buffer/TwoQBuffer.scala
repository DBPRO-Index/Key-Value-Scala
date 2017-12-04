package buffer

import scala.collection.mutable.{Queue,ListBuffer,Map}

class TwoQBuffer[K,V](bufferSize:Int) extends Buffer[K,V]{
  
  private val modifiedMap: Map[K,Int] = Map()
  private val theMap: Map[K,V] = Map.empty
  
  private val Am: LRUQueue[K] = LRUQueue()
  private val A1In: Queue[K] = Queue()
  private val A1Out: Queue[K] = Queue()
  
  private val KIn = bufferSize * 0.25
  private val KOut = bufferSize * 0.5
  private val Km = bufferSize - KIn - KOut
  
  private val DELETED_VALUE = "NULL"
  
  private var pageFaults = 0
  private var references = 0 
  
  def putInFreeSlot(key:K):Boolean = {
    var spotFound = false
    if(A1In.size < KIn){
      A1In += key
      spotFound = true
    }else if(A1Out.size < KOut){
      A1Out += key
      spotFound = true
    }else if(Am.size < Km){
      Am += key
      spotFound = true
    }
    spotFound
  }
  
  def reclaimFor(key:K):Unit = {
    if(!putInFreeSlot(key)){
      if(A1In.size > KIn){
        val oldKey:K = A1In.dequeue()
        A1Out += oldKey
        if(A1Out.size > KOut){
          A1Out.dequeue()
          //TODO: write?!
        }
        key +=: A1In
      }else{
        val oldKey:K = Am.dequeue()
        //TODO: write?!
        key +=: Am
      }
    }
  }
  
  def manageQueues(key:K):Unit = {
    if(Am.contains(key)) Am.pushToHead(key)
    else if(A1Out.contains(key)){
      reclaimFor(key)
      Am += key
    }else if(!A1In.contains(key)){
      reclaimFor(key)
      A1In += key
    }
  }
  
  override def set(key:K, value:V):Unit = {
    theMap += key->value
    manageQueues(key)
  }
   
  override def get(key:K): Option[V] = {
    if(theMap.get(key).isEmpty){
      pageFaults+=1
      None //TODO
    }else{
      val valueOpt:Option[V] = theMap.get(key)
      manageQueues(key)
      valueOpt
    }
  }
  override def delete(key:K): Boolean = {false}
  override def faultRate():Double = {1.0}
  override def flushBuffer():Unit = {}
  
}

object TwoQBuffer{
  def apply[K,V](size:Int) = new TwoQBuffer[K,V](size)
}