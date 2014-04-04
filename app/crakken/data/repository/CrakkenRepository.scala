package crakken.data.repository

trait CrakkenRepository extends CrawlRequestRepositoryComponent
  with PageFetchRequestRepositoryComponent

object MongoCrakkenRepository extends CrakkenRepository {
  val crawlRequestRepository = new MongoCrawlRequestRepository
  val pageFetchRequestRepository = new MongoPageFetchRequestRepository
}

object MockCrakkenRepository extends CrakkenRepository {
  val crawlRequestRepository = new MockCrawlRequestRepository
  val pageFetchRequestRepository = new MockPageFetchRequestRepository
}