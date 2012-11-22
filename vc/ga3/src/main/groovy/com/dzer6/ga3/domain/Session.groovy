package com.dzer6.ga3.domain

import javax.persistence.*
import org.hibernate.annotations.GenericGenerator
import groovy.transform.*

@EqualsAndHashCode
@ToString
@Entity
@Table(name="chat_session")
class Session {

    @Id
    @GeneratedValue(generator = "system-uuid")
    @GenericGenerator(name = "system-uuid", strategy = "uuid")
    String id
  
    @Version
    long version = 0

    @Temporal(TemporalType.TIMESTAMP)
    Date lastUserDisconnection

}
