package crakken.actor

import akka.actor._
import crakken.data.repository._
import scala.concurrent.ExecutionContext
import ExecutionContext.Implicits.global
import akka.event.LoggingReceive

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

    case PageFetchRequestMessages.create(request) => {
      val replyTo = sender
      repository.pageFetchRequestRepository.create(request) onComplete (tryResponse => replyTo ! PageFetchRequestMessages.created(tryResponse))
    }
    case PageFetchRequestMessages.getById(id: String)  => {
      val replyTo = sender
      repository.pageFetchRequestRepository.getById(id) onComplete(tryResponse => replyTo ! PageFetchRequestMessages.gotById(tryResponse))
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
