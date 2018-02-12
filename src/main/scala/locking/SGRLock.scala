package locking

import scala.collection.mutable.{ListBuffer,Map}

class SGRLock {

  private val lockedRanges:Map[(String,String),Int] = Map()
  private val readWriteMap:Map[String,ReadWriteManager] = Map()
  
//  private val lockReadMonitor:Object = new Object()
//  private val unlockReadMonitor:Object = new Object()
//  private val lockWriteMonitor:Object = new Object()
//  private val unlockWriteMonitor:Object = new Object()
//  private val lockRangeMonitor:Object = new Object()
//  private val unlockRangeMonitor:Object = new Object()

  private def addIfAbsent(key:String):Unit = {
    if(!readWriteMap.contains(key)) readWriteMap += key -> new ReadWriteManager()
  }
  
  private def removeIfUnused(key:String):Unit = {
    if(readWriteMap.contains(key) && readWriteMap(key).isIdle) readWriteMap -= key
  }
  
  private def isInLockedRange(key:String):Boolean = {
    lockedRanges.exists(kv => kv._1._1 <= key && key <= kv._1._2 && kv._2 > 0)
  }
  
  private def writersInRange(start:String, end:String):Boolean = {
    readWriteMap.exists(kv => start <= kv._1 && kv._1 <= end && 
                        kv._2.writeRequests > 0 && kv._2.writers > 0)
  }
  
  @throws(classOf[InterruptedException])
  def lockRange(start:String, end:String):Unit = {
    synchronized{
      if(!lockedRanges.contains((start,end))) lockedRanges += ((start,end)) -> 0
      
      while(writersInRange(start,end)){
        wait()
      }
      val rangeQueries = lockedRanges((start,end))
      lockedRanges += ((start,end)) -> (rangeQueries + 1)
    }
  }
  
  @throws(classOf[InterruptedException])
  def unlockRange(start:String, end:String):Unit = {
    synchronized{
      val rangeLocks = lockedRanges((start,end))
      if(rangeLocks > 1) lockedRanges((start,end)) = rangeLocks - 1
      else lockedRanges.remove((start,end))
      notifyAll()
    }
  }
  
  @throws(classOf[InterruptedException])
  def lockRead(key:String):Unit = {
    synchronized{
      addIfAbsent(key)
      while(readWriteMap(key).writers > 0 || readWriteMap(key).writeRequests > 0){
        wait()
      }
      readWriteMap(key).addReader
    }
  }

  @throws(classOf[InterruptedException])
  def unlockRead(key:String):Unit = {
    synchronized{
      readWriteMap(key).removeReader
      removeIfUnused(key)
      notifyAll()
    }
  }

  @throws(classOf[InterruptedException])
  def lockWrite(key:String):Unit = {
    synchronized{
      addIfAbsent(key)
      
      readWriteMap(key).addWriteRequest
  
      while(readWriteMap(key).readers > 0 || 
            readWriteMap(key).writers > 0 || 
            isInLockedRange(key)){
        wait()
      }
      readWriteMap(key).removeWriteRequest
      readWriteMap(key).addWriter
    }
  }

  @throws(classOf[InterruptedException])
  def unlockWrite(key:String):Unit = {
    synchronized{
      readWriteMap(key).removeWriter
      removeIfUnused(key)
      notifyAll()
    }
  }
}

object SGRLock{
  def apply() = new SGRLock()
}