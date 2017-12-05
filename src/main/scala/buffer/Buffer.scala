package buffer

trait Buffer{
  def set(key:String, value:String):Unit
  def get(key:String): Option[String]
  def delete(key:String): Boolean
  def hitRate():Double
  def flushBuffer():Unit
}