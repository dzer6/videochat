import com.dzer6.ga3.*
import com.dzer6.ga3.domain.*
import com.dzer6.ga3.services.*
import com.dzer6.vc.ga.*

log.info("next()")
    
PropertyPlaceholderConfigurer config = applicationContext.config
WebToolService webToolService =  applicationContext.webToolService
UserService userService =  applicationContext.userService
BayeuxWrapperService bayeuxWrapperService =  applicationContext.bayeuxWrapperService
FlashClientInterface flashClient =  applicationContext.flashClient
  
User me = webToolService.getUser(request)

log.info("next() me = $me")
      
bayeuxWrapperService.turnOffChat(me)
bayeuxWrapperService.turnOffBlocking(me)

log.info("next() me.playing = ${me.playing}")
// if user already exists but not playing then turn on camera
if (!me.playing) {
    userService.changeUser(me.id, [playing: true])
    flashClient.cameraOn(me.id)
}
  
log.info("next() me.playing = ${me.playing}")
      
User opponent = me.opponent
  
log.info("next() opponent = $opponent")

if (opponent != null) {
    flashClient.stopStream(opponent.id)
    bayeuxWrapperService.turnOffChat(opponent)
    bayeuxWrapperService.turnOffBlocking(opponent)
}
      
userService.releaseUser(me.id)
// choose new opponent
userService.chooseOpponent(me.id)
      
opponent = me.opponent
  
log.info("next() opponent = $opponent")

flashClient.stopStream(me.id)

Map result = [playing: me.playing]

if (opponent != null) {
    flashClient.stopStream(opponent.id)
    flashClient.playStream(me.id, me.rtmpServer.url + config.R5WA_APPLICATION_NAME_POSTFIX, opponent.id)
    flashClient.playStream(opponent.id, opponent.rtmpServer.url + config.R5WA_APPLICATION_NAME_POSTFIX, me.id)

    bayeuxWrapperService.clearChat(me)
    bayeuxWrapperService.clearChat(opponent)

    bayeuxWrapperService.turnOnChat(me)
    bayeuxWrapperService.turnOnBlocking(me)
    bayeuxWrapperService.turnOnChat(opponent)
    bayeuxWrapperService.turnOnBlocking(opponent)

    result.success = true
} else {
    result.error = true
    result.message = "There is no free users at the moment. Please, try later."
}

webToolService.renderJson(response, result)
