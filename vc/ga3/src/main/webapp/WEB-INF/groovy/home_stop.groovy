import com.dzer6.ga3.*
import com.dzer6.ga3.domain.*
import com.dzer6.ga3.services.*
import com.dzer6.vc.ga.*

log.info("stop()")

PropertyPlaceholderConfigurer config = applicationContext.config
WebToolService webToolService =  applicationContext.webToolService
UserService userService =  applicationContext.userService
BayeuxWrapperService bayeuxWrapperService =  applicationContext.bayeuxWrapperService
FlashClientInterface flashClient =  applicationContext.flashClient

User me = webToolService.getUser(request, forward)

if (me == null) {
    return
}
  
log.info("stop() me = $me")
      
bayeuxWrapperService.turnOffBlocking(me)

User opponent = me.opponent

log.info("stop() opponent = $opponent")
  
if (opponent != null) {
    flashClient.stopStream(opponent.id)
    bayeuxWrapperService.turnOffChat(opponent)
    bayeuxWrapperService.turnOffBlocking(opponent)
    userService.releaseUser(me.id)
}
  
log.info("stop() me.playing = ${me.playing}")

if (me.playing) {
    flashClient.stopStream(me.id)
    bayeuxWrapperService.turnOffChat(me)
}

userService.changeUser(me.id, [playing: false])
  
log.info("stop() me.playing = ${me.playing}")
      
flashClient.cameraOff(me.id)

webToolService.renderJson(response, [playing: false])