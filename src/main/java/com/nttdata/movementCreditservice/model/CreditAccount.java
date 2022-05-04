package com.nttdata.movementCreditservice.model;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public final class CreditAccount extends Account {
	private Long idCreditAccount;
	private Long idProduct;
	private Double amountCreditLimit;
	private Long idAccount;
}
