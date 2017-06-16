package controllers

import scala.concurrent.{ExecutionContext, Future}


trait RequestHandler {
  def matchPath(path: String): Boolean
  def handle(input: String): Future[String]

}

trait Router {self =>
  type Path = String

  protected def findHandler(path: Path): Option[RequestHandler]

  def handle(path: Path, data: String): Option[Future[String]] = findHandler(path).map(_.handle(data))

  def append(other: Router): Router = new Router {
    override def findHandler(path: Path): Option[RequestHandler] =
      self.findHandler(path).orElse(other.findHandler(path))
  }

  def +(other: Router) = append(other)

}

case class RouterBuilder(handlers: List[RequestHandler] = Nil) extends Router {
  override protected def findHandler(path: Path): Option[RequestHandler] = handlers.find(_.matchPath(path))

  private def addHandler(requestHandler: RequestHandler) = copy(handlers = requestHandler::handlers)

  def addHandler[I,O](path: String, reader: String => I, writer: O => String)
                     (f: I => Future[O])
                     (implicit ec: ExecutionContext): RouterBuilder = {
    addHandler(
      new RequestHandler {
        override def matchPath(p: String): Boolean = p == path
        override def handle(input: String): Future[String] = f(reader(input)).map(writer)
      }
    )
  }

  def addHandler[I,O](signature: (String, String => I, O => String))
                     (f: I => Future[O])
                     (implicit ec: ExecutionContext): RouterBuilder = {
    addHandler(signature._1, signature._2, signature._3)(f)
  }
}