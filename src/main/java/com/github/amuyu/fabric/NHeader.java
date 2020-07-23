package com.github.amuyu.fabric;

import com.github.amuyu.fabric.exception.ParseException;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.bouncycastle.util.encoders.Hex;
import org.hyperledger.fabric.protos.common.Common;
import org.hyperledger.fabric.protos.msp.Identities;
import org.hyperledger.fabric.sdk.TransactionInfo;
import org.hyperledger.fabric.sdk.transaction.ProtoUtils;

/**
 * Transaction 의 Header 정보
 */
@Builder
@Getter
@Setter
@ToString
public class NHeader {
    private String txId;
    private String channel;
    private String timestamp;
    private int type;   // Common.HeaderType
    private String nonce;
    private String creatorMspId;

    public static NHeader of(TransactionInfo info) throws ParseException {

        try {
            Common.Payload payload = Common.Payload.parseFrom(info.getEnvelope().getPayload());
            Common.Header header = payload.getHeader();

            Common.SignatureHeader signatureHeader = Common.SignatureHeader.parseFrom(header.getSignatureHeader());
            Identities.SerializedIdentity creator = Identities.SerializedIdentity.parseFrom(signatureHeader.getCreator());

            Common.ChannelHeader channelHeader = Common.ChannelHeader.parseFrom(header.getChannelHeader());
            long timestamp = ProtoUtils.getDateFromTimestamp(channelHeader.getTimestamp()).getTime();

            return NHeader.builder()
                    .txId(info.getTransactionID())
                    .channel(channelHeader.getChannelId())
                    .timestamp(timestamp + "")
                    .type(channelHeader.getType())
                    .nonce(Hex.toHexString(signatureHeader.getNonce().toByteArray()))
                    .creatorMspId(creator.getMspid())
                    .build();

        } catch (InvalidProtocolBufferException e) {
            throw new ParseException("Payload parse error", e);
        }


    }


}
