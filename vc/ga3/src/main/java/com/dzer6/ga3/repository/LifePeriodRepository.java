package com.dzer6.ga3.repository;

import com.dzer6.ga3.domain.LifePeriod;
import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface LifePeriodRepository extends CrudRepository<LifePeriod, Long> {

    LifePeriod findByValue(String value);
}
