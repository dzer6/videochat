package com.dzer6.ga3.domain

import javax.persistence.*
import org.hibernate.annotations.Type
import groovy.transform.*

@EqualsAndHashCode
@ToString
@Entity
class SessionData {
  
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    long id
  
    @Version
    long version = 0

    @ManyToOne
    Session session
  
    @Column(nullable = false)
    String key
  
    @Column(nullable = false)
    Serializable data
  
}
