import com.dzer6.ga3.*
import com.dzer6.ga3.domain.*
import com.dzer6.ga3.services.*
import com.dzer6.ga3.repository.*

log.info("cusp() // change user selection parameter")
    
PropertyPlaceholderConfigurer config = applicationContext.config
WebToolService webToolService =  applicationContext.webToolService
UserService userService =  applicationContext.userService
SexTypeRepository sexTypeRepository = applicationContext.sexTypeRepository
LifePeriodRepository lifePeriodRepository = applicationContext.lifePeriodRepository

User me = webToolService.getUser(request, forward)

if (me == null) {
    return
}
    
log.info("cusp() me = $me")
    
String userParameter
LifePeriod lifePeriod
SexType sex
    
if (config.MY_LIFE_PERIOD_PARAM == params.usp) {
    userParameter = UserParametersUtil.convertLifePeriodUIToModel(params.value)
    lifePeriod = lifePeriodRepository.findByValue(userParameter)
    userService.changeUser(me, [myLifePeriod: lifePeriod])
} else if (config.MY_SEX_PARAM == params.usp) {
    userParameter = UserParametersUtil.convertSexUIToModel(params.value)
    sex = sexTypeRepository.findByValue(userParameter)
    userService.changeUser(me, [mySexType: sex])
} else if (config.OPPONENT_LIFE_PERIOD_PARAM == params.usp) {
    userParameter = UserParametersUtil.convertLifePeriodUIToModel(params.value)
    lifePeriod = lifePeriodRepository.findByValue(userParameter)
    userService.changeUser(me, [opponentLifePeriod: lifePeriod])
} else if (config.OPPONENT_SEX_PARAM == params.usp) {
    userParameter = UserParametersUtil.convertSexUIToModel(params.value)
    sex = sexTypeRepository.findByValue(userParameter)
    userService.changeUser(me, [opponentSexType: sex])
}

log.info("cusp() userParameter = $userParameter")
log.info("cusp() lifePeriod = $lifePeriod")
log.info("cusp() sex = $sex")

webToolService.renderJson(response, [success: true])