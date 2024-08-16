package com.example.restea.live.controller;

import com.example.restea.live.service.LiveService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class LivekitController {
    private final LiveService liveService;

    // LiveKit 웹훅 이벤트를 수신 -> LiveKit Server로 부터 받음, 항상 ok를 반환해 줘야함.
    @PostMapping(value = "/livekit/webhook", consumes = "application/webhook+json")
    public ResponseEntity<String> receiveWebhook(@RequestHeader("Authorization") String authHeader,
                                                 @RequestBody String body) {

        liveService.webHook(authHeader, body);

        return ResponseEntity.ok("ok");
    }

}
