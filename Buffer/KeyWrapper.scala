

class KeyWrapper[K](key:K) {
  def getKey() = key
  var modified = false
  def modify() = modified = true
  def unmodify() = modified = false
  def isModified() = modified
}