import com.dzer6.ga3.*
import com.dzer6.ga3.domain.*
import com.dzer6.ga3.services.*
import com.dzer6.vc.session.storage.*

PropertyPlaceholderConfigurer config = applicationContext.config
SessionStorage sessionStorageService =  applicationContext.sessionStorageService
WebToolService webToolService =  applicationContext.webToolService
UserService userService =  applicationContext.userService

String sessionId = webToolService.getSessionId(request)

log.info("sessionId = $sessionId")

String myId

try {
    myId = sessionStorageService.get(sessionId, config.SESSION_PARAMETER_USER_ID)
} catch(SessionNotFoundException e) {
    webToolService.redirectToIndexPage("Session with sessionId = ${sessionId} is not found", forward)
    return null
}

log.info("bannedtill() myId = $myId")

User me = userService.getUser(myId)

log.info("bannedtill() me = $me")

if (me == null) {
    webToolService.redirectToIndexPage("There is no user for current session, sessionId = ${sessionId}, userId = ${myId}", forward)
    return null
}

webToolService.renderJson(response, [bannedTillDelta: me.bannedTill.time - new Date().time])