import com.dzer6.ga3.*
import com.dzer6.ga3.domain.*
import com.dzer6.ga3.services.*
import com.dzer6.vc.ga.*

log.info("play()")

WebToolService webToolService =  applicationContext.webToolService
UserService userService =  applicationContext.userService
FlashClientInterface flashClient =  applicationContext.flashClient

User me = webToolService.getUser(request)
log.info("play() me = $me")
  
userService.changeUser(me.id, [playing: true])
flashClient.cameraOn(me.id)

webToolService.renderJson(response, [playing: true])