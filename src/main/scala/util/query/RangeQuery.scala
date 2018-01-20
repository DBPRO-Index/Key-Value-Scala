package util.query

import util.Operation

class RangeQuery(_start:String, _end:String) extends Query(Operation.Range) {
  def start = _start
  def end = _end
}



