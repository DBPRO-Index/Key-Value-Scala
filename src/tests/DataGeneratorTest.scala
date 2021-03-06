package tests

import org.junit.Test
import org.junit.Assert._
import util.DataGenerator

class DataGeneratorTest {
  @Test def getNewestTest{
      val dir_1 = "Tests/RandomData/getDataFromLast"
      val dir_2 = "Tests/RandomData/getDataFromLast/A"
      DataGenerator.clearDir(dir_2,false)
      DataGenerator.clearDir(dir_1,false)
      val dataSet_1 = DataGenerator.generateData(dir_1,20)
      val dataSet_2 = DataGenerator.generateData(dir_1,20)
      val dataSet_3 = DataGenerator.generateData(dir_1,20)
      val dataSet_4 = DataGenerator.generateData(dir_2,20)
      val last = DataGenerator.getNewest()
      val lastFrom = DataGenerator.getNewestFrom("Tests/RandomData/getDataFromLast")
      assertEquals(last.mkString("->"), dataSet_4.mkString("->"))
      assertEquals(lastFrom.mkString("->"), dataSet_3.mkString("->"))
  }
  
  @Test def getSpecificFileTest{
      val dir = "Tests/RandomData/getSpecificFile"
      DataGenerator.clearDir(dir,false)
      val dataSet_1 = DataGenerator.generateData(dir,20)
      val dataSet_2 = DataGenerator.generateData(dir,20)
      val dataSet_3 = DataGenerator.generateData(dir,20)
      
      val specific = DataGenerator.getData(dir, 1)
      assertEquals(specific.mkString("->"), dataSet_2.mkString("->"))
  }
}