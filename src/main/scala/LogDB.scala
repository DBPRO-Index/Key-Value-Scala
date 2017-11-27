import scala.io.Source

object LogDB extends App {

  val blockSize = 64000
  val memTable = new Heap(128000000)

  memTable.load("traffic_data.json")

  println(get("2015-05-29-000709"))

  memTable.save()


  def get(key: String): String = {
    memTable.get(key)

    // TODO if not found search SSTs
  }
}