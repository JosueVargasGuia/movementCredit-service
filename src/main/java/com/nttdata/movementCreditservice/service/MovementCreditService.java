package com.nttdata.movementCreditservice.service;

import java.util.Map;

import com.nttdata.movementCreditservice.entity.MovementCredit;
import com.nttdata.movementCreditservice.model.CreditAccount;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface MovementCreditService {
	Flux<MovementCredit> findAll();

	Mono<MovementCredit> findById(Long idMovementCredit);

	Mono<MovementCredit> save(MovementCredit movementCredit);

	Mono<MovementCredit> update(MovementCredit movementCredit);

	Mono<Void> delete(Long idMovementCredit);

	Mono<Map<String, Object>> recordsMovement(MovementCredit movementCredit);

	CreditAccount findByIdCredit(Long idCreditAccount);

	Mono<Map<String, Object>> balanceInquiry(CreditAccount creditAccount);

	Long generateKey(String nameTable);
	// List<MovementCredit> findAllList();

}
