package buffer

import filemanager.{FMA,FileManager}
import scala.collection.mutable.{Map,ListBuffer}

trait Buffer{
  protected val theMap: Map[String,String] = Map()
  protected val modifiedMap: Map[String,Boolean] = Map()
  protected val fileManager: FMA = new FileManager()
  
  protected val DELETED_VALUE = "NULL"
  
  protected var references = 0 
  protected var pageFaults = 0
  
  def set(key:String, value:String):Unit
  def get(key:String): Option[String]
  def range(lower:String, upper:String):ListBuffer[String] = {
    val list = ListBuffer[String]()
    
    modifiedMap.foreach(tuple => {
      if((tuple._1 >= lower) && (tuple._1 <= upper)){
        fileManager.write(tuple._1, theMap(tuple._1))
        modifiedMap.remove(tuple._1)
      }
    })
    fileManager.read(lower, upper).foreach(tuple => {
      set(tuple._1,tuple._2)
      list += tuple._2
    })
    list
  }
  def delete(key:String): Boolean
  
  def flushBuffer():Unit = {
		references = 0
    pageFaults = 0
    
    theMap.foreach(x => if(modifiedMap.get(x._1).isDefined) fileManager.write(x._1, x._2))
    theMap.clear()
    modifiedMap.clear()
    
    fileManager.close()
  }
  
  def hitRate():Double = {
    val fr = (pageFaults.toDouble/references)
    if(fr.isNaN()) 0.0 else 1 - fr
  }
}