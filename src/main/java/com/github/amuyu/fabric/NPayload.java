package com.github.amuyu.fabric;

import com.github.amuyu.fabric.exception.ParseException;
import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hyperledger.fabric.protos.common.Common;
import org.hyperledger.fabric.protos.msp.Identities;
import org.hyperledger.fabric.protos.peer.Chaincode;
import org.hyperledger.fabric.protos.peer.FabricProposal;
import org.hyperledger.fabric.protos.peer.FabricProposalResponse;
import org.hyperledger.fabric.protos.peer.FabricTransaction;
import org.hyperledger.fabric.sdk.TransactionInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Transaction 의 payload 정보
 */
@Builder
@Getter
@Setter
@ToString
public class NPayload {

    private int responseStatus;
    private List<String> endorsers;
    private String chaincodeName;
    private String chaincodeVersion;
    private String fcn;
    private List<String> chaincodeInputArgs;
    private String proposalResponsePayload;

    public static NPayload of(FabricTransaction.TransactionAction action) throws ParseException {
        try {
            FabricTransaction.ChaincodeActionPayload chaincodeActionPayload = FabricTransaction.ChaincodeActionPayload.parseFrom(action.getPayload());
            FabricTransaction.ChaincodeEndorsedAction endorsedAction = chaincodeActionPayload.getAction();
            FabricProposalResponse.ProposalResponsePayload proposalResponsePayload = FabricProposalResponse.ProposalResponsePayload.parseFrom(endorsedAction.getProposalResponsePayload());
            FabricProposal.ChaincodeAction chaincodeAction = FabricProposal.ChaincodeAction.parseFrom(proposalResponsePayload.getExtension());
            Chaincode.ChaincodeID chaincodeID = chaincodeAction.getChaincodeId();

            FabricProposal.ChaincodeProposalPayload proposalPayload = FabricProposal.ChaincodeProposalPayload.parseFrom(chaincodeActionPayload.getChaincodeProposalPayload());
            Chaincode.ChaincodeInvocationSpec chaincodeInvocation = Chaincode.ChaincodeInvocationSpec.parseFrom(proposalPayload.getInput());
            Chaincode.ChaincodeInput chaincodeInput = chaincodeInvocation.getChaincodeSpec().getInput();
            String fcn = null;
            List<String> chaincodeInputArgs = new ArrayList<>();
            for (int i = 0; i < chaincodeInput.getArgsCount(); i++) {
                if (i == 0) {
                    fcn = chaincodeInput.getArgs(i).toStringUtf8();
                } else {
                    chaincodeInputArgs.add(chaincodeInput.getArgs(i).toStringUtf8());
                }
            }

            List<String> endorsers = new ArrayList<>();
            List<FabricProposalResponse.Endorsement> endorsements = endorsedAction.getEndorsementsList();
            for (int e = 0; e < endorsedAction.getEndorsementsCount(); e++) {
                Identities.SerializedIdentity endorser = Identities.SerializedIdentity.parseFrom(endorsements.get(e).getEndorser());
                endorsers.add(endorser.getMspid());
            }

            String responsePayload = null;
            ByteString bResponsePayload = chaincodeAction.getResponse().getPayload();
            if (bResponsePayload != null) {
                responsePayload = bResponsePayload.toStringUtf8();
            }

            return NPayload.builder()
                    .responseStatus(chaincodeAction.getResponse().getStatus())
                    .endorsers(endorsers)
                    .chaincodeName(chaincodeID.getName())
                    .chaincodeVersion(chaincodeID.getVersion())
                    .fcn(fcn)
                    .chaincodeInputArgs(chaincodeInputArgs)
                    .proposalResponsePayload(responsePayload)
                    .build();

        } catch (InvalidProtocolBufferException e) {
            throw new ParseException("Payload parse error", e);
        }

    }

    public static List<NPayload> parseFrom(TransactionInfo info) throws ParseException {
        try {
            Common.Payload payload = Common.Payload.parseFrom(info.getEnvelope().getPayload());
            FabricTransaction.Transaction transaction = FabricTransaction.Transaction.parseFrom(payload.getData());
            List<FabricTransaction.TransactionAction> actions = transaction.getActionsList();

            List<NPayload> payloads = new ArrayList<>();
            for (int i = 0; i < transaction.getActionsCount(); i++) {
                payloads.add(NPayload.of(actions.get(i)));
            }
            return payloads;
        } catch (InvalidProtocolBufferException e) {
            throw new ParseException("Payload parse error", e);
        }

    }
}
