package com.github.amuyu;

import com.github.amuyu.fabric.ChannelManger;
import com.github.amuyu.fabric.Client;
import com.github.amuyu.fabric.ca.NEnrollment;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ChannelManagerTest {
    private static Logger logger = LoggerFactory.getLogger(ChannelManagerTest.class);
    private HFClient hfClient;

    @Before
    public void setUp() throws Exception {
        // dummy client 생성
        Client client = Client.builder()
                .name("peer0")
                .mspId("OrgMSP1")
                .enrollment(NEnrollment.builder().build())
                .build();
        CryptoSuite cryptoSuite = CryptoSuite.Factory.getCryptoSuite();
        hfClient = HFClient.createNewInstance();
        hfClient.setCryptoSuite(cryptoSuite);
        hfClient.setUserContext(client);
    }

    @Test
    public void expireAfterWrite() throws InterruptedException, InvalidArgumentException {
        ChannelManger manager = new ChannelManger(10L, TimeUnit.SECONDS);
        List<Channel> channels = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Channel ch = hfClient.newChannel("ch" + String.format("%03d", i));
            channels.add(ch);
            manager.put(ch);
        }

        Thread.sleep(5 * 1000);

        for (Channel c : channels) {
            Assert.assertNotNull(manager.get(c.getName()));
        }

        Thread.sleep(5 * 1000);

        for (Channel c : channels) {
            Assert.assertNull(manager.get(c.getName()));
        }
    }

    @Test
    public void invalidateAll() throws InvalidArgumentException {
        ChannelManger manager = new ChannelManger(10L, TimeUnit.SECONDS);
        List<Channel> channels = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Channel ch = hfClient.newChannel("ch" + String.format("%03d", i));
            channels.add(ch);
            manager.put(ch);
        }

        for (Channel c : channels) {
            Assert.assertNotNull(manager.get(c.getName()));
        }

        manager.invalidateAll();

        for (Channel c : channels) {
            Assert.assertNull(manager.get(c.getName()));
        }
    }

    @Test
    public void invalidate() throws InvalidArgumentException {
        ChannelManger manager = new ChannelManger(10L, TimeUnit.SECONDS);
        String chName = "helloA";
        manager.put(hfClient.newChannel(chName));
        Assert.assertNotNull(manager.get(chName));
        manager.invalidate(chName);
        Assert.assertNull(manager.get(chName));
    }

    @Test
    public void expireAndPut() throws InvalidArgumentException, InterruptedException {
        ChannelManger manager = new ChannelManger(2L, TimeUnit.SECONDS);
        String chName = "helloA";
        Channel channel = hfClient.newChannel(chName);
        manager.put(channel);

        Thread.sleep(10000);

        channel = manager.get(chName);
        Assert.assertNull(channel);

        try {
            channel = hfClient.getChannel(chName);
            logger.debug("hfClient has channel:{}", channel);
            manager.put(channel);
        } catch (NullPointerException e) {
            manager.put(hfClient.newChannel(chName));
        }

        channel = manager.get(chName);
        Assert.assertNotNull(channel);
        Assert.assertFalse(channel.isShutdown());
        logger.debug("channel:{} ", channel);

        Thread.sleep(10000);

        hfClient.getChannel(chName).shutdown(true);

        try {
            channel = hfClient.getChannel(chName);
            manager.put(channel);
        } catch (NullPointerException e) {
            logger.debug("hfClient {} channel is null", chName);
            manager.put(hfClient.newChannel(chName));
        }

        channel = manager.get(chName);
        Assert.assertNotNull(channel);
        Assert.assertFalse(channel.isShutdown());
        logger.debug("channel:{} ", channel);

    }

}
