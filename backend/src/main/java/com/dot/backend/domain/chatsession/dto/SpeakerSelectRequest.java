package com.dot.backend.domain.chatsession.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SpeakerSelectRequest {
    private String speakerName; // 선택한 화자 이름
}
