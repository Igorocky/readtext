package app.components

import app.Utils.{BTN_LINK, buttonWithIcon}
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{BackendScope, Callback, ScalaComponent}

case class TreeNodeModel(key: String,
                         nodeValue: Option[VdomElement],
                         mayHaveChildren: Boolean,
                         getChildren: Option[List[TreeNodeModel]],
                         loadChildren: Callback,
                         expanded: Option[Boolean] = None,
                         onExpand: Callback = Callback.empty,
                         onCollapse: Callback = Callback.empty
                        )

object Tree {

  protected case class Props(model: TreeNodeModel)

  protected case class State(expanded: Boolean)

  def apply(model: TreeNodeModel): VdomElement =
    comp.withKey(model.key)(Props(model: TreeNodeModel))

  private lazy val comp = ScalaComponent.builder[Props](this.getClass.getName)
    .initialStateFromProps(p => State(expanded = p.model.expanded.getOrElse(false)))
    .renderBackend[Backend]
    .componentDidMount($ => {
      if ($.props.model.expanded.getOrElse(false) && $.props.model.getChildren.isEmpty) {
        $.props.model.loadChildren
      } else {
        Callback.empty
      }
    })
    .build

  protected class Backend($: BackendScope[Props, State]) {
    def render(implicit props: Props, state: State) = <.div(
      ^.`class` := Tree.getClass.getSimpleName,
      props.model.nodeValue.whenDefined(branch =>
        <.div(^.key:= "branch",
          if (props.model.mayHaveChildren) expandCollapseButton else EmptyVdom,
          <.div(^.`class`:="nodeValue", branch)
        )
      ),
      if (props.model.mayHaveChildren && isExpanded) <.div(^.key:= "children", renderChildren) else EmptyVdom
    )

    def isExpanded(implicit props: Props, state: State): Boolean =
      !props.model.nodeValue.isDefined || props.model.expanded.getOrElse(state.expanded)

    private def expandCollapseButton(implicit props: Props, state: State) = buttonWithIcon(
      onClick = if (state.expanded) collapse else expand,
      btnType = BTN_LINK,
      iconType = if (state.expanded) "fa-minus-square" else "fa-plus-square"
    )

    private def collapse(implicit props: Props, state: State) =
      $.modState(_.copy(expanded = false)) >> props.model.onCollapse

    private def expand(implicit props: Props, state: State) =
      $.modState(_.copy(expanded = true)) >> props.model.onExpand >>
        props.model.getChildren.map(_ => Callback.empty).getOrElse(props.model.loadChildren)

    private def renderChildren(implicit props: Props, state: State): VdomNode =
      props.model.getChildren.map(_.toVdomArray(Tree(_))).getOrElse(loadingIcon)

    private def loadingIcon: VdomNode = "Loading..."
  }
}
