package util.query

import util.Operation

abstract class Query(_op:Operation.Value) {
  def op = _op
}