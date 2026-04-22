package com.credential.shop.domain;

public enum PaymentStatus {
READY, 
PROCESSING,//redis 상태 처리흐름용 toss api 호출중
PAID, //db 저장까지 완료
FAILED,
RETRYING, //재시도중(redis 상태 처리흐름용)
UNKNOWN, //응답 유실 재시도 필요(redis 상태 처리흐름용)

}
