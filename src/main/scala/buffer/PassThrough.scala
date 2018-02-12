package buffer

class PassThrough extends Buffer{
  def delete(key:String): Boolean = {false}
  override def toString():String = {
    "None"
  }
}
object PassThrough{
  def apply() = new PassThrough()
  
}