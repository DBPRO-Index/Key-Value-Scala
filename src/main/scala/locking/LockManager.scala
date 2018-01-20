//package locking
//
//import java.util.concurrent.locks.ReentrantReadWriteLock
//
//import java.util.concurrent.ConcurrentHashMap
//
//import scala.collection.concurrent.Map
//import scala.collection.mutable.PriorityQueue
//import scala.collection.JavaConverters._
//

//TODO: Remove
class LockManager {
//  
//  private val locker = new Object()
//  private val keyToLockMap: Map[String,ReentrantReadWriteLock] = new ConcurrentHashMap().asScala
//  private var firstKey = ""
//  private var lastKey = ""
//  
//  private def updateFirstLast(key:String):Unit = {
//    firstKey = if(firstKey.isEmpty()) key else (if(firstKey > key) key else firstKey)
//    lastKey = if(lastKey.isEmpty()) key else (if(lastKey < key) key else lastKey)
//  }
//  
//  private def deleteLockIfUnused(key:String):Unit = {
//    locker.synchronized{
//      if(!acquireLock(key).hasQueuedThreads()) keyToLockMap.remove(key)
//      if(key.equals(firstKey)){} 
//      if(key.equals(lastKey)){}  
//    }
//  }
//  
//  private def acquireLock(key:String):ReentrantReadWriteLock = {
//      if(!keyToLockMap.contains(key)){
//        // Make sure that only one lock is created
//        // if condition happens to hold for another thread
//        // before the new lock has been added
//        locker.synchronized{
//          updateFirstLast(key)
//        	var newLock = new ReentrantReadWriteLock()
//    			if(!keyToLockMap.contains(key)){
//    			  keyToLockMap += key -> newLock
//    			  newLock
//    			}else keyToLockMap(key)
//        }
//      }else keyToLockMap(key)
//    
//  }
//  
//  private def writeCollisions(start:String, end:String):Int = {
//    
//  }
//   
//  def lockRange(start:String, end:String):Unit = {
//    // Possible collisions with individual entries
//    if((start >= firstKey && start <= lastKey) ||
//      (end >= firstKey && end <= lastKey)){
//      
//      for((k,v) <- keyToLockMap){
//        if(k >= start  && k <= end && v.isWriteLocked()){
//          
//        }
//      }
//    }
//  }
//  
//  private def setLock(key:String):Unit = {
//    acquireLock(key).writeLock().lock()
//  }
//  
//  private def getLock(key:String):Unit = {
//    acquireLock(key).readLock().lock() 
//  }
//  
//  private def setUnlock(key:String):Unit = {
//    acquireLock(key).writeLock().unlock()
//    deleteLockIfUnused(key)
//  }
//  
//  private def getUnlock(key:String):Unit = {
//    acquireLock(key).readLock().unlock()
//    deleteLockIfUnused(key)
//  }
//  
//  
}