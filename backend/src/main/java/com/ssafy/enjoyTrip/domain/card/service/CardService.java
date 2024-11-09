package com.ssafy.enjoyTrip.domain.card.service;

import com.ssafy.enjoyTrip.domain.attraction.repository.SidoRepository;
import com.ssafy.enjoyTrip.domain.card.dto.CardListResponseDto;
import com.ssafy.enjoyTrip.domain.card.dto.LocationRequest;
import com.ssafy.enjoyTrip.domain.card.entity.Card;
import com.ssafy.enjoyTrip.domain.card.entity.UserCard;
import com.ssafy.enjoyTrip.domain.card.repository.CardRepository;
import com.ssafy.enjoyTrip.domain.card.repository.UserCardRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CardService {
    private final CardRepository cardRepository;
    private final UserCardRepository userCardRepository;
    private final SidoRepository sidoRepository;

    @Getter
    @AllArgsConstructor
    private static class RegionCoordinates {
        private final double southLatitude;
        private final double northLatitude;
        private final double westLongitude;
        private final double eastLongitude;

        public boolean contains(double latitude, double longitude) {
            return latitude >= southLatitude &&
                    latitude <= northLatitude &&
                    longitude >= westLongitude &&
                    longitude <= eastLongitude;
        }
    }

    // 각 지역별 위도, 경도 경계
    private static final Map<Long, RegionCoordinates> REGION_BOUNDARIES = new HashMap<>() {{
        // 특별시/광역시
        put(1L, new RegionCoordinates(37.4288, 37.7017, 126.7644, 127.1840)); // 서울
        put(2L, new RegionCoordinates(37.3794, 37.7242, 126.3644, 126.7156)); // 인천
        put(3L, new RegionCoordinates(36.1098, 36.4895, 127.2465, 127.5211)); // 대전
        put(4L, new RegionCoordinates(35.6016, 36.0103, 128.3420, 128.7607)); // 대구
        put(5L, new RegionCoordinates(35.0905, 35.2472, 126.6518, 126.9539)); // 광주
        put(6L, new RegionCoordinates(34.8799, 35.3891, 128.7369, 129.3160)); // 부산
        put(7L, new RegionCoordinates(35.4669, 35.7366, 129.0756, 129.4729)); // 울산
        put(8L, new RegionCoordinates(36.4641, 36.6284, 127.1565, 127.3649)); // 세종

        // 도 단위
        put(31L, new RegionCoordinates(36.8924, 38.2846, 126.3923, 127.8264)); // 경기도
        put(32L, new RegionCoordinates(37.0498, 38.6149, 127.0495, 129.3722)); // 강원도
        put(33L, new RegionCoordinates(36.0657, 37.2173, 127.2493, 128.7959)); // 충북
        put(34L, new RegionCoordinates(35.9832, 37.0291, 125.9066, 127.3458)); // 충남
        put(35L, new RegionCoordinates(35.7133, 37.1909, 127.8108, 129.6919)); // 경북
        put(36L, new RegionCoordinates(34.5565, 35.6753, 127.4669, 129.2876)); // 경남
        put(37L, new RegionCoordinates(35.0641, 35.9942, 126.3322, 127.8573)); // 전북
        put(38L, new RegionCoordinates(33.8968, 35.4961, 125.0662, 127.5347)); // 전남
        put(39L, new RegionCoordinates(33.1060, 34.0070, 126.1524, 126.9848)); // 제주
    }};

    public Card processUserLocation(LocationRequest location, String memberId) {  // memberId 타입 변경
        // 위치 기반으로 sido_code 찾기
        Long sidoCode = determineSidoCode(location.getLatitude(), location.getLongitude());

        if (sidoCode != null) {
            // 해당 지역의 카드가 있는지 확인
            Card card = cardRepository.findByAreaCode(sidoCode)
                    .orElseThrow(() -> new RuntimeException("Card not found for area code: " + sidoCode));

            // 사용자가 이미 해당 카드를 가지고 있는지 확인
            if (!hasUserCard(memberId, card.getCardId())) {
                grantCardToUser(memberId, card);
                return card;
            }
        }
        return null;
    }

    private Long determineSidoCode(double latitude, double longitude) {
        // 광역시/특별시 먼저 체크 (1~8)
        Optional<Map.Entry<Long, RegionCoordinates>> cityResult = REGION_BOUNDARIES.entrySet().stream()
                .filter(entry -> entry.getKey() <= 8)  // 광역시/특별시만 필터
                .filter(entry -> entry.getValue().contains(latitude, longitude))
                .findFirst();

        if (cityResult.isPresent()) {
            return cityResult.get().getKey();
        }

        // 광역시가 아닌 경우 도 단위 체크
        return REGION_BOUNDARIES.entrySet().stream()
                .filter(entry -> entry.getKey() > 8)  // 도 단위만 필터
                .filter(entry -> entry.getValue().contains(latitude, longitude))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    private boolean hasUserCard(String memberId, Long cardId) {
        return userCardRepository.existsByMemberIdAndCardId(memberId, cardId);
    }

    private void grantCardToUser(String memberId, Card card) {
        UserCard userCard = new UserCard(memberId, card.getCardId());  // 생성자 사용
        userCardRepository.save(userCard);
    }

    @Transactional(readOnly = true)
    public List<Card> getAllCards() {
        return cardRepository.findAllWithArea();  // 수정된 메서드 사용
    }

    @Transactional(readOnly = true)
    public List<Card> getUserCards(String memberId) {
        List<UserCard> userCards = userCardRepository.findByMemberId(memberId);
        return userCards.stream()
                .map(UserCard::getCard)
                .collect(Collectors.toList());
    }

    // DTO 변환 메서드 추가
    private CardListResponseDto convertToDto(Card card) {
        return CardListResponseDto.builder()
                .cardId(card.getCardId())
                .cardCode(card.getCardCode())
                .cardName(card.getCardName())
                .image1(card.getImage1())
                .areaCode(card.getAreaCode())
                .sidoName(card.getArea() != null ? card.getArea().getSidoName() : null)
                .build();
    }
}