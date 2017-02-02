package shared.forms

import upickle.default._

sealed trait PostData
case class FormSubmit(formData: FormData) extends PostData
case class FormWithErrors(formData: FormData) extends PostData
case class DataResponse(content: String) extends PostData
case class ErrorResponse(content: String) extends PostData

object PostData {
  def readPostData(str: String): PostData = read[PostData](str)
  def formSubmit(formData: FormData): String = write(FormSubmit(formData))
  def dataResponse(data: String): String = write(DataResponse(data))
  def formWithErrors(formData: FormData): String = write(FormWithErrors(formData))
  def errorResponse(content: String): String = write(ErrorResponse(content))
}