import com.dzer6.ga3.*
import com.dzer6.ga3.domain.*
import com.dzer6.ga3.services.*
import com.dzer6.vc.session.storage.SessionNotFoundException

log.info("ec() // find evercookeied user or create new")

def config = applicationContext.config
def sessionStorageService = applicationContext.sessionStorageService
def webToolService = applicationContext.webToolService
def userService = applicationContext.userService
def flashClient = applicationContext.flashClient

def chatWithMe = { String myId, String opponentId ->
    log.info("chatWithMe()")

    log.info("myId = " + myId)
    log.info("opponentId = " + opponentId)

    if (opponentId == null || myId == null) {
        log.warn("changeUser() wrong parameters")
        return
    }

    if (myId == opponentId) {
        log.warn("Opponent and user is the same.")
        return
    }

    User me = userService.getUser(myId)
    User opponent = userService.getUser(opponentId)

    if (me == null) {
        log.warn("There is no user with such id = " + myId)
        return
    }

    if (opponent == null) {
        log.warn("There is no opponent with such id = " + opponentId)
        //TODO show message to user that there is no opponent with such id
        return
    }

    if (!opponent.playing) {
        log.warn("Opponent is not playing at the moment.")
        //TODO show message to user that opponent should push "play" button
        return
    }

    flashClient.stopStream(myId)
    flashClient.stopStream(opponentId)

    userService.releaseUser(me)
    userService.changeUser(me, [playing: false])
    userService.changeUser(opponent, [playing: false])

    userService.setOpponent(myId, opponentId)

    userService.changeUser(me, [playing: true])
    userService.changeUser(opponent, [playing: true])
}

String sessionId = webToolService.getSessionId(request)
log.info("ec() sessionId = $sessionId")
    
def myId
    
try {
    myId = sessionStorageService.get(sessionId, config.SESSION_PARAMETER_USER_ID)
    log.info("ec() myId = $myId")
} catch(SessionNotFoundException e) {
    webToolService.redirectToIndexPage("Session with sessionId = ${sessionId} is not found", forward)
    return
}
    
User me
        
def ecuid = request.ecuid
log.info("ec() ecuid = $ecuid")
        
if (myId == null && ecuid == null) {
    me = userService.createUser()
    myId = me.id
    log.info("ec() after creation myId = " + myId)
    sessionStorageService.put(sessionId, config.SESSION_PARAMETER_USER_ID, me.id)
} else if (myId != ecuid) {
    me = userService.getUser(ecuid)
    log.info("ec() me = $me")
    if (me != null) {
        log.info("We have find evercookeied user and replace it in session.")
        sessionStorageService.put(sessionId, config.SESSION_PARAMETER_USER_ID, ecuid)
        myId = ecuid
    } else {
        me = userService.getUser(myId)
        log.info("ec() me = $me")
        if (me == null) {
            me = userService.createUser()
            myId = me.id
            log.info("ec() after creation myId = " + myId)
            sessionStorageService.put(sessionId, config.SESSION_PARAMETER_USER_ID, me.id)
        } else {
            //TODO what should we do if there is user in session and in DB with different id from evercookie id?
            log.warning("There was user in session with different id from evercookie id!")
        }
    }
} else {
    me = userService.getUser(myId)
    log.info("ec() me = $me")
}
    
if (request.id != null) {
    chatWithMe(myId, request.id)
}

webToolService.renderJson(response, [
        ecuid: myId,
        rtmpServerUrl: me.rtmpServer.url,
        playing: me.playing,
        bannedTill: me.bannedTill == null || me.bannedTill <= new Date() ? 0 : me.bannedTill.time,
        conversationInProcess: webToolService.isConversationInProcess(me),
        chatWithMeURL: webToolService.getChatWithMeURL(request, myId),
        sessionId: sessionId
    ])