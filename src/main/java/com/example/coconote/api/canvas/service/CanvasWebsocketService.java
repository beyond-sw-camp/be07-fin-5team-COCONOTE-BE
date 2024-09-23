package com.example.coconote.api.canvas.service;

import com.example.coconote.api.canvas.dto.request.ChatMessage;
import com.example.coconote.api.canvas.dto.response.CanvasDetResDto;
import com.example.coconote.api.canvas.dto.response.CanvasListResDto;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
@Service
public class CanvasWebsocketService {

}
