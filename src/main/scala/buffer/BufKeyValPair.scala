package buffer

class BufKeyValPair(key:String, value:String){
  def getValue() = value
  def getKey() = key
  override def toString() = {
    key + ":" + value
  }
}