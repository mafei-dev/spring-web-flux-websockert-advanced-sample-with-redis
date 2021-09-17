package com.mafei.websocket.service;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RTopicReactive;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.util.Arrays;

@Service
@Slf4j
public class SWService implements WebSocketHandler {

    @Autowired
    private RedissonReactiveClient client;

    public static Flux<String> sendMessage() {
        return Flux.interval(Duration.ofSeconds(1)).map(aLong -> "hi " + aLong).startWith(Arrays.asList("your welcome."));
    }

    @Override
    public Mono<Void> handle(WebSocketSession webSocketSession) {
        String service = getParamFromURL(webSocketSession.getHandshakeInfo().getUri(), "service");
        log.info("service name is {}", service);
        log.info("info {} ", webSocketSession.getHandshakeInfo());
        RTopicReactive healthTopic = client.getTopic("health", StringCodec.INSTANCE);

        webSocketSession.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .doOnNext(webSocketMessage -> {
                    System.out.println("webSocketMessage " + webSocketMessage);
                })
                .flatMap(healthTopic::publish)
                .doOnError(Throwable::printStackTrace)
                .doFinally(signalType -> {
                    System.out.println("receive.doFinally");
                })
                .subscribe();
        Flux<WebSocketMessage> fluxMsg = healthTopic.getMessages(String.class)
                .map(webSocketSession::textMessage)
                .doOnError(Throwable::printStackTrace)
                .doFinally(signalType -> {
                    System.out.println("send.doFinally");
                });

        return webSocketSession.send(fluxMsg);
    }

    private String getParamFromURL(URI uri, String param) {
        return UriComponentsBuilder.fromUri(uri)
                .build()
                .getQueryParams()
                .getFirst(param);
    }
}