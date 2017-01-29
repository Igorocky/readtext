package shared.forms

import shared.forms.PostDataTypes.{DATA_RESPONSE, ERROR_RESPONSE, FORM_SUBMIT, FORM_WITH_ERRORS}
import upickle.default._

sealed trait PostDataType

object PostDataTypes {
  //client sends these types:
  /* content should contain FormData */
  case object FORM_SUBMIT extends PostDataType

  //server sends these responses:
  /* content should contain any data */
  case object DATA_RESPONSE extends PostDataType
  /* FormData if it was submitted with errors */
  case object FORM_WITH_ERRORS extends PostDataType

  /* content should contain description of error */
  case object ERROR_RESPONSE extends PostDataType
}

case class PostData(typ: PostDataType, content: String)

object PostData {
  def readPostData(str: String): PostData = read[PostData](str)
  def forSubmit(formData: FormData): String = write(PostData(FORM_SUBMIT, write(formData)))
  def dataResponse(data: String): String = write(PostData(DATA_RESPONSE, data))
  def formWithErrors(formData: FormData): String = write(PostData(FORM_WITH_ERRORS, write(formData)))
  def errorResponse(descr: String): String = write(PostData(ERROR_RESPONSE, descr))
}