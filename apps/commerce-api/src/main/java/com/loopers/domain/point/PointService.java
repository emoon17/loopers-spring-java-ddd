package com.loopers.domain.point;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class PointService {

    private final PointRepository pointRepository;

    public PointModel getPointModelByLoginId(String loginId) {
        return pointRepository.findPointByLoginId(loginId);
    }
}
