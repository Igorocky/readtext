package app.components.listtopics

import app.Utils._
import app.WsClient
import app.components.WindowFunc
import app.Reusabilities._
import japgolly.scalajs.react.extra.Reusability
import japgolly.scalajs.react.vdom.html_<^._
import japgolly.scalajs.react.{BackendScope, Callback, ScalaComponent}
import shared.SharedConstants._
import shared.api.{CardsApi, TopicApi}
import shared.dto.{Topic, TopicState}

trait TopicActionsCmpActions {
  def wf: WindowFunc
  def deleteTopic(topicId: Long) = wf.openWaitPane >> topicApi.post(
    _.deleteTopic(topicId),
    th => wf.openOkDialog("Could not delete topic: " + th.getMessage)
  ) {
    case () => wf.closeWaitPane >> topicDeleted(topicId)
  }
  def showTopicActions(topicId: Long, show: Boolean): Callback
  def moveUpTopicAction(topicId: Long): Callback
  def moveDownTopicAction(topicId: Long): Callback
  def topicDeleted(topicId: Long): Callback
  def cardsApi: WsClient[CardsApi]
  def topicApi: WsClient[TopicApi]
}

object TopicActionsCmp {

  implicit val propsReuse = Reusability.caseClassExcept[Props]('ctx)
  case class Props(ctx: WindowFunc with TopicActionsCmpActions,
                   topic: Topic,
                   actionsHidden: Boolean,
                   readOnly: Boolean,
                   onEdit: Callback) {
    @inline def render = comp(this)
  }

  implicit val stateReuse = Reusability.byRef[State]
  protected case class State(currTopicState: Option[Option[TopicState]] = None,
                             history: Option[List[List[String]]] = None)

  private lazy val comp = ScalaComponent.builder[Props](this.getClass.getName)
    .initialState(State())
    .renderBackend[Backend]
      .componentWillReceiveProps{$=>
        if ($.nextProps.actionsHidden && ($.state.currTopicState.isDefined || $.state.history.isDefined)) {
          $.modState(_.copy(
            currTopicState = None, history = None
          ))
        } else {
          Callback.empty
        }
      }
    .configure(Reusability.shouldComponentUpdate)
    .build

  protected class Backend($: BackendScope[Props, State]) {
    def render(implicit p: Props, s: State) = if (p.actionsHidden) {
      <.span(showAllActionsButton)
    } else {
      <.div(^.`class`:=TOPIC_ACTION_BUTTONS,
        <.div(
          hideAllActionsButton,
          if (!p.readOnly) TagMod(
            editTopicButton,
            moveUpButton,
            moveDownButton,
            deleteTopicButton
          ) else EmptyVdom
        ),
        <.div(^.`class`:=TOPIC_CURR_STATE,
          currTopicSate
        )
      )
    }



    def showAllActionsButton(implicit p: Props) = buttonWithIcon(
      onClick = p.ctx.showTopicActions(p.topic.id.get, true) >>
        p.ctx.cardsApi.post(_.loadCardState(p.topic.id.get), p.ctx.showError) (
          st => $.modState(_.copy(currTopicState = Some(st)))
        ),
      btnType = BTN_INFO,
      iconType = "fa-ellipsis-h"
    )

    def hideAllActionsButton(implicit p: Props) = buttonWithIcon(
      onClick = p.ctx.showTopicActions(p.topic.id.get, false) >> $.modState(_.copy(currTopicState = None, history = None)),
      btnType = BTN_INFO,
      iconType = "fa-arrow-left"
    )

    def editTopicButton(implicit p: Props) = buttonWithIcon(
      onClick = p.onEdit,
      btnType = BTN_INFO,
      iconType = "fa-pencil-square-o"
    )

    def moveUpButton(implicit p: Props) = buttonWithIcon(
      onClick = p.ctx.moveUpTopicAction(p.topic.id.get),
      btnType = BTN_INFO,
      iconType = "fa-long-arrow-up"
    )

    def moveDownButton(implicit p: Props) = buttonWithIcon(
      onClick = p.ctx.moveDownTopicAction(p.topic.id.get),
      btnType = BTN_INFO,
      iconType = "fa-long-arrow-down"
    )

    def deleteTopicButton(implicit p: Props) = buttonWithIcon(
      onClick = p.ctx.openOkCancelDialog(
        text = s"Delete topic '${p.topic.title}'?",
        onOk = p.ctx.deleteTopic(p.topic.id.get)
      ),
      btnType = BTN_DANGER,
      iconType = "fa-trash-o"
    )

    def showHistoryButton(implicit p: Props, s: State) = buttonWithText(
      onClick = p.ctx.cardsApi.post(_.loadCardHistory(p.topic.id.get), p.ctx.showError)(
        hist => $.modState(_.copy(history = Some(hist)))
      ),
      btnType = BTN_INFO,
      text = "Show history"
    )

    def hideHistoryButton(implicit p: Props, s: State) = buttonWithText(
      onClick = $.modState(_.copy(history = None)),
      btnType = BTN_INFO,
      text = "Hide history"
    )

    def currTopicSate(implicit p: Props, s: State) =
      s.currTopicState match {
        case None => <.div("Loading topic state...")
        case Some(None) => <.div("This topic doesn't have state yet")
        case Some(Some(state)) => <.div(
          <.div(
            s"Last changed: ${state.timeOfChange} (", <.b(state.lastChangedDuration), " ago)",
            s" last estimated: ", <.b(state.score),
            s" current overtime: ", <.b(calcOvertime(state)),
            s" last comment: ", <.b(state.comment), " ",
            if (s.history.isEmpty) showHistoryButton else hideHistoryButton
          ),
          drawHistory
        )
      }

    def calcOvertime(state: TopicState) =
      state.timeLeftUntilActivation.map('-' + _).getOrElse(state.timePassedAfterActivation.map('+' + _).get)

    def drawHistory(implicit p: Props, s: State) =
      s.history match {
        case None => EmptyVdom
        case Some(header::tail) => <.table(^.`class`:=TOPIC_HISTORY_TABLE,
          <.thead(
            <.tr(header.toVdomArray(h => <.th(^.key := h, h)))
          ),
          <.tbody(
            tail.zipWithIndex.toVdomArray {
              case (r, idx) => <.tr(
                ^.key := idx,
                r.zipWithIndex.toVdomArray { case (d, idxD) => <.td(^.key := idxD, d) }
              )
            }
          )
        )
      }

  }
}