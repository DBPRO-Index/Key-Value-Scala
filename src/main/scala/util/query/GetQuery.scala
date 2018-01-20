package util.query

import util.Operation

class GetQuery(_key:String) extends Query(Operation.Get) {
  def key = _key
}