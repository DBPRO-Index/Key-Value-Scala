package util

import org.junit.Test
import org.junit.Assert._

class DataGeneratorTest {
  @Test def test{
    for(i <- 1 to 4){
      DataGenerator.generateData(5)
    }
    print(DataGenerator.getData(3))
  }
}