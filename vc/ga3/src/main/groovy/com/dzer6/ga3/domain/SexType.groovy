package com.dzer6.ga3.domain

import javax.persistence.*
import groovy.transform.*

@EqualsAndHashCode
@ToString
@Entity
class SexType {
  
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    long id
  
    @Version
    long version = 0

    @Column(unique = true)
    String value

}
