package com.dzer6.ga3.repository;

import com.dzer6.ga3.domain.SexType;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface SexTypeRepository extends CrudRepository<SexType, Long> {

    SexType findByValue(String value);
    
}
