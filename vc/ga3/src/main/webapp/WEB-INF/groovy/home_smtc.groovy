import com.dzer6.ga3.*
import com.dzer6.ga3.domain.*
import com.dzer6.ga3.services.*

log.info("smtc() // send message to chat")

PropertyPlaceholderConfigurer config = applicationContext.config
WebToolService webToolService =  applicationContext.webToolService
BayeuxWrapperService bayeuxWrapperService =  applicationContext.bayeuxWrapperService

String message = params.message
    
log.info("smtc() message = $message")

if (message == null || message == "") {
    log.warn("Empty or null chat message.")
    webToolService.renderJson(response, [error: true, message: "Empty or null chat message."])
    return
}

User me = webToolService.getUser(request, forward)

if (me == null) {
    return
}
  
log.info("smtc() me = $me")

if (me.opponent == null || !me.playing) {
    log.warn("smtc() There is no opponent.")
    bayeuxWrapperService.turnOffChat(me)
    bayeuxWrapperService.turnOffBlocking(me)
    webToolService.renderJson(response, [error: true, message: "There is no opponent."])
    return
} else {
    bayeuxWrapperService.sendMessageToChat(me, message)
}

webToolService.renderJson(response, [success: true])