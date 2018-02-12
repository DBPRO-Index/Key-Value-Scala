package locking

class ReadWriteManager {
  private var _readers = 0;
  private var _writers = 0;
  private var _writeRequests = 0;
  
  def addReader:Unit = _readers += 1
  def removeReader:Unit = _readers = if(_readers == 0) 0 else _readers-1
  def addWriteRequest:Unit = _writeRequests += 1
  def removeWriteRequest:Unit = _writeRequests = if(_writeRequests == 0) 0 else _writeRequests-1
  def addWriter:Unit = _writers += 1
  def removeWriter:Unit = _writers = if(_writers == 0) 0 else _writers-1

  def writers = _writers
  def readers = _readers
  def writeRequests = _writeRequests
  
  def isIdle = _readers == 0 && _writeRequests == 0 && _writers == 0
}