package app.components

import app.Utils.{BTN_LINK, buttonWithIcon}
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{BackendScope, Callback, ScalaComponent}
import shared.SharedConstants._

object Tree {

  case class Props(key: String,
                             nodeValue: Option[VdomElement],
                             mayHaveChildren: Boolean,
                             children: Option[List[Props]],
                             loadChildren: Callback,
                             expanded: Option[Boolean] = None,
                             onExpand: Callback = Callback.empty,
                             onCollapse: Callback = Callback.empty) {
    @inline def render = comp.withKey(key)(this)
  }

  protected case class State(expanded: Boolean)

  private lazy val comp = ScalaComponent.builder[Props](this.getClass.getName)
    .initialStateFromProps(p => State(expanded = p.expanded.getOrElse(false)))
    .renderBackend[Backend]
    .componentDidMount($ =>
      $.props.loadChildren.when_($.props.expanded.getOrElse(false) && $.props.children.isEmpty)
    ).build

  protected class Backend($: BackendScope[Props, State]) {
    def render(implicit props: Props, state: State) = <.div(
      ^.`class` := Tree.getClass.getSimpleName,
      props.nodeValue.whenDefined(nodeValue =>
        <.div(^.`class` := TREE_NODE_VALUE_WRAPPER,
          if (props.mayHaveChildren) expandCollapseButton else EmptyVdom,
          <.div(^.`class`:=TREE_NODE_VALUE, nodeValue)
        )
      ),
      if (props.mayHaveChildren && isExpanded) {
        <.div(^.`class`:=TREE_NODE_CHILDREN, renderChildren)
      } else EmptyVdom
    )

    def isExpanded(implicit props: Props, state: State): Boolean =
      props.nodeValue.isEmpty || props.expanded.getOrElse(state.expanded)

    private def expandCollapseButton(implicit props: Props, state: State) = buttonWithIcon(
      onClick = if (isExpanded) collapse else expand,
      btnType = BTN_LINK,
      iconType = if (isExpanded) "fa-minus-square" else "fa-plus-square"
    )

    private def collapse(implicit props: Props, state: State) =
      $.modState(_.copy(expanded = false)) >> props.onCollapse

    private def expand(implicit props: Props, state: State) =
      $.modState(_.copy(expanded = true)) >> props.onExpand >> props.loadChildren.when_(props.children.isEmpty)

    private def renderChildren(implicit props: Props, state: State): VdomNode =
      props.children.map(_.toVdomArray(_.render)).getOrElse(loadingIcon)

    private def loadingIcon: VdomNode = "Loading..."
  }
}
