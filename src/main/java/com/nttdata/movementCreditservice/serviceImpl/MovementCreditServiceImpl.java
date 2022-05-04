package com.nttdata.movementCreditservice.serviceImpl;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

 
import com.nttdata.movementCreditservice.FeignClient.ConfigurationFeingClient;
import com.nttdata.movementCreditservice.FeignClient.CreditFeignClient;
import com.nttdata.movementCreditservice.FeignClient.ProductFeignClient;
import com.nttdata.movementCreditservice.FeignClient.TableIdFeignClient;
import com.nttdata.movementCreditservice.entity.MovementCredit;
import com.nttdata.movementCreditservice.entity.TypeMovementCredit;
import com.nttdata.movementCreditservice.model.Configuration;
import com.nttdata.movementCreditservice.model.CreditAccount;
import com.nttdata.movementCreditservice.model.Product;
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

	@Autowired
	ProductFeignClient productFeignClient;
	@Autowired
	ConfigurationFeingClient configurationFeingClient;

	@Override
	public Flux<MovementCredit> findAll() {
		return movementCreditRepository.findAll().sort((movementCredit1, movementCredit2) -> movementCredit1
				.getIdMovementCredit().compareTo(movementCredit2.getIdMovementCredit()));
	}

	@Override
	public Mono<MovementCredit> save(MovementCredit movementCredit) {
		// Long key = generateKey(MovementCredit.class.getSimpleName());
		// if (key >= 1) {

		Long idMovementCredit,count;
		count = this.findAll().collect(Collectors.counting()).blockOptional().get();
		if (count != null) {
			if (count <= 0) {
				idMovementCredit = Long.valueOf(0);
			} else {
				idMovementCredit = this.findAll()
						.collect(Collectors.maxBy(Comparator.comparing(MovementCredit::getIdMovementCredit)))
						.blockOptional().get().get().getIdMovementCredit();
			}
		} else {
			idMovementCredit = Long.valueOf(0);
		}
		movementCredit.setIdMovementCredit( idMovementCredit + 1);
		movementCredit.setCreationDate(Calendar.getInstance().getTime());
		// }
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
		log.info("recordsMovement[MovementCredit]:"+movementCredit);
		Map<String, Object> hashMap = new HashMap<String, Object>();
		CreditAccount credit = this.findByIdCredit(movementCredit.getIdCreditAccount());

		if (credit != null) {
			// Cantidad de movimientos
			SimpleDateFormat mmyyyy = new SimpleDateFormat("MM/yyyy");
			Long cantsMovi = this.findAll()
					.filter(fil -> mmyyyy.format(fil.getDateMovement())
							.equals(mmyyyy.format(Calendar.getInstance().getTime())))
					.collect(Collectors.counting()).map(cant -> {
						return cant;
					}).blockOptional().get();
			Product product = productFeignClient.findById(credit.getIdProduct());
			if (product != null) {
				Configuration configuration = configurationFeingClient.findById(product.getIdConfiguration());
				if (configuration != null) {
					/**
					 * Todas las cuentas bancarias tendrán un número máximo de transacciones
					 * (depósitos y retiros) que no cobrará comisión y superado ese número se
					 * cobrará comisión por cada transacción realizada.
					 */
					if (configuration.getQuantityMovement() != null && configuration.getQuantityMovement() >= 1) {
						if (configuration.getQuantityMovement() < cantsMovi) {
							movementCredit.setCommissionForTransaction(configuration.getTransactionFee());
						}
					} else {
						movementCredit.setCommissionForTransaction(0.00);
					}
					if (movementCredit.getTypeMovementCredit() == TypeMovementCredit.charge) {
						 Double _saldo= this.findAll()
								.filter(o -> (o.getIdCreditAccount() == movementCredit.getIdCreditAccount()))
								.map(mov -> {
									if (mov.getTypeMovementCredit() == TypeMovementCredit.charge) {
										mov.setAmount(-1 * mov.getAmount());
									}
									return mov;
								})							 
								.collect(Collectors.summingDouble(MovementCredit::getAmount)).blockOptional().get();
								//.map(_saldo -> {
									log.info("Saldo [Abono-Cargo]:" + _saldo);
									log.info("Credit:" + credit.toString());
									log.info("Saldo disponible:" + (_saldo
											+ (credit.getAmountCreditLimit() != null ? credit.getAmountCreditLimit()
													: 0.00)));

									if (movementCredit.getAmount() <= (_saldo
											+ (credit.getAmountCreditLimit() != null ? credit.getAmountCreditLimit()
													: 0.00))) {
										log.info("Registra Movimiento:");
										movementCredit.setDateMovement(Calendar.getInstance().getTime());
										this.save(movementCredit).map(_obj -> {
											hashMap.put("CreditSucces", "Registro de movimiento de "
													+ movementCredit.getTypeMovementCredit());
											hashMap.put("MovementCredit", movementCredit);
											hashMap.put("status", "success");
											hashMap.put("idMovementCredit", movementCredit.getIdMovementCredit());
											return _obj;
										}).blockOptional().get();

												//.subscribe(e -> log.info("Save:" + e.toString()));

									} else {
										hashMap.put("CreditAccount",
												"El saldo de la cuenta de credito supera el limite de credito.Saldo disponible:"
														+ (_saldo + (credit.getAmountCreditLimit() != null
																? credit.getAmountCreditLimit()
																: 0.00)));
										hashMap.put("status", "error");
										log.info("El cargo a la cuenta de credito supera el limite de credito.");
									}
									log.info("hashMap:"+hashMap);
									return Mono.just(hashMap);
								//});
					} else {
						movementCredit.setDateMovement(Calendar.getInstance().getTime());
						return this.save(movementCredit).map(_value -> {
							hashMap.put("MovementCredit", _value);
							hashMap.put("CreditSucces", "Registro de movimiento de " + _value.getTypeMovementCredit());
							hashMap.put("status", "success");
							hashMap.put("idMovementCredit", _value.getIdMovementCredit());
							return hashMap;
						});

					}

				} else {
					hashMap.put("status", "error");
					hashMap.put("configuration", "configuracion del producto no existe.");
				}
			} else {
				hashMap.put("status", "error");
				hashMap.put("product", "El producto no existe.");
			}
			return Mono.just(hashMap);
		} else {
			hashMap.put("status", "error");
			hashMap.put("credit", "Cuenta de credito no existe.");
			return Mono.just(hashMap);
		}

	}

	@Override
	public CreditAccount findByIdCredit(Long idCreditAccount) {
		CreditAccount credit = creditFeignClient.creditfindById(idCreditAccount);
		// log.info("CreditFeignClient: " + credit.toString());
		return credit;
	}

	@Override
	public Mono<Map<String, Object>> balanceInquiry(CreditAccount _creditAccount) {
		Map<String, Object> hashMap = new HashMap<String, Object>();
		CreditAccount credit = this.findByIdCredit(_creditAccount.getIdCreditAccount());
		if (credit != null) {
			return this.findAll().filter(o -> (o.getIdCreditAccount() == credit.getIdCreditAccount())).map(mov -> {
				if (mov.getTypeMovementCredit() == TypeMovementCredit.charge) {
					mov.setAmount(-1 * mov.getAmount());
				}
				// log.info("Change:"+mov.toString());
				return mov;
			}).collect(Collectors.summingDouble(MovementCredit::getAmount)).map(_value -> {
				log.info("balanceInquiry:" + _value);
				return _value;
			}).map(value -> {
				hashMap.put("StatusBalance", "El saldo de la cuenta es de:" + value);
				hashMap.put("creditBalance", value);
				hashMap.put("Credit", credit);
				hashMap.put("status", "success");
				return hashMap;
			});
		} else {
			hashMap.put("status", "error");
			hashMap.put("credit", "Cuenta de credito no existe.");
			return Mono.just(hashMap);
		}
	}

	@Override
	public Long generateKey(String nameTable) {
		return tableIdFeignClient.generateKey(nameTable);
	}

}
