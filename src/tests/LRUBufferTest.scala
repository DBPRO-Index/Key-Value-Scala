package tests

import org.junit.Test
import org.junit.Assert._
import buffer.Buffer
import buffer.LRUBuffer

class LRUBufferTest {
  
   @Test def faultRateTest{
     val testBuffer:Buffer[Int,String] = new LRUBuffer(3)
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
     val testBuffer = new LRUBuffer[Int,String](3)
     testBuffer.set(1, "a")
     testBuffer.set(2, "b")
     testBuffer.set(3, "c")
     assertEquals("1->2->3",testBuffer.getBufferContents())
     testBuffer.set(1, "a")
     assertEquals("2->3->1",testBuffer.getBufferContents())
     testBuffer.set(3, "d")
     assertEquals("2->1->3",testBuffer.getBufferContents())
     testBuffer.get(2)
     assertEquals("1->3->2",testBuffer.getBufferContents())
     testBuffer.delete(1)
     assertEquals("3->2",testBuffer.getBufferContents())
   }
  
}