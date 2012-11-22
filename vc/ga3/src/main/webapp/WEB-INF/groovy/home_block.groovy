import com.dzer6.ga3.*
import com.dzer6.ga3.domain.*
import com.dzer6.ga3.services.*
import com.dzer6.vc.ga.*

log.info("block()")

PropertyPlaceholderConfigurer config = applicationContext.config
WebToolService webToolService =  applicationContext.webToolService
UserService userService =  applicationContext.userService
BayeuxWrapperService bayeuxWrapperService =  applicationContext.bayeuxWrapperService
FlashClientInterface flashClient =  applicationContext.flashClient

User me = webToolService.getUser(request, forward)

if (me == null) {
    return
}
  
log.info("block() me = $me")
      
bayeuxWrapperService.turnOffBlocking(me)

User opponent = me.opponent
  
log.info("block() opponent = $opponent")

if (opponent != null) {
    flashClient.stopStream(opponent.id)
    flashClient.cameraOff(opponent.id)
    bayeuxWrapperService.turnOffChat(opponent)
    bayeuxWrapperService.turnOffBlocking(opponent)
    userService.releaseUser(me)
    int code = userService.blockUser(opponent)
    Map blockingParams = [:]
    blockingParams.message = config["BLOCK_USER_MESSAGE_${code}"]
    if (code == 0) {
        blockingParams.counting = config.HALF_HOUR_IN_MILLS as long
    } else if (code == 4) {
        blockingParams.counting = config.TEN_MINUTES_IN_MILLS as long
    }
    bayeuxWrapperService.blockUser(opponent, blockingParams)
}

if (me.playing) {
    flashClient.stopStream(me.id)
    bayeuxWrapperService.turnOffChat(me)
}

webToolService.renderJson(response, [success: true, playing: false])