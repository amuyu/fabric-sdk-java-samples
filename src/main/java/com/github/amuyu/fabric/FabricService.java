package com.github.amuyu.fabric;

import com.github.amuyu.fabric.exception.FabricServiceException;
import com.github.amuyu.fabric.exception.ParseException;
import org.hyperledger.fabric.sdk.*;
import org.hyperledger.fabric.sdk.exception.*;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Fabric SDK 서비스 제공 Class
 * <p>
 * transaction invoke, query
 */
public class FabricService {
    private Logger logger = LoggerFactory.getLogger(FabricService.class);

    private HFClient hfClient;
    private Channel curChannel;
    private ChaincodeID chaincodeID;
    private ChannelManger channelManger;

    public FabricService(Client client) throws FabricServiceException {
        hfClient = createHFClient(client);
        channelManger = new ChannelManger();
    }

    public FabricService(NetworkConfig networkConfig, Client client) throws FabricServiceException {
        hfClient = createHFClient(client);
        channelManger = new ChannelManger();
        connectToChannel(networkConfig);
    }

    /**
     * client 정보를 다시 셋팅
     * 호출 후, {@link #connectToChannel(NetworkConfig)} 호출해야함
     *
     * @param client
     * @throws FabricServiceException
     */
    public void resetClient(Client client) throws FabricServiceException {
        hfClient = createHFClient(client);
        channelManger.invalidateAll();
        curChannel = null;
    }

    public Channel connectToChannel(NetworkConfig networkConfig) throws FabricServiceException {
        return connectToChannel(networkConfig, networkConfig.getChannelName());
    }

    /**
     * {@link NetworkConfig} 정보로부터 channel 객체를 초기화한다
     *
     * @param networkConfig
     * @throws FabricServiceException
     */
    public Channel connectToChannel(NetworkConfig networkConfig, String channelName) throws FabricServiceException {
        Channel c = channelManger.get(channelName);
        if (c == null) {
            try {
                // cache removalevent 가 아직 발생하지 않은 겨웅, hfClient 에 channel 정보가 있음
                c = hfClient.getChannel(channelName);
            } catch (NullPointerException e) {
                // channels 에서 channel 호출할 때 발생할 수 있음
            }
            if (c == null) {
                c = createChannel(networkConfig, channelName);
            }
            channelManger.put(c);
        }
        curChannel = c;
        return c;
    }

    /**
     * HFClient instance 를 생성한다.
     *
     * @param client peer, orderer 를 호출할 사용자 정보
     * @return HFClient
     * @throws FabricServiceException
     */
    private HFClient createHFClient(Client client) throws FabricServiceException {
        try {
            CryptoSuite cryptoSuite = CryptoSuite.Factory.getCryptoSuite();
            HFClient c = HFClient.createNewInstance();
            c.setCryptoSuite(cryptoSuite);
            c.setUserContext(client);
            return c;
        } catch (IllegalAccessException | InstantiationException | ClassNotFoundException |
                CryptoException | org.hyperledger.fabric.sdk.exception.InvalidArgumentException |
                NoSuchMethodException | InvocationTargetException e) {
            throw new FabricServiceException(e.getMessage());
        }
    }

    /**
     * Channel instance 를 생성한다.
     *
     * @param networkConfig channelName, peer, orderer 설정 정보
     * @return Channel
     * @throws FabricServiceException
     */
    private Channel createChannel(NetworkConfig networkConfig) throws FabricServiceException {
        return createChannel(networkConfig, networkConfig.getChannelName());
    }

    /**
     * Channel instance 를 생성한다.
     *
     * @param networkConfig channelName, peer, orderer 설정 정보
     * @return Channel
     * @throws FabricServiceException
     */
    private Channel createChannel(NetworkConfig networkConfig, String channelName) throws FabricServiceException {
        try {
            Channel c = hfClient.newChannel(channelName);
            for (Node peer : networkConfig.getPeers()) {
                c.addPeer(hfClient.newPeer(peer.getName(), peer.getUrl(), peer.getProperties()));
            }

            c.addOrderer(hfClient.newOrderer(networkConfig.getOrderer().getName(),
                    networkConfig.getOrderer().getUrl(), networkConfig.getOrderer().getProperties()));

            if (networkConfig.getEventHub() != null) {
                c.addEventHub(hfClient.newEventHub(networkConfig.getEventHub().getName(),
                        networkConfig.getEventHub().getUrl()));
            }

            c.initialize();
            logger.info("CHANNEL [{}] INITIALIZED : [{}]", c.getName(), c.isInitialized());

            return c;
        } catch (InvalidArgumentException | TransactionException e) {
            throw new FabricServiceException(e.getMessage());
        }
    }

    public void setChaincode(String name, String version) {
        chaincodeID = ChaincodeID.newBuilder()
                .setName(name)
                .setVersion(version)
                .build();
    }

    public void setChaincodeID(ChaincodeID chaincodeID) {
        if (chaincodeID == null) {
            throw new IllegalArgumentException("chaincodeID is null");
        }
        this.chaincodeID = chaincodeID;
    }

    /**
     * proposal 이나 transaction 을 전송하기 위해서는
     * channel 정보와 chaincode 정보가 셋팅되어 있어야 한다
     */
    private void checkForInvokeAndQuery() {
        checkChannel();
        checkChaincodeID();
    }

    /**
     * chaincodeID 가 null 인지 확인
     * {@link #setChaincode(String, String)} 로 체인코드 정보 셋팅
     */
    private void checkChaincodeID() {
        if (chaincodeID == null) {
            throw new IllegalArgumentException("chaincodeID is null");
        }
    }

    /**
     * channel 이 null 인지 확인
     * {@link #connectToChannel(NetworkConfig)} 로 channel 정보 셋팅
     */
    private void checkChannel() {
        if (curChannel == null) {
            throw new IllegalArgumentException("channel is null");
        }
    }

    /**
     * 체인코드 invoke proposal 후, transaction 을 전송한다.
     *
     * @throws FabricServiceException
     */
    public NProposalResponse invoke(String fcn, List<String> args) throws FabricServiceException {
        checkForInvokeAndQuery();

        try {

            TransactionProposalRequest request = hfClient.newTransactionProposalRequest();
            request.setChaincodeID(chaincodeID);
            request.setFcn(fcn);
            request.setArgs(new ArrayList<>(args));
            request.setProposalWaitTime(180000);

            Collection<ProposalResponse> responses = curChannel.sendTransactionProposal(request);

            NProposalResponse r = proposalResponseInterceptor(responses);
            // 성공일 때만 sendTransaction 호출
            if (r.getSuccess()) {
                curChannel.sendTransaction(responses).get();
            }
            return r;

        } catch (InvalidArgumentException | ProposalException | ExecutionException e) {
            throw new FabricServiceException(e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FabricServiceException(e.getMessage());
        }

    }

    /**
     * 체인코드 Query proposal 을 전송한다.
     *
     * @throws FabricServiceException
     */
    public NProposalResponse query(String fcn, List<String> args) throws FabricServiceException {
        checkForInvokeAndQuery();

        QueryByChaincodeRequest request = hfClient.newQueryProposalRequest();
        request.setChaincodeID(chaincodeID);
        request.setFcn(fcn);
        request.setArgs(new ArrayList<>(args));
        request.setProposalWaitTime(180000);

        try {
            Collection<ProposalResponse> responses = curChannel.queryByChaincode(request);
            return proposalResponseInterceptor(responses);
        } catch (InvalidArgumentException | ProposalException e) {
            throw new FabricServiceException(e.getMessage(), e);
        }
    }

    public void installChaincode(String sourcePath) throws FabricServiceException {
        installChaincode(sourcePath, null);
    }

    /**
     * 체인코드 install
     *
     * @param sourcePath 체인코드 source 위치
     * @param metaPath   체인코드 meta-inf 위치
     * @throws FabricServiceException
     */
    public NProposalResponse installChaincode(String sourcePath, String metaPath) throws FabricServiceException {
        checkForInvokeAndQuery();
        if (chaincodeID.getPath() == null) {
            throw new IllegalArgumentException("chaincodeID.path is null");
        }

        try {
            InstallProposalRequest installProposalRequest = hfClient.newInstallProposalRequest();
            installProposalRequest.setChaincodeID(chaincodeID);
            installProposalRequest.setChaincodeSourceLocation(new File(sourcePath));
            if (metaPath != null) {
                installProposalRequest.setChaincodeMetaInfLocation(new File(metaPath));
            }
            installProposalRequest.setProposalWaitTime(90000);

            Collection<ProposalResponse> responses = hfClient.sendInstallProposal(installProposalRequest, curChannel.getPeers());
            return proposalResponseInterceptor(responses);

        } catch (InvalidArgumentException | ProposalException e) {
            throw new FabricServiceException(e.getMessage());
        }
    }

    public NProposalResponse instantiateChaincode() throws FabricServiceException {
        return instantiateChaincode(null);
    }

    public NProposalResponse instantiateChaincode(String policyPath) throws FabricServiceException {
        checkForInvokeAndQuery();
        if (chaincodeID.getPath() == null) {
            throw new IllegalArgumentException("chaincodeID.path is null");
        }

        try {
            InstantiateProposalRequest instantiateProposalRequest = hfClient.newInstantiationProposalRequest();
            instantiateProposalRequest.setChaincodeLanguage(TransactionRequest.Type.GO_LANG);
            instantiateProposalRequest.setChaincodeID(chaincodeID);
            instantiateProposalRequest.setProposalWaitTime(90000);
            instantiateProposalRequest.setFcn("init");
            instantiateProposalRequest.setArgs("");

            if (policyPath != null) {
                try {
                    ChaincodeEndorsementPolicy chaincodeEndorsementPolicy = new ChaincodeEndorsementPolicy();
                    chaincodeEndorsementPolicy.fromYamlFile(new File(policyPath));
                    instantiateProposalRequest.setChaincodeEndorsementPolicy(chaincodeEndorsementPolicy);
                } catch (IOException | ChaincodeEndorsementPolicyParseException e) {
                    throw new FabricServiceException(e.getMessage());
                }
            }

            Collection<ProposalResponse> responses = curChannel.sendInstantiationProposal(instantiateProposalRequest, curChannel.getPeers());
            NProposalResponse r = proposalResponseInterceptor(responses);
            if (r.getSuccess()) {
                curChannel.sendTransaction(responses).get();
            }
            return r;

        } catch (InvalidArgumentException | ProposalException | ExecutionException e) {
            throw new FabricServiceException(e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FabricServiceException(e.getMessage());
        }
    }

    public NProposalResponse upgradeChaincode() throws FabricServiceException {
        checkForInvokeAndQuery();
        if (chaincodeID.getPath() == null) {
            throw new IllegalArgumentException("chaincodeID.path is null");
        }

        try {
            UpgradeProposalRequest upgradeProposalRequest = hfClient.newUpgradeProposalRequest();
            upgradeProposalRequest.setChaincodeLanguage(TransactionRequest.Type.GO_LANG);
            upgradeProposalRequest.setFcn("init");
            upgradeProposalRequest.setArgs("");
            upgradeProposalRequest.setChaincodeID(chaincodeID);
            upgradeProposalRequest.setProposalWaitTime(90000);

            Collection<ProposalResponse> responses = curChannel.sendUpgradeProposal(upgradeProposalRequest, curChannel.getPeers());
            return proposalResponseInterceptor(responses);

        } catch (InvalidArgumentException | ProposalException e) {
            throw new FabricServiceException(e.getMessage());
        }
    }

    /**
     * 블록체인 정보 조회
     *
     * @return
     */
    public BlockchainInfo[] queryBlockchainInfo() {
        AtomicLong atomicHeight = new AtomicLong(Long.MAX_VALUE);
        BlockchainInfo[] blockchainInfo = new BlockchainInfo[1];
        curChannel.getPeers().forEach(peer -> {
            try {
                BlockchainInfo channelInfo;
                channelInfo = curChannel.queryBlockchainInfo(peer, hfClient.getUserContext());
                final long height = channelInfo.getHeight();
                if (height < atomicHeight.longValue()) {
                    atomicHeight.set(height);
                    blockchainInfo[0] = channelInfo;
                }
            } catch (ProposalException | InvalidArgumentException e) {
                e.printStackTrace();
            }
        });
        return blockchainInfo;
    }

    /**
     * last block height 조회
     *
     * @return Object
     */
    public long getBlockHeight() {
        BlockchainInfo[] blockchainInfo = queryBlockchainInfo();
        if (blockchainInfo[0] == null) {
            return 0L;
        }
        return blockchainInfo[0].getHeight();
    }

    /**
     * 블록 정보 조회
     *
     * @param height
     * @return
     * @throws ProposalException
     * @throws InvalidArgumentException
     */
    public BlockInfo queryBlockByNumber(long height) throws ProposalException, InvalidArgumentException {
        checkChannel();
        return curChannel.queryBlockByNumber(height);
    }

    /**
     * TransactionInfo 조회
     *
     * @param txId the ID of the transaction
     * @throws FabricServiceException
     */
    public NTransactionInfo getTransactionInfo(String txId) throws ParseException, FabricServiceException {
        checkChannel();
        try {
            TransactionInfo info = curChannel.queryTransactionByID(txId);
            return NTransactionInfo.of(info);
        } catch (ProposalException | InvalidArgumentException e) {
            throw new FabricServiceException("Query by transactionID error", e);
        }
    }

    /**
     * ProposalResponse 를 처리함
     * <p>
     * responses 는 collection 이지만 한 개의 peer 에만 전송하므로 responses 는 1개임
     *
     * @param responses peer 에게 proposal 후 받는 response
     * @return NProposalResponse
     * @throws InvalidArgumentException
     */
    private NProposalResponse proposalResponseInterceptor(Collection<ProposalResponse> responses) throws InvalidArgumentException {
        if (responses.isEmpty()) {
            throw new IllegalArgumentException("responses.size is 0");
        }

        ProposalResponse r = null;
        for (ProposalResponse response : responses) {
            r = response;
            logger.info("chaincode propsal - chaincode: name=[{}], version=[{}], status=[{}], message[{}]",
                    response.getChaincodeID().getName(), response.getChaincodeID().getVersion(),
                    response.getStatus(), response.getMessage());
        }
        return NProposalResponse.of(r);
    }

    private Channel.TransactionOptions channelTxOptions() {
        checkChannel();
        return new Channel.TransactionOptions()
                .orderers(curChannel.getOrderers())
                .userContext(hfClient.getUserContext())
                .nOfEvents(Channel.NOfEvents.nofNoEvents);
    }

    /**
     * 지정 블록 범위에 포함된 트랜잭션 조회
     *
     * @param fromBlock
     * @param toBlock
     * @return List<Transaction>
     */
    public List<Transaction> getTransactions(long fromBlock, long toBlock) throws TransactionException {

        List<Transaction> transactions = null;

        try {
            BlockchainInfo[] blockchainInfo = queryBlockchainInfo();
            long lastBlock = blockchainInfo[0].getHeight();
            if (lastBlock < fromBlock) {
                throw new IllegalArgumentException("현재 block(" + lastBlock + ")이 from(" + fromBlock + ") 보다 작습니다.");
            }
            long endBlock = toBlock;
            if (toBlock < lastBlock) endBlock = lastBlock;

            transactions = new ArrayList<>();

            for (long h = fromBlock; h < endBlock; h++) {
                BlockInfo blockInfo = queryBlockByNumber(h);

                for (BlockInfo.EnvelopeInfo envelopeInfo : blockInfo.getEnvelopeInfos()) {
                    if (envelopeInfo.getType() == BlockInfo.EnvelopeType.TRANSACTION_ENVELOPE) {

                        BlockInfo.TransactionEnvelopeInfo transactionEnvelopeInfo = (BlockInfo.TransactionEnvelopeInfo) envelopeInfo;

                        // txId
                        String txId = transactionEnvelopeInfo.getTransactionID();
                        boolean isValid = transactionEnvelopeInfo.isValid();


                        Iterator<BlockInfo.TransactionEnvelopeInfo.TransactionActionInfo> transactionActionInfoIterator =
                                transactionEnvelopeInfo.getTransactionActionInfos().iterator();

                        List<TransactionAction> actions = new ArrayList<>();

                        while (transactionActionInfoIterator.hasNext()) {
                            BlockInfo.TransactionEnvelopeInfo.TransactionActionInfo transactionActionInfo = transactionActionInfoIterator.next();

                            // chaincode 정보
                            String chaincodeName = transactionActionInfo.getChaincodeIDName();
                            String chaincodeVersion = transactionActionInfo.getChaincodeIDVersion();

                            List<String> args = new ArrayList<>();

                            int argsCount = transactionActionInfo.getChaincodeInputArgsCount();
                            if (argsCount > 0) {
                                for (int i = 0; i < argsCount; i++) {
                                    args.add(new String(transactionActionInfo.getChaincodeInputArgs(i), StandardCharsets.UTF_8));
                                }
                            }

                            actions.add(TransactionAction.builder()
                                    .chaincodeName(chaincodeName)
                                    .chaincodeVersion(chaincodeVersion)
                                    .args(args)
                                    .build());

                        }

                        transactions.add(Transaction.builder()
                                .blockNumber(h)
                                .txId(txId)
                                .isValid(isValid)
                                .actions(actions)
                                .build());
                    }

                }
            }


        } catch (InvalidArgumentException | ProposalException e) {
            throw new TransactionException(e.getMessage());
        }
        return transactions;
    }

}
