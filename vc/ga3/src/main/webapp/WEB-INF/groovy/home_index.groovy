import com.dzer6.ga3.*
import com.dzer6.ga3.domain.*
import com.dzer6.ga3.services.*
import com.dzer6.vc.ga.*
import com.dzer6.vc.session.storage.*

log.info("index() ip = ${request.remoteAddr}")
log.info("index() id = ${request.id}")

PropertyPlaceholderConfigurer config = applicationContext.config
SessionStorage sessionStorageService = applicationContext.sessionStorageService
WebToolService webToolService = applicationContext.webToolService
RtmpService rtmpService = applicationContext.rtmpService

if (rtmpService.isThereRemainsFreeRtmpServers()) {
    String sessionId = webToolService.getSessionId(request)
  
    log.info("index() sessionId = $sessionId")  
  
    if (sessionStorageService.sessionExists(sessionId)) {
        User me = webToolService.getUser(request, forward)
        if (me == null) {
            return
        }
    } else {
        sessionId = sessionStorageService.createSession()
        log.info("index() create new session, sessionId = $sessionId")
        webToolService.saveSessionIdToCookie(sessionId, response)
    } 
    	
    request.sessionId = sessionId
    request.laszloSwfTarget = config.LASZLO_SWF_TARGET
    request.quality = config.PICTURE_QUALITY
    request.bandwidth = config.STREAM_BANDWIDTH

    forward "/WEB-INF/pages/index.gtpl"
} else {
    request.message = config.THERE_IS_NO_FREE_RTMP_SERVERS
    forward "/WEB-INF/pages/outOfService.gtpl"
}