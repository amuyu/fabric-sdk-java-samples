package com.github.amuyu;

import com.github.amuyu.fabric.NProposalResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * fabric-samples 의 marbles 체인코드에서
 * initMarble 과 queryMarblesByOwner 를 invoke 하는 샘플
 */
public class InvokeSample {

    private static Logger logger = LoggerFactory.getLogger(InvokeSample.class);

    public static void main(String[] args) throws Exception {
        SampleService holder = SampleService.defaultCreate();
        holder.fabricService.setChaincode("marbles","1.0");

        // invoke
        List<String> params = Arrays.asList("marble11","blue","35","tom");
        NProposalResponse response = holder.fabricService.invoke("initMarble", params);
        logger.debug("Response:{}", response);

        Thread.sleep(3000);

        // query
        response = holder.fabricService.query("queryMarblesByOwner", Collections.singletonList("tom"));
        logger.debug("Response:{}", response);
    }
}
