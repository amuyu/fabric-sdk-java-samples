package com.github.amuyu.fabric;

import com.github.amuyu.fabric.exception.ParseException;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hyperledger.fabric.sdk.TransactionInfo;

import java.util.List;

@Builder
@Getter
@Setter
@ToString
public class NTransactionInfo {
    private NHeader header;
    private List<NPayload> payloads;


    public static NTransactionInfo of(TransactionInfo info) throws ParseException {
        // Header
        NHeader header = NHeader.of(info);
        // Payload
        List<NPayload> payloads = NPayload.parseFrom(info);
        return NTransactionInfo.builder()
                .header(header)
                .payloads(payloads)
                .build();
    }

}
