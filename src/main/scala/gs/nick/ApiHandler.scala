package gs.nick

import gs.nick.server.birds.{BirdsHandler, BirdsResource}
import gs.nick.server.definitions.{Bird, Sighting}
import gs.nick.server.sightings.{SightingsHandler, SightingsResource}

import scala.concurrent.Future

object BirdsDao {
  var all: IndexedSeq[Bird] = IndexedSeq.empty

  def reset(): Unit = {
    all = IndexedSeq.empty
  }
}


class BirdsController extends BirdsHandler {
  override def getBirdList(respond: BirdsResource.getBirdListResponse.type)(): Future[BirdsResource.getBirdListResponse] = {
    Future.successful {
      respond.OK(BirdsDao.all)
    }
  }

  override def addBird(respond: BirdsResource.addBirdResponse.type)(newBird: Bird): Future[BirdsResource.addBirdResponse] = {
    Future.successful {
      BirdsDao.all = BirdsDao.all :+ newBird
      respond.OK(newBird)
    }
  }

  override def getBirdById(respond: BirdsResource.getBirdByIdResponse.type)(birdId: Int): Future[BirdsResource.getBirdByIdResponse] = {
    Future.successful {
      val item = BirdsDao.all.find(_.id == birdId)
      if (item.isDefined) {
        respond.OK(item.get)
      } else {
        respond.NotFound
      }
    }
  }
}

object SightingsDao {
  var all: IndexedSeq[Sighting] = IndexedSeq.empty

  def reset(): Unit = {
    all = IndexedSeq.empty
  }
}

class SightingsController extends SightingsHandler {
  override def getSightingById(respond: SightingsResource.getSightingByIdResponse.type)(sightingsId: Int): Future[SightingsResource.getSightingByIdResponse] = {
    Future.successful {
      val item = SightingsDao.all.find(_.id == sightingsId)
      if (item.isDefined) {
        respond.OK(item.get)
      } else {
        respond.NotFound
      }
    }
  }

  override def getSightingsList(respond: SightingsResource.getSightingsListResponse.type)(): Future[SightingsResource.getSightingsListResponse] = {
    Future.successful {
      respond.OK(SightingsDao.all)
    }
  }

  override def addSighting(respond: SightingsResource.addSightingResponse.type)(newSighting: Sighting): Future[SightingsResource.addSightingResponse] = {
    Future.successful {
      SightingsDao.all = SightingsDao.all :+ newSighting
      respond.OK(newSighting)
    }
  }
}
