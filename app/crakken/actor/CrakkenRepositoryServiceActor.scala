package crakken.actor

import akka.actor._
import crakken.data.repository._
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global
import akka.event.LoggingReceive
import akka.util.ByteString
import reactivemongo.bson.BSONDocument

object CrakkenRepositoryServiceActor {
  def props(repository: CrakkenRepository): Props = Props(new CrakkenRepositoryServiceActor(repository))
}
class CrakkenRepositoryServiceActor(val repository: CrakkenRepository) extends Actor with UnboundedStash with ActorLogging {

  def receive = LoggingReceive {
    case CrawlRequestMessages.create(request)  => {
      val replyTo = sender
      repository.crawlRequestRepository.create(request) onComplete(tryResponse => replyTo ! CrawlRequestMessages.created(tryResponse))
    }
    case CrawlRequestMessages.getById(id: String)  => {
      val replyTo = sender
      repository.crawlRequestRepository.getById(id) onComplete(tryResponse => replyTo ! CrawlRequestMessages.gotById(tryResponse))
    }
    case CrawlRequestMessages.getAll  => {
      val replyTo = sender
      repository.crawlRequestRepository.getAll() onComplete(tryResponse => replyTo ! CrawlRequestMessages.gotAll(tryResponse))
    }

    case GridFsMessages.create(data: ByteString, filename: String, contentType: String, metadata: BSONDocument)  => {
      val replyTo = sender
      repository.gridFsRepository.create(data, filename, contentType, metadata) onComplete(tryResponse => replyTo ! GridFsMessages.created(tryResponse))
    }
    case GridFsMessages.getById(id: String)  => {
      val replyTo = sender
      repository.gridFsRepository.getById(id) onComplete(tryResponse => replyTo ! GridFsMessages.gotById(tryResponse))
    }

    case PageFetchRequestMessages.create(request) => {
      val replyTo = sender
      repository.pageFetchRequestRepository.create(request) onComplete (tryResponse => replyTo ! PageFetchRequestMessages.created(tryResponse))
    }
    case PageFetchRequestMessages.getById(id: String)  => {
      val replyTo = sender
      repository.pageFetchRequestRepository.getById(id) onComplete(tryResponse => replyTo ! PageFetchRequestMessages.gotById(tryResponse))
    }
    case PageFetchRequestMessages.getByCrId(id: String)  => {
      val replyTo = sender
      repository.pageFetchRequestRepository.getByCrId(id) onComplete(tryResponse => replyTo ! PageFetchRequestMessages.gotByCrId(tryResponse))
    }
    case PageFetchRequestMessages.getAll  => {
      val replyTo = sender
      repository.pageFetchRequestRepository.getAll() onComplete(tryResponse => replyTo ! PageFetchRequestMessages.gotAll(tryResponse))
    }
    case PageFetchRequestMessages.update(request) => {
      val replyTo = sender
      repository.pageFetchRequestRepository.update(request) onComplete(tryResponse => replyTo ! PageFetchRequestMessages.updated(tryResponse))
    }

  }
}
