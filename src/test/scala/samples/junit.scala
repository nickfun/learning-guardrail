package samples


import scala.concurrent.duration.Duration
import org.junit._
import Assert._
import gs.nick.server.birds.BirdsResource
import gs.nick.server.definitions.{Bird, Sighting}
import gs.nick.server.sightings.SightingsResource
import gs.nick.{BirdsController, BirdsDao, SightingsController, SightingsDao}

import scala.concurrent.Await

@Test
class AppTest {


  @Before
  def setup(): Unit = {
    BirdsDao.reset()
    SightingsDao.reset()
  }

  @Test
  def testAddBirds(): Unit = {
    val controller = new BirdsController
    assertTrue(BirdsDao.all.isEmpty)
    controller.addBird(BirdsResource.addBirdResponse)(Bird(1, "blue jay", "sings a lot"))
    controller.addBird(BirdsResource.addBirdResponse)(Bird(2, "crow", "ugly"))
    assertTrue(BirdsDao.all.length == 2)
  }

  @Test
  def testAddSighting(): Unit = {
    val controller = new SightingsController
    assertTrue(SightingsDao.all.isEmpty)
    controller.addSighting(SightingsResource.addSightingResponse)(Sighting(1, 1, "2017-09-14", "saw a blue jay this morning"))
    controller.addSighting(SightingsResource.addSightingResponse)(Sighting(2, 1, "2018-01-03", "At the park in Hayward"))
  }

}


