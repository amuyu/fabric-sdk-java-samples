`fabric-java-sdk` 를 사용한 간단한 샘플입니다.




## 샘플

| Sample          | 설명                                       |
| ------------------- | ------------------------------------------ |
| CAAdminEnrollSample | Fabric-ca 서버 admin 계정 enroll 하는 샘플 |
| RegisterUserSample | 체인코드 호출을 위한 계정 등록 및 msp 저장하는 샘플|
| InvokeSample | 체인코드 invoke/query 하는 샘플 |
| BlockEventSample | BlockListener 를 통해 commit 된 block 정보를 확인하는 샘플 |



## 시작하기

#### Fabric network

[Off chain data](https://github.com/hyperledger/fabric-samples/tree/master/off_chain_data) 샘플에서 네트워크를 실행한다. 

```
./startFabric.sh
```



#### 샘플에서 사용할 사용자 등록

`fabric-sdk-java` 를 사용해서 블록체인 network 와 communication 하기 위해서 사용자 등록이 필요하다.

1. `CAAdminEnrollSample` 실행해서 admin 사용자 enroll 
2. `RegisterUserSample` 실행해서 사용자 등록



#### 샘플 실행

사용자 등록 후, `InvokeSample` 이나 `BlockEventSample` 을 호출해서 동작을 확인할 수 있다.

