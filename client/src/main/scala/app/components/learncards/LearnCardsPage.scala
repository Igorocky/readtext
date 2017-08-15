package app.components.learncards

import app.Utils
import app.components._
import app.components.listtopics.TopicCmp
import japgolly.scalajs.react._
import japgolly.scalajs.react.component.Scala.Unmounted
import japgolly.scalajs.react.vdom.html_<^._
import shared.dto.Topic
import shared.pageparams.LearnCardsPageParams
import upickle.default._


// TODO: add possibility to change history
object LearnCardsPage {
  protected type Props = LearnCardsPageParams

  protected type State = LearnCardsPageState

  def apply(str: String): Unmounted[Props, State, Backend] = comp(read[Props](str))

  private lazy val comp = ScalaComponent.builder[Props](this.getClass.getName)
    .initialStateFromProps(p => LearnCardsPageState(pageParams = p))
    .renderBackend[Backend]
    .componentWillMount { $ =>
      $.modState(
        _.copy(
          modState = $.modState(_),
          getState = () => $.state,
          wsClient = Utils.createWsClient($.props.wsEntryUrl),
          sessionWsClient = Utils.createWsClient($.props.wsEntryUrl)
        ),
        CallbackTo($.state) >>= (_.loadActiveTopics(None))
      )
    }
    .build

  protected class Backend($: BackendScope[Props, State]) {

    def render(implicit s: State) = UnivPage.Props(
      language = s.pageParams.headerParams.language,
      sessionWsClient = s.sessionWsClient,
      onLanguageChange = newLang => $.modState(_.setLanguage(newLang)),
      windowFuncMem = s.windowFuncMem,
      windowFunc = s,
      content = <.div(
        <.h2(s.pageParams.paragraphTitle),
        s.learnCardsPageMem.activationTimeReduction.whenDefined("Activation time reduction: " + _),
        <.div(
          TextField.Props(
            onEnter = str => s.changeActivationTimeReduction(str).map(_ => ""),
            onEscape = _ => CallbackTo(""),
            placeholder = "Activation time reduction",
            width = 300
          ).render
        ),
        if (s.learnCardsPageMem.activeTopics.children.isEmpty) {
          "Loading..."
        } else if (s.learnCardsPageMem.activeTopics.children.get.isEmpty) {
          "No active topics."
        } else {
          s.learnCardsPageMem.activeTopics.children.get.toVdomArray{topicNode=>
            val t = topicNode.value.get.asInstanceOf[Topic]
            TopicCmp.Props(
              ctx = s,
              topic = t,
              selected = false,
              showImg = topicNode.attrs.showImg,
              actionsHidden = topicNode.attrs.actionsHidden,
              selectTopicAction = (_, _) => Callback.empty,
              selectMode = false,
              showTopicImgBtnClicked = s.showTopicImgBtnClicked(t.id.get),
              getTopicImgUrl = s.pageParams.getTopicImgUrl,
              wsClient = null/*new WsClient[TopicApi] {
                override def doCall[O](path: String, dataStr: String, reader: (String) => O, errHnd: (Throwable) => Callback): (O => Callback) => Callback = {
                  println(s"PATH = '$path'")
                  oc => Callback.empty
                }
              }*/,
              topicUpdated = _ => Callback.empty,
              showTopicActions = s.showTopicActions,
              cardsClient = s.wsClient,
              moveUpTopicAction = _ => Callback.empty,
              moveDownTopicAction = _ => Callback.empty,
              topicDeleted = _ => Callback.empty,
              language = s.pageParams.headerParams.language,
              uploadTopicFileUrl = null,
              unregisterPasteListener = _ => Callback.empty,
              registerPasteListener = (_,_) => Callback.empty,
              topicStateUpdated = _ => s.loadActiveTopics(s.learnCardsPageMem.activationTimeReduction),
              readOnly = true
            ).render
          }
        }
      )
    ).render

  }
}
