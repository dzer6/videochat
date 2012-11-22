package com.dzer6.ga3

import com.dzer6.ga3.domain.*
import com.dzer6.ga3.repository.*

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Scope
import org.springframework.context.annotation.ScopedProxyMode
import org.springframework.stereotype.Service

import javax.annotation.PostConstruct

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service("bootstrap")
@Scope("singleton") 
class Bootstrap {
  
    private static final Logger log = LoggerFactory.getLogger(Bootstrap.class)
  
    @Autowired
    @Qualifier("rtmpService")
    def rtmpService
  
    @Autowired
    @Qualifier("config")
    def config
  
    @Autowired
    @Qualifier("rtmpServerTopic")
    def rtmpServerTopic
  
    @Autowired
    private LifePeriodRepository lifePeriodRepository
  
    @Autowired
    private SexTypeRepository sexTypeRepository
  
    @Autowired
    private RtmpServerRepository rtmpServerRepository
  
    @PostConstruct
    public void init() {
        RtmpServer rs = rtmpServerRepository.findByUrl(config.RTMP_SERVER_URL)
        log.info("Existed rtmp server = ${rs}")
        if (rs == null) {
            rtmpService.createRtmpServer(config.RTMP_SERVER_URL, config.RTMP_SERVER_CAPACITY as int)
            log.info("Create rtmp server = ${rtmpServerRepository.findByUrl(config.RTMP_SERVER_URL)}")
        }
    
        if (lifePeriodRepository.findByValue(config.LIFE_PERIOD_UNKNOWN) == null) {
            lifePeriodRepository.save(new LifePeriod([value: config.LIFE_PERIOD_UNKNOWN]))
            lifePeriodRepository.save(new LifePeriod([value: config.LIFE_PERIOD_1]))
            lifePeriodRepository.save(new LifePeriod([value: config.LIFE_PERIOD_2]))
            lifePeriodRepository.save(new LifePeriod([value: config.LIFE_PERIOD_3]))
            lifePeriodRepository.save(new LifePeriod([value: config.LIFE_PERIOD_4]))
            lifePeriodRepository.save(new LifePeriod([value: config.LIFE_PERIOD_5]))
            lifePeriodRepository.save(new LifePeriod([value: config.LIFE_PERIOD_6]))
        }

        if (sexTypeRepository.findByValue(config.SEX_UNKNOWN) == null) {
            sexTypeRepository.save(new SexType([value: config.SEX_UNKNOWN]))
            sexTypeRepository.save(new SexType([value: config.SEX_1]))
            sexTypeRepository.save(new SexType([value: config.SEX_2]))
            sexTypeRepository.save(new SexType([value: config.SEX_3]))
            sexTypeRepository.save(new SexType([value: config.SEX_4]))
            sexTypeRepository.save(new SexType([value: config.SEX_5]))
        }
    }
}