package filemanager

import scala.io.Source
import java.io.PrintWriter

import scala.collection.mutable

class ScriptDB(val db: String = "database") extends FMA {
  def insertFromFile(filepath: String = "src/test/resources/scriptDB/database"): Unit = {
    val source = Source.fromFile(filepath, "UTF-8")
    val lineIterator = source.getLines

    val out = new PrintWriter(db)
    for (l <- lineIterator) {
      val kv = l.split(":").toList
      out.println(s"${kv.head}:${kv.tail.mkString(":")}")
    }

    out.close()
  }

  def write(key: String, value: String): Unit = {
    val source = Source.fromFile(db, "UTF-8")
    val lineIterator = source.getLines
    val db_cache = scala.collection.mutable.Map[String, String]()

    for (l <- lineIterator) {
      val pair = l.split(", ")
      db_cache(pair(0)) = pair(1)
    }
    db_cache(key) = value

    val out = new PrintWriter(db)
    for((k,v) <- db_cache) out.println(s"$k, $v")
    out.close()
  }

  def read(key: String): mutable.Map[String, String] = {
    val source = Source.fromFile(db, "UTF-8")
    val lineIterator = source.getLines
    val db_cache = scala.collection.mutable.Map[String, String]()

    for (l <- lineIterator) {
      val kv = l.split(":").toList
      db_cache(kv.head) = kv.tail.mkString(":")
    }
    mutable.Map[String, String](key -> db_cache.getOrElse(key, ""))
  }
  def read(lower: String, upper: String): mutable.Map[String, String] = {
    mutable.Map[String, String]()
  }
  def close(): Unit = {
    println(s"closing $db")
  }
}



