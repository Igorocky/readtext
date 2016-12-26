package shared.forms

case class InputElem(name: String,
                     value: String = "",
                     errors: List[String] = Nil
                    )