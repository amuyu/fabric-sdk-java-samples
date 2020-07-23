package com.github.amuyu.fabric;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Builder
@Getter
@Setter
@ToString
public class Transaction {
    long blockNumber;
    String txId;
    boolean isValid;
    List<TransactionAction> actions;
}
