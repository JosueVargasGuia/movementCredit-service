package com.nttdata.movementCreditservice.serviceImpl;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.nttdata.movementCreditservice.FeignClient.CreditFeignClient;
import com.nttdata.movementCreditservice.FeignClient.TableIdFeignClient;
import com.nttdata.movementCreditservice.entity.MovementCredit;
import com.nttdata.movementCreditservice.entity.TypeMovementCredit;
import com.nttdata.movementCreditservice.model.Credit;
import com.nttdata.movementCreditservice.repository.MovementCreditRepository;
import com.nttdata.movementCreditservice.service.MovementCreditService;

import lombok.extern.log4j.Log4j2;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Log4j2
@Service
public class MovementCreditServiceImpl implements MovementCreditService {

	@Autowired
	MovementCreditRepository movementCreditRepository;

	@Autowired
	CreditFeignClient creditFeignClient;

	@Autowired
	TableIdFeignClient tableIdFeignClient;

	@Autowired
	RestTemplate restTemplate;

	@Override
	public Flux<MovementCredit> findAll() {
		return movementCreditRepository.findAll().sort((movementCredit1, movementCredit2) -> movementCredit1
				.getIdMovementCredit().compareTo(movementCredit2.getIdMovementCredit()));
	}

	@Override
	public Mono<MovementCredit> save(MovementCredit movementCredit) {
		Long key = generateKey(MovementCredit.class.getSimpleName());
		if (key >= 1) {
			movementCredit.setIdMovementCredit(key);
			log.info("SAVE[product]:" + movementCredit.toString());
		}
		return movementCreditRepository.insert(movementCredit);
	}

	@Override
	public Mono<MovementCredit> findById(Long idMovementCredit) {
		return movementCreditRepository.findById(idMovementCredit);
	}

	@Override
	public Mono<MovementCredit> update(MovementCredit movementCredit) {
		return movementCreditRepository.save(movementCredit);
	}

	@Override
	public Mono<Void> delete(Long idMovementCredit) {
		return movementCreditRepository.deleteById(idMovementCredit);
	}

	@Override
	public Mono<Map<String, Object>> recordsMovement(MovementCredit movementCredit) {
		Map<String, Object> hashMap = new HashMap<String, Object>();
		Credit credit = this.findByIdCredit(movementCredit.getIdCredit());
		if (credit != null) {
			if (movementCredit.getTypeMovementCredit() == TypeMovementCredit.cargo) {
				return this.findAll().filter(o -> (o.getIdCredit() == movementCredit.getIdCredit()
				// && o.getTypeMovementCredit() == TypeMovementCredit.cargo
				))
						//
						.map(mov -> {
							if (mov.getTypeMovementCredit() == TypeMovementCredit.cargo) {
								mov.setAmount(-1 * mov.getAmount());
							}
							// log.info("Change:"+mov.toString());
							return mov;
						})
						//
						.collect(Collectors.summingDouble(MovementCredit::getAmount)).map(_saldo -> {
							log.info("Saldo [Abono-Cargo]:" + _saldo);
							log.info("Credit:" + credit.toString());
							log.info("Saldo disponible:" + (_saldo + credit.getAmountCreditLimit()));
							if (movementCredit.getAmount() <= (_saldo + credit.getAmountCreditLimit())) {
								log.info("Registra Movimiento:");
								hashMap.put("CreditSucces", "Registro de movimiento de " + TypeMovementCredit.abono);
								hashMap.put("Credit", movementCredit);
								movementCredit.setDateMovement(Calendar.getInstance().getTime());
								this.save(movementCredit).subscribe(e -> log.info("Save:" + e.toString()));

							} else {
								hashMap.put("Credit",
										"El saldo de la cuenta de credito supera el limite de credito.Saldo disponible:"
												+ (_saldo + credit.getAmountCreditLimit()));
								log.info("El cargo a la cuenta de credito supera el limite de credito.");
							}
							return hashMap;
						});
			} else {
				movementCredit.setDateMovement(Calendar.getInstance().getTime());
				return this.save(movementCredit).map(_value -> {
					hashMap.put("Credit", "Registro de movimiento de " + TypeMovementCredit.abono);
					return hashMap;
				});

			}
		} else {
			hashMap.put("credit", "Cuenta de credito no existe.");
			return Mono.just(hashMap);
		}

	}

	@Override
	public Credit findByIdCredit(Long idCredit) {
		/*
		 * log.info(creditService + "/" + idCredit); ResponseEntity<Credit> responseGet
		 * = restTemplate.exchange(creditService + "/" + idCredit, HttpMethod.GET, null,
		 * new ParameterizedTypeReference<Credit>() { }); if
		 * (responseGet.getStatusCode() == HttpStatus.OK) { return
		 * responseGet.getBody(); } else { return null; }
		 */

		Credit credit = creditFeignClient.creditfindById(idCredit);
		log.info("CreditFeignClient: " + credit.toString());
		return credit;
	}

	@Override
	public Mono<Map<String, Object>> balanceInquiry(Credit _credit) {
		Map<String, Object> hashMap = new HashMap<String, Object>();
		Credit credit = this.findByIdCredit(_credit.getIdCredit());
		if (credit != null) {
			return this.findAll().filter(o -> (o.getIdCredit() == credit.getIdCredit())).map(mov -> {
				if (mov.getTypeMovementCredit() == TypeMovementCredit.cargo) {
					mov.setAmount(-1 * mov.getAmount());
				}
				// log.info("Change:"+mov.toString());
				return mov;
			}).collect(Collectors.summingDouble(MovementCredit::getAmount)).map(_value -> {
				log.info("balanceInquiry:" + _value);
				return _value;
			}).map(value -> {
				hashMap.put("Status", "El saldo de la cuenta es de:" + value);
				hashMap.put("creditBalance", value);
				hashMap.put("Credit", credit);
				return hashMap;
			});
		} else {
			hashMap.put("credit", "Cuenta de credito no existe.");
			return Mono.just(hashMap);
		}
	}

	@Override
	public Long generateKey(String nameTable) {
		/*
		 * log.info(tableIdService + "/generateKey/" + nameTable); ResponseEntity<Long>
		 * responseGet = restTemplate.exchange(tableIdService + "/generateKey/" +
		 * nameTable, HttpMethod.GET, null, new ParameterizedTypeReference<Long>() { });
		 * if (responseGet.getStatusCode() == HttpStatus.OK) { log.info("Body:"+
		 * responseGet.getBody()); return responseGet.getBody(); } else { return
		 * Long.valueOf(0); }
		 */

		return tableIdFeignClient.generateKey(nameTable);
	}

	/*
	 * @Override public List<MovementCredit> findAllList() {
	 * ResponseEntity<List<MovementCredit>> responseGet =
	 * restTemplate.exchange("http://localhost:8085/movementCredit", HttpMethod.GET,
	 * null, new ParameterizedTypeReference<List<MovementCredit>>() { }); if
	 * (responseGet.getStatusCode() == HttpStatus.OK) { return
	 * responseGet.getBody(); } else { return null; } }
	 */

}

/*
 * credit = this.findByIdProduc(movementCredit.getIdCredit());
 * Mono.just(this.findAll()
 * .takeUntil(objCredit->objCredit.getIdCredit()==movementCredit.getIdCredit())
 * .collectList().map(e->e))
 * 
 * .subscribe(z->log.info(z.toString()));
 */

// this.findAllList().forEach(e->log.info(e.toString()));

// Mono<List<MovementCredit>>l=
/*
 * this.findAll()
 * //.groupBy(objCredit->objCredit.getIdCredit()==movementCredit.getIdCredit())
 * 
 * .takeUntil(objCredit->objCredit.getIdCredit()==movementCredit.getIdCredit())
 * //.flatMap(idFlux -> idFlux.collectList()) .subscribe(e->{
 * Flux.fromIterable(e.)
 * //.takeUntil(_ob->_ob.getIdCredit()==movementCredit.getIdCredit())
 * .collect(Collectors.summingDouble(MovementCredit::getAmount))
 * .subscribe(eFlux->log.info("Suma:"+eFlux.toString()));
 * 
 * });
 */
// .collect(Collectors.summingDouble(MovementCredit::getAmount))
// .subscribe(e->log.info("Total:"+e));

// l.collect(Collectors.summingDouble(MovementCredit::getAmount)) ;
// .takeUntil(objCredit->objCredit.getIdCredit()==movementCredit.getIdCredit()).fl;
