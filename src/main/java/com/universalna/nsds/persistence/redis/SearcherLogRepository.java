package com.universalna.nsds.persistence.redis;

import org.springframework.data.repository.CrudRepository;

public interface SearcherLogRepository extends CrudRepository<SearcherLogEntry, String> {

}
