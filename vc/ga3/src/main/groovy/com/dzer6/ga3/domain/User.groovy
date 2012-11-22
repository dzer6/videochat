package com.dzer6.ga3.domain

import javax.persistence.*
import org.hibernate.annotations.GenericGenerator
import groovy.transform.*

@EqualsAndHashCode(excludes="opponent")
@ToString(includeNames=true, excludes="opponent")
@Entity
@Table(name="chat_user")
class User {

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    String id
  
    @Version
    long version = 0
  
    @OneToOne
    LifePeriod myLifePeriod
  
    @OneToOne
    SexType mySexType
  
    @OneToOne
    LifePeriod opponentLifePeriod
  
    @OneToOne
    SexType opponentSexType
  
    @ManyToOne
    RtmpServer rtmpServer
  
    @OneToOne
    User opponent
  
    boolean playing
  
    boolean broadcasting
  
    @Temporal(TemporalType.TIMESTAMP)
    Date bannedTill

}
