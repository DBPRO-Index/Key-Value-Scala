import scala.collection.mutable.ListBuffer
import org.junit.Test
import org.junit.Assert._

class LRUBufferTest {
  
   @Test def faultRateTest{
     val testBuffer:Buffer[Int,String] = new LRUBuffer(3,3,3)
     testBuffer.set(1, "a")
     testBuffer.set(2, "b")
     testBuffer.set(3, "c")
     assertEquals(1, testBuffer.faultRate(),0.002)
     testBuffer.get(3)
     assertEquals(0.75, testBuffer.faultRate(),0.002)
     testBuffer.get(5)
     assertEquals(0.8, testBuffer.faultRate(),0.002)
     testBuffer.set(3, "b")
     assertEquals(0.666, testBuffer.faultRate(),0.002)
     testBuffer.set(1, "a")
     assertEquals(0.571, testBuffer.faultRate(),0.002)
   }
   
   @Test def bufferContentsTest{
     val testBuffer = new LRUBuffer[Int,String](3,3,3)
     testBuffer.set(1, "a")
     testBuffer.set(2, "b")
     testBuffer.set(3, "c")
     assertEquals("(key:1|m:y)->(key:2|m:y)->(key:3|m:y)",testBuffer.getBufferContents())
     testBuffer.set(1, "a")
     assertEquals("(key:2|m:y)->(key:3|m:y)->(key:1|m:y)",testBuffer.getBufferContents())
     testBuffer.set(3, "d")
     assertEquals("(key:2|m:y)->(key:1|m:y)->(key:3|m:y)",testBuffer.getBufferContents())
     testBuffer.get(2)
     assertEquals("(key:1|m:y)->(key:3|m:y)->(key:2|m:y)",testBuffer.getBufferContents())
     testBuffer.delete(1)
     assertEquals("(key:3|m:y)->(key:2|m:y)",testBuffer.getBufferContents())
   }
  
}