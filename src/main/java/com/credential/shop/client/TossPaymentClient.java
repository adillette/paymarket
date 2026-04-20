package com.credential.shop.client;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.credential.shop.global.TossPaymentException;

@Component
public class TossPaymentClient {

  private static final String TOSS_CONFIRM_URL= "https://api.tosspayments.com/v1/payments/confirm";
  //토스 서버의 결제승인 api 주소
  
  @Value("${toss.payments.secret-key}")//스프링이 런타임에 값 주입 설정값 바인딩용
  private String secretKey;

  private final RestTemplate restTemplate = new RestTemplate();
  //외부 HTTP 호출용 객체 토스 서버에 요청 보내는 역할
  public Map<String, Object> confirm(String paymentKey,String orderId, int amount){
   String encoded=Base64.getEncoder()
                    .encodeToString((secretKey+":").getBytes());
    //basic 인증용 인코딩 형식: "secretKey:" → 이걸 Base64로 변환 basicAuth 규격
    //"username:password" 형태라서 

    HttpHeaders headers= new HttpHeaders();
    //http 요청 헤더 생성

    headers.set("Authorization", "Basic " + encoded); // 인증 헤더 설정 → 토스가 이걸 보고 "누가 요청했는지" 확인
    headers.setContentType(MediaType.APPLICATION_JSON); // 요청 body가 JSON 형식이라는 걸 명시
				
    // headers.set("Idempotency-Key", paymentKey); // ← 여기 추가

     

        Map<String, Object> body = Map.of(
                "paymentKey", paymentKey,
                "orderId", orderId,
                "amount", amount
        );
         
        // 실제로 토스에 보낼 데이터
        // paymentKey → 프론트에서 받은 결제 식별값
        // orderId → 너희 시스템 주문 ID
        // amount → 결제 금액 (검증용, 틀리면 승인 거절됨)

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);
        // 헤더 + 바디를 하나로 묶은 HTTP 요청 객체

        ResponseEntity<Map> response =restTemplate.postForEntity(
          TOSS_CONFIRM_URL, request, Map.class);

        // POST 요청 실행

        if(!response.getStatusCode().is2xxSuccessful()){
          throw new TossPaymentException();
        }
        return response.getBody();
       

        
  }
  
}
