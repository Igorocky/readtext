package controllers

import shared.forms.FormMethods
import shared.forms.Forms.SubmitResponse
import utils.Session

import scala.concurrent.{ExecutionContext, Future}


trait RequestHandler {
  def matchPath(path: String): Boolean

  def handle(session: Session, input: String): Future[(Session, String)]

}

trait Router {
  self =>
  type Path = String

  protected def findHandler(path: Path): Option[RequestHandler]

  def handle(path: Path, session: Session, data: String): Option[Future[(Session, String)]] =
    findHandler(path).map(_.handle(session, data))

  def append(other: Router): Router = new Router {
    override def findHandler(path: Path): Option[RequestHandler] =
      self.findHandler(path).orElse(other.findHandler(path))
  }

  def +(other: Router) = append(other)

}

case class RouterBuilder(handlers: List[RequestHandler] = Nil) extends Router {
  override protected def findHandler(path: Path): Option[RequestHandler] = handlers.find(_.matchPath(path))

  private def addHandler(requestHandler: RequestHandler) = copy(handlers = requestHandler :: handlers)

  def addHandler[I, O](path: String, reader: String => I, writer: O => String)
                      (f: (Session, I) => Future[(Session, O)])
                      (implicit ec: ExecutionContext): RouterBuilder = {
    addHandler(
      new RequestHandler {
        override def matchPath(p: String): Boolean = p == path

        override def handle(session: Session, input: String): Future[(Session, String)] = f(session, reader(input)).map{
          case (ses, out) => (ses, writer(out))
        }
      }
    )
  }

  def addHandler[I, O](signature: (String, String => I, O => String))
                      (f: I => Future[O])
                      (implicit ec: ExecutionContext): RouterBuilder = {
    addHandler(signature._1, signature._2, signature._3)((s, i) => f(i).map(o => (s,o)))
  }

  def addHandlerWithSession[I, O](signature: (String, String => I, O => String))
                                 (f: (Session, I) => Future[(Session, O)])
                                 (implicit ec: ExecutionContext): RouterBuilder = {
    addHandler(signature._1, signature._2, signature._3)(f)
  }

  def addHandlerOfFormSubmit[I, S](signature: (String, String => I, SubmitResponse[I,S] => String))
                                  (formMethods: FormMethods[I])
                                  (onFormIsValid: I => Future[S])
                                  (implicit ec: ExecutionContext): RouterBuilder = {
    addHandlerWithSession(signature._1, signature._2, signature._3){
      case (session, data) =>
        val fd = formMethods.validate(session.language, data)
        if (fd.hasErrors) {
          Future.successful((session, Left(fd)))
        } else {
          onFormIsValid(data).map{
            case o => (session, Right(o))
          }
        }
    }

  }
}