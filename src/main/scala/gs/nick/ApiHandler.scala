package gs.nick

import gs.nick.server.definitions.{Game, System}
import gs.nick.server.games.{GamesHandler, GamesResource}
import gs.nick.server.systems.{SystemsHandler, SystemsResource}

import scala.concurrent.Future

object GamesDao {
  var games: IndexedSeq[Game] = IndexedSeq(
    Game(1, 2, "Sonic 2"),
    Game(2, 2, "Vector Man"),
    Game(3, 1, "Kid Chamelion"),
  )

  def gameById(id: Int): Option[Game] = {
    games.find(g => g.id == id)
  }

  def haveGameById(id: Int): Boolean = gameById(id).isDefined

  def addGame(newGame: Game): Unit = {
    games = games :+ newGame
  }
}

object SystemsDao {

  var systems: IndexedSeq[System] = IndexedSeq(
    System(1, "Gensis", "Sega")
  )

  def systemById(id: Int): Option[System] = {
    systems.find(s => s.id == id)
  }

  def haveSystemById(id: Int): Boolean = systemById(id).isDefined

  def addSystem(newSystem: System): Unit = {
    systems = systems :+ newSystem
  }

}

class ApiHandler extends GamesHandler with SystemsHandler {
  override def getGameList(respond: GamesResource.getGameListResponse.type)(): Future[GamesResource.getGameListResponse] = {
    Future.successful {
      respond.OK(GamesDao.games)
    }
  }

  override def addGame(respond: GamesResource.addGameResponse.type)(newGame: Game): Future[GamesResource.addGameResponse] = {
    Future.successful {
      if (SystemsDao.haveSystemById(newGame.systemId)) {
        GamesDao.addGame(newGame)
        respond.OK(newGame)
      } else {
        respond.UnprocessableEntity(s"Bad system id: ${newGame.systemId}")
      }
    }
  }

  override def getGameById(respond: GamesResource.getGameByIdResponse.type)(gameId: Int): Future[GamesResource.getGameByIdResponse] = {
    Future.successful {
      GamesDao
        .gameById(gameId)
        .fold(respond.NotFound) { game =>
          respond.OK(game)
        }
    }
  }

  override def getSystemById(respond: SystemsResource.getSystemByIdResponse.type)(systemId: Int): Future[SystemsResource.getSystemByIdResponse] = {
    Future.successful {
      SystemsDao
        .systemById(systemId)
        .fold(respond.NotFound) { system =>
          respond.OK(system)
        }
    }
  }

  override def getSystemList(respond: SystemsResource.getSystemListResponse.type)(): Future[SystemsResource.getSystemListResponse] = {
    Future.successful {
      respond.OK(SystemsDao.systems)
    }
  }

  override def addSystem(respond: SystemsResource.addSystemResponse.type)(newSystem: System): Future[SystemsResource.addSystemResponse] = {
    Future.successful {
      SystemsDao.addSystem(newSystem)
      respond.OK(newSystem)
    }
  }
}
