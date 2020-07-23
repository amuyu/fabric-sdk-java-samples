package com.github.amuyu;

import org.hyperledger.fabric.protos.common.Common;
import org.hyperledger.fabric.sdk.BlockInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Block 이 commit 되면 BlockListener 로 block 정보를 확인하는 샘플
 *
 * BlockEvent 를 받으려면 실행 후, block  commit 되야하므로 InvokeSample 을 실행해서 새로운 Block 을 생성한다.
 * BlockListener 로 block 정보를 확인할 수 있음
 */
public class BlockEventSample {

    private static Logger logger = LoggerFactory.getLogger(BlockEventSample.class);

    public static void main(String[] argss) throws Exception {

        SampleService holder = SampleService.defaultCreate(true);
        long height = holder.fabricService.getBlockHeight();
        logger.debug("current block number:{}", height);

        holder.channel.registerBlockListener(blockEvent -> {
            logger.debug("==== Block Event Start ====");

            Common.Block block = blockEvent.getBlock();
            Common.BlockHeader blockHeader = block.getHeader();
            long n = blockHeader.getNumber();
            logger.debug("block number:{}", n);

            for (BlockInfo.EnvelopeInfo envelopeInfo : blockEvent.getEnvelopeInfos()) {
                logger.debug("evelopeType:{}", envelopeInfo.getType());
                if (envelopeInfo.getType() == BlockInfo.EnvelopeType.TRANSACTION_ENVELOPE) {
                    BlockInfo.TransactionEnvelopeInfo transactionEnvelopeInfo = (BlockInfo.TransactionEnvelopeInfo) envelopeInfo;

                    // txId
                    String txId = transactionEnvelopeInfo.getTransactionID();
                    boolean isValid = transactionEnvelopeInfo.isValid();

                    logger.debug("txId:{}", txId);
                    logger.debug("isValid:{}", isValid);

                    Iterator<BlockInfo.TransactionEnvelopeInfo.TransactionActionInfo> transactionActionInfoIterator =
                            transactionEnvelopeInfo.getTransactionActionInfos().iterator();


                    while (transactionActionInfoIterator.hasNext()) {
                        BlockInfo.TransactionEnvelopeInfo.TransactionActionInfo transactionActionInfo = transactionActionInfoIterator.next();

                        // chaincode 정보
                        String chaincodeName = transactionActionInfo.getChaincodeIDName();
                        String chaincodeVersion = transactionActionInfo.getChaincodeIDVersion();

                        logger.debug("chaincodeName:{}", chaincodeName);
                        logger.debug("chaincodeVersion:{}", chaincodeVersion);

                        List<String> args = new ArrayList<>();
                        int argsCount = transactionActionInfo.getChaincodeInputArgsCount();
                        if (argsCount > 0) {
                            for (int i = 0; i < argsCount; i++) {
                                args.add(new String(transactionActionInfo.getChaincodeInputArgs(i), StandardCharsets.UTF_8));
                            }
                        }
                        logger.debug("args:{}", args);
                    }
                }
            }
        });

        Thread.sleep(30000);

    }
}
