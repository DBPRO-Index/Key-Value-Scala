

class KeyWrapper[K](key:K) {
  def getKey() = key
  var modified = false
  def modify() = modified = true
  def unmodify() = modified = false
  def isModified() = modified
  override def toString() = {
    "(key:" + key.toString() + "|" + (if(modified) "m:y)" else "m:n)")
  }
}