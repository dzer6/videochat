package com.dzer6.ga3.domain

import javax.persistence.*
import groovy.transform.*

@EqualsAndHashCode
@ToString
@Entity
class RtmpServer {
  
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    long id
  
    @Version
    long version = 0

    @Column(nullable = false, unique = true)
    String url
  
    long freeStreamsNumber

}
