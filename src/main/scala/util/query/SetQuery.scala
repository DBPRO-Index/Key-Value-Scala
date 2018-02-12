package util.query

import util.Operation

class SetQuery(_key:String,_value:String) extends Query(Operation.Set) {
  def key = _key
  def value = _value
}