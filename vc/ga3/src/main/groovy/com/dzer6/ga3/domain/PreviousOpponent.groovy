package com.dzer6.ga3.domain

import javax.persistence.*
import groovy.transform.*

@EqualsAndHashCode
@ToString
@Entity
class PreviousOpponent implements Comparable {
  
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    long id
  
    @Version
    long version = 0
  
    @OneToOne
    User user
  
    @OneToOne
    User opponent
  
    @Temporal(TemporalType.TIMESTAMP)
    Date disconnection
  
    boolean deleted = false
  
    int compareTo(obj) {
        disconnection.compareTo(obj.disconnection)
    }
}
