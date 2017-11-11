trait Buffer[K,V] {
  def set(key:K, value:V):Unit
  def get(key:K): Option[V]
  def delete(key:K): Boolean
  def faultRate():Double
}