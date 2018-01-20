package locking 

import java.lang.Runnable
import java.util.concurrent.Callable
import java.util.concurrent.{Executors,ExecutorService, Future}
import java.util.UUID

import configuration._
import util._
import util.query._
import buffer._

class Processor(callback:(UUID, String) => Unit){
  
  val executor:ExecutorService = Executors.newFixedThreadPool(Configuration.defaultThreadCount)
  val buffer:Buffer = LRUBuffer(Configuration.defaultBufferSize)
  val lock:SGRLock = SGRLock()
  
  private def sendToBuffer(query:Query):String = {
    var result = ""
    query.op match{
      case Operation.Range => {
        val queryCast = query.asInstanceOf[RangeQuery]
        lock.lockRange(queryCast.start, queryCast.end)
        result = buffer.range(queryCast.start, queryCast.end).toString()
        lock.unlockRange(queryCast.start, queryCast.end)
      }
      case Operation.Set => {
        val queryCast = query.asInstanceOf[SetQuery]
        lock.lockWrite(queryCast.key)
        result = buffer.set(queryCast.key, queryCast.value).toString()
        lock.unlockWrite(queryCast.key)
      }
      case Operation.Get => {
        val queryCast = query.asInstanceOf[GetQuery]
        lock.lockRead(queryCast.key)
        result = buffer.get(queryCast.key).toString()
        lock.unlockRead(queryCast.key)
      }
    }
    result
  }
  
	def processQuery(query:Query, keyID:UUID):Unit = {
    val taskResult:Future[String] = executor.submit(() => sendToBuffer(query))
    executor.execute(() => callback(keyID, taskResult.get))
  }
}

object Processor{
  def apply(callback:(UUID, String) => Unit) = new Processor(callback)
}