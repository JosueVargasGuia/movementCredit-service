package com.nttdata.movementCreditservice.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.nttdata.movementCreditservice.entity.MovementCredit;
import com.nttdata.movementCreditservice.model.CreditAccount;
import com.nttdata.movementCreditservice.service.MovementCreditService;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Log4j2
@RestController
@RequestMapping("/movementCredit")
public class MovementCreditController {
	
	
	@Autowired
	MovementCreditService movementCreditService;


	@GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
	public Flux<MovementCredit> findAll() {
		return movementCreditService.findAll();
	}

	@PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<MovementCredit>> save(@RequestBody MovementCredit movementCredit) {
		return movementCreditService.save(movementCredit)
				.map(_movementCredit -> ResponseEntity.ok().body(_movementCredit)).onErrorResume(e -> {
					log.info("Error:" + e.getMessage());
					return Mono.just(ResponseEntity.badRequest().build());
				});
	}

	@GetMapping(value = "/{idMovementCredit}", produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<MovementCredit>> findById(
			@PathVariable(name = "idMovementCredit") long idMovementCredit) {
		return movementCreditService.findById(idMovementCredit)
				.map(movementCredit -> ResponseEntity.ok().body(movementCredit)).onErrorResume(e -> {
					log.info(e.getMessage());
					return Mono.just(ResponseEntity.badRequest().build());
				}).defaultIfEmpty(ResponseEntity.noContent().build());
	}

	@PutMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<MovementCredit>> update(@RequestBody MovementCredit movementCredit) {

		Mono<MovementCredit> mono = movementCreditService.findById(movementCredit.getIdMovementCredit())
				.flatMap(objMovementCredit -> {
					log.info("Update:[new]" + movementCredit + " [Old]:" + movementCredit);
					return movementCreditService.update(movementCredit);
				});

		return mono.map(_movementCredit -> {
			log.info("Status:" + HttpStatus.OK);
			return ResponseEntity.ok().body(_movementCredit);
		}).onErrorResume(e -> {
			log.info("Status:" + HttpStatus.BAD_REQUEST + " menssage" + e.getMessage());
			return Mono.just(ResponseEntity.badRequest().build());
		}).defaultIfEmpty(ResponseEntity.noContent().build());

	}

	@DeleteMapping(value = "/{idMovementCredit}", produces = MediaType.APPLICATION_JSON_VALUE)
	public Mono<ResponseEntity<Void>> delete(@PathVariable(name = "idMovementCredit") long idMovementCredit) {
		return movementCreditService.findById(idMovementCredit).flatMap(movementCredit -> {
			return movementCreditService.delete(movementCredit.getIdMovementCredit())
					.then(Mono.just(ResponseEntity.ok().build()));
		});
	}

	// registro de movimientos de cuenta
	@PostMapping(value = "/recordsMovement")
	public Mono<ResponseEntity<Map<String, Object>>> recordsMovement(@RequestBody MovementCredit movementCredit) {
		return movementCreditService.recordsMovement(movementCredit).map(_val -> {
			log.info("Resultado:"+_val);
			return ResponseEntity.ok().body(_val);
		}).onErrorResume(e -> {
			log.info("Status:" + HttpStatus.BAD_REQUEST + " message" + e.getMessage());
			Map<String, Object> hashMap = new HashMap<>();
			hashMap.put("Error", e.getMessage());
			return Mono.just(ResponseEntity.badRequest().body(hashMap));
		}).defaultIfEmpty(ResponseEntity.noContent().build());
	}

	@PostMapping(value = "/balanceInquiry")
	public Mono<ResponseEntity<Map<String, Object>>> balanceInquiry(@RequestBody CreditAccount creditAccount) {
		return movementCreditService.balanceInquiry(creditAccount).map(_val -> ResponseEntity.ok().body(_val))
				.onErrorResume(e -> {
					log.info("Status:" + HttpStatus.BAD_REQUEST + " menssage" + e.getMessage());
					Map<String, Object> hashMap = new HashMap<>();
					hashMap.put("Error", e.getMessage());
					return Mono.just(ResponseEntity.badRequest().body(hashMap));
				}).defaultIfEmpty(ResponseEntity.noContent().build());
	}
	
	
	@GetMapping(value="/findAllByCustomer/{idCustomer}", produces = MediaType.APPLICATION_JSON_VALUE)
	public Flux<MovementCredit> findAllByCustomer(@PathVariable("idCustomer") Long idCustomer){
		return movementCreditService.findAllByCustomer(idCustomer);
	}
}
