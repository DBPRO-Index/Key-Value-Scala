import scala.io.Source
import java.io.PrintWriter


def dbInsertFromFile(file: String, db:String = "database") = {
	val source = Source.fromFile(file, "UTF-8")
	val lineIterator = source.getLines
	var id = 0 

	val out = new PrintWriter(db)
	for (l <- lineIterator) {
		out.println(s"$id, $l")
		id +=  1
	}
	out.close	
}

def dbSet(key: String, value: String, db:String = "database") = {
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
	out.close
}

def dbGet(key: String, db:String = "database") = {
	val source = Source.fromFile(db, "UTF-8")
	val lineIterator = source.getLines
	val db_cache = scala.collection.mutable.Map[String, String]()

	for (l <- lineIterator) {
		val pair = l.split(", ")
		db_cache(pair(0)) = pair(1)
	}
	db_cache(key)
}

// Measure Time of block in ms
// usage: time { block }
def time[R](block: => R): R = {  
    val t0 = System.currentTimeMillis()
    val result = block    // call-by-name
    val t1 = System.currentTimeMillis()
    println(s"Elapsed time: ${t1 - t0}ms")
    result
}