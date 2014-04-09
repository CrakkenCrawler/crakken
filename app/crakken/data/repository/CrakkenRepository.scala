package crakken.data.repository

trait CrakkenRepository extends CrawlRequestRepositoryComponent
  with PageFetchRequestRepositoryComponent
  with GridFsRepositoryComponent

object MongoCrakkenRepository extends CrakkenRepository {
  val crawlRequestRepository = new MongoCrawlRequestRepository
  val pageFetchRequestRepository = new MongoPageFetchRequestRepository
  val gridFsRepository = new MongoGridFsRepositoryRepository
}

object MockCrakkenRepository extends CrakkenRepository {
  val crawlRequestRepository = new MockCrawlRequestRepository
  val pageFetchRequestRepository = new MockPageFetchRequestRepository
  val gridFsRepository = new MockGridFsRepository
}