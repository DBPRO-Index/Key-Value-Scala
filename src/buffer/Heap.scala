package buffer

trait Heap {
  def write(key:Int, value:String)
  def read(key:Int)
  def read(from:Int, to:Int)
  def close()
}