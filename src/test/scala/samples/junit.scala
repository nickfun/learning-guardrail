package samples


import scala.concurrent.duration.Duration

import org.junit._
import Assert._
import gs.nick.{ApiHandler, GamesDao}
import gs.nick.server.definitions.Game

import scala.concurrent.Await

@Test
class AppTest {

  val handler = new ApiHandler()
  val GR = gs.nick.server.games.GamesResource

  @Test
  def testAddGame() = {
    val g = Game(100, 1, "best game yet")
    assertTrue(!GamesDao.haveGameById(g.id))
    handler.addGame(GR.addGameResponse)(g)
    assertTrue(GamesDao.haveGameById(g.id))
    val r = handler.getGameById(GR.getGameByIdResponse)(g.id)
    val resp = Await.result(r, Duration.Inf)
    assertTrue(resp.statusCode.intValue() == 200)
  }

}


