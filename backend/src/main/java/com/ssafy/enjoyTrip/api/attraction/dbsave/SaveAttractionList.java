package com.ssafy.enjoyTrip.api.attraction.dbsave;

import com.ssafy.enjoyTrip.api.attraction.entity.AttractionList;
import com.ssafy.enjoyTrip.api.attraction.repository.AttractionListRepository;
import com.ssafy.enjoyTrip.api.config.OpenApiSecretInfo;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SaveAttractionList {
    private final OpenApiSecretInfo openApiSecretInfo;
    private final AttractionListRepository attractionListRepository;
    private final RestTemplate restTemplate;

    private static final String ATTRACTION_API_URL = "https://apis.data.go.kr/B551011/KorService1";
    private static final int[] AREA_CODES = {1, 2, 3, 4, 5, 6, 7, 8, 31, 32, 33, 34, 35, 36, 37, 38, 39};
    private static final int[] CONTENT_TYPE_IDS = {12, 14, 15, 25, 28, 32, 38, 39};

    @PostConstruct
    @Scheduled(cron = "0 0 12 * * *", zone = "Asia/Seoul")
    public void saveAttractionList() {
        log.info("Starting attraction data update...");
        List<AttractionList> attractions = new ArrayList<>();
        long idCounter = 1;

        try {
            for (int contentTypeId : CONTENT_TYPE_IDS) {
                for (int areaCode : AREA_CODES) {
                    log.info("Fetching data for contentTypeId: {} and areaCode: {}", contentTypeId, areaCode);

                    String responseDataBody = fetchAttractionData(contentTypeId, areaCode);
                    List<AttractionList> areaAttractions = parseAttractionData(responseDataBody, idCounter);

                    // 일단 기본 데이터만 저장
                    attractions.addAll(areaAttractions);
                    idCounter += areaAttractions.size();

                    // 너무 많은 데이터가 쌓이는 것을 방지하기 위해 중간 저장
                    if (attractions.size() > 1000) {
                        log.info("Batch saving {} attractions...", attractions.size());
                        attractionListRepository.saveAll(attractions);
                        log.info("Successfully saved batch of attractions");
                        attractions.clear();
                    }

                    Thread.sleep(100);
                }
            }

            // 남은 데이터 저장
            if (!attractions.isEmpty()) {
                log.info("Saving remaining {} attractions...", attractions.size());
                attractionListRepository.saveAll(attractions);
                log.info("Successfully saved remaining attractions");
            }

            // 상세 정보는 비동기로 처리
            log.info("Starting async update of attraction details...");
            updateDetailsAsync();

            log.info("Successfully completed basic attraction data save");

        } catch (Exception e) {
            log.error("Error occurred while saving attraction data: ", e);
        }
    }

    private String fetchAttractionData(int contentTypeId, int areaCode) {
        String baseUrl = ATTRACTION_API_URL + "/areaBasedList1";
        String serviceKey = openApiSecretInfo.getServiceKey();

        String url = baseUrl +
                "?serviceKey=" + serviceKey +
                "&numOfRows=" + 10000 +
                "&pageNo=" + 1 +
                "&MobileOS=ETC" +
                "&MobileApp=AppTest" +
                "&_type=json" +
                "&listYN=Y" +
                "&arrange=A" +
                "&contentTypeId=" + contentTypeId +
                "&areaCode=" + areaCode;

        ResponseEntity<String> responseData = restTemplate.getForEntity(url, String.class);
        return responseData.getBody();
    }

    @Async
    public void updateDetailsAsync() {
        try {
            log.info("Starting detail updates for attractions...");
            List<AttractionList> allAttractions = attractionListRepository.findAll();
            int totalCount = allAttractions.size();
            int currentCount = 0;

            for (AttractionList attraction : allAttractions) {
                try {
                    String detailDataBody = fetchCommonData(attraction.getContentId(), attraction.getContentTypeId());
                    updateAttractionWithDetail(attraction, detailDataBody);
                    attractionListRepository.save(attraction);

                    currentCount++;
                    if (currentCount % 100 == 0) {
                        log.info("Updated details for {}/{} attractions", currentCount, totalCount);
                    }

                    Thread.sleep(50);
                } catch (Exception e) {
                    log.error("Error updating details for attraction {}: {}",
                            attraction.getContentId(), e.getMessage());
                }
            }

            log.info("Successfully completed updating details for all attractions");
        } catch (Exception e) {
            log.error("Error in updateDetailsAsync: ", e);
        }
    }

    private String fetchCommonData(Long contentId, Long contentTypeId) {
        String baseUrl = ATTRACTION_API_URL + "/detailCommon1";
        String serviceKey = openApiSecretInfo.getServiceKey();

        String url = baseUrl +
                "?serviceKey=" + serviceKey +
                "&contentId=" + contentId +
                "&contentTypeId=" + contentTypeId +
                "&MobileOS=ETC" +
                "&MobileApp=AppTest" +
                "&_type=json" +
                "&defaultYN=Y" +
                "&firstImageYN=N" +
                "&areacodeYN=N" +
                "&catcodeYN=N" +
                "&addrinfoYN=N" +
                "&mapinfoYN=N" +
                "&overviewYN=Y";

        ResponseEntity<String> responseData = restTemplate.getForEntity(url, String.class);
        return responseData.getBody();
    }

    private void updateAttractionWithDetail(AttractionList attraction, String detailDataBody) {
        try {
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(detailDataBody);
            JSONObject response = (JSONObject) jsonObject.get("response");
            JSONObject body = (JSONObject) response.get("body");
            JSONObject items = (JSONObject) body.get("items");

            if (items != null) {
                JSONArray itemArray = (JSONArray) items.get("item");
                if (itemArray != null && !itemArray.isEmpty()) {
                    JSONObject detailItem = (JSONObject) itemArray.get(0);
                    String homepage = (String) detailItem.get("homepage");
                    String overview = (String) detailItem.get("overview");

                    attraction.setHomepage(homepage);
                    attraction.setOverview(overview);
                }
            }
        } catch (Exception e) {
            log.error("Error parsing detail data for contentId {}: {}", attraction.getContentId(), e.getMessage());
        }
    }

    private List<AttractionList> parseAttractionData(String responseDataBody, long startId) throws Exception {
        List<AttractionList> attractions = new ArrayList<>();
        JSONParser jsonParser = new JSONParser();
        JSONObject jsonObject = (JSONObject) jsonParser.parse(responseDataBody);
        JSONObject response = (JSONObject) jsonObject.get("response");
        JSONObject body = (JSONObject) response.get("body");
        JSONObject items = (JSONObject) body.get("items");

        if (items == null) {
            return attractions;
        }

        JSONArray item = (JSONArray) items.get("item");
        if (item == null) {
            return attractions;
        }

        for (int i = 0; i < item.size(); i++) {
            JSONObject tmp = (JSONObject) item.get(i);
            AttractionList attraction = createAttractionFromJson(tmp, startId + i);
            attractions.add(attraction);
        }

        return attractions;
    }

    private AttractionList createAttractionFromJson(JSONObject json, long id) {
        return new AttractionList(
                id,
                safeParseLong(json.get("contentid")),
                (String) json.get("title"),
                safeParseLong(json.get("contenttypeid")),
                safeParseLong(json.get("areacode")),
                safeParseLong(json.get("sigungucode")),
                (String) json.get("firstimage"),
                (String) json.get("firstimage2"),
                safeParseInteger(json.get("mlevel")),
                safeParseBigDecimal(json.get("mapy")),
                safeParseBigDecimal(json.get("mapx")),
                (String) json.get("tel"),
                (String) json.get("addr1"),
                (String) json.get("addr2")
        );
    }

    // safe parsing methods remain the same...
    private Long safeParseLong(Object value) {
        if (value == null) return null;
        if (value instanceof Long) return (Long) value;
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private Integer safeParseInteger(Object value) {
        if (value == null) return null;
        if (value instanceof Integer) return (Integer) value;
        if (value instanceof String) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    private BigDecimal safeParseBigDecimal(Object value) {
        if (value == null) return null;
        if (value instanceof BigDecimal) return (BigDecimal) value;
        if (value instanceof String) {
            try {
                return new BigDecimal((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    @Scheduled(cron = "0 59 11 * * *", zone = "Asia/Seoul")
    public void deleteAttractionList() {
        log.info("Deleting all attraction data...");
        attractionListRepository.deleteAll();
        log.info("Successfully deleted all attraction data");
    }
}