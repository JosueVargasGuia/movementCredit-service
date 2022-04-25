package com.nttdata.movementCreditservice.repository;

import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

import com.nttdata.movementCreditservice.entity.MovementCredit;

@Repository
public interface MovementCreditRepository extends ReactiveMongoRepository<MovementCredit,Long>{

}
