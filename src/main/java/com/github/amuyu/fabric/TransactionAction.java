package com.github.amuyu.fabric;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Builder
@Getter
@Setter
public class TransactionAction {
    String chaincodeName;
    String chaincodeVersion;
    List<String> args;
}
