package com.dzer6.ga3.domain

import javax.persistence.*
import groovy.transform.*

@EqualsAndHashCode
@ToString
@Entity
class UserBan {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    long id
  
    @Version
    long version = 0
  
    @OneToOne
    User user

    @Temporal(TemporalType.TIMESTAMP)
    Date dateCreated = new Date()

}
