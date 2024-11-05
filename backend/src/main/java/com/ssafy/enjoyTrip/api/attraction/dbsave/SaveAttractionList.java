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

import java.math.BigDecimal;
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
    private static final int[] AREA_CODES = {1, 2, 3, 4, 5, 6, 7, 8, 31, 32, 33, 34, 35, 36, 37, 38, 39}; // 시/도 코드
    private static final int[] CONTENT_TYPE_IDS = {12, 14, 15, 25, 28, 32, 38, 39}; // 카테고리
    private static final int BATCH_SIZE = 500;

    @PostConstruct
    @Scheduled(cron = "0 0 12 * * *", zone = "Asia/Seoul")
    public void saveAttractionList() {
        log.info("Starting attraction data update...");
        List<AttractionList> attractions = new ArrayList<>();

        try {
            fetchAndSaveBasicData(attractions);
            log.info("Successfully completed basic attraction data save");
        } catch (Exception e) {
            log.error("Error occurred while saving attraction data: ", e);
        }
    }

    private void fetchAndSaveBasicData(List<AttractionList> attractions) {
        long idCounter = 1;

        for (int contentTypeId : CONTENT_TYPE_IDS) {
            for (int areaCode : AREA_CODES) {
                try {
                    log.info("Fetching data for contentTypeId: {} and areaCode: {}", contentTypeId, areaCode);

                    String responseDataBody = fetchAttractionData(contentTypeId, areaCode);
                    if (responseDataBody == null || responseDataBody.trim().isEmpty()) {
                        log.warn("Empty response for contentTypeId: {} and areaCode: {}", contentTypeId, areaCode);
                        continue;
                    }

                    List<AttractionList> areaAttractions = parseAttractionData(responseDataBody, idCounter);

                    if (!areaAttractions.isEmpty()) {
                        attractions.addAll(areaAttractions);
                        idCounter += areaAttractions.size();

                        if (attractions.size() >= BATCH_SIZE) {
                            saveBatch(attractions);
                        }
                    }

                    Thread.sleep(100);
                } catch (Exception e) {
                    log.error("Error processing contentTypeId: {} and areaCode: {}: {}",
                            contentTypeId, areaCode, e.getMessage());
                    continue;
                }
            }
        }

        // 남은 데이터 처리
        if (!attractions.isEmpty()) {
            saveBatch(attractions);
        }
    }

    private String fetchAttractionData(int contentTypeId, int areaCode) {
        try {
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
        } catch (Exception e) {
            log.error("Error fetching attraction data: {}", e.getMessage());
            return null;
        }
    }

    private void saveBatch(List<AttractionList> attractions) {
        try {
            log.info("Saving batch of {} attractions...", attractions.size());
            attractionListRepository.saveAll(attractions);
            log.info("Successfully saved batch of attractions");
            attractions.clear();
        } catch (Exception e) {
            log.error("Error saving batch: {}", e.getMessage());
        }
    }

    private List<AttractionList> parseAttractionData(String responseDataBody, long startId) {
        List<AttractionList> attractions = new ArrayList<>();

        if (responseDataBody == null || responseDataBody.trim().isEmpty()) {
            return attractions;
        }

        try {
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(responseDataBody);

            JSONObject response = getJsonObject(jsonObject, "response");
            if (response == null) return attractions;

            JSONObject body = getJsonObject(response, "body");
            if (body == null) return attractions;

            JSONObject items = getJsonObject(body, "items");
            if (items == null) return attractions;

            JSONArray item = getJsonArray(items, "item");
            if (item == null) return attractions;

            for (int i = 0; i < item.size(); i++) {
                try {
                    JSONObject tmp = (JSONObject) item.get(i);
                    AttractionList attraction = createAttractionFromJson(tmp, startId + i);
                    if (isValidAttraction(attraction)) {
                        attractions.add(attraction);
                    }
                } catch (Exception e) {
                    log.warn("Error parsing attraction at index {}: {}", i, e.getMessage());
                    continue;
                }
            }
        } catch (Exception e) {
            log.error("Error parsing attraction data: {}", e.getMessage());
        }

        return attractions;
    }

    private JSONObject getJsonObject(JSONObject json, String key) {
        try {
            Object obj = json.get(key);
            return obj instanceof JSONObject ? (JSONObject) obj : null;
        } catch (Exception e) {
            log.warn("Error getting JSON object for key {}: {}", key, e.getMessage());
            return null;
        }
    }

    private JSONArray getJsonArray(JSONObject json, String key) {
        try {
            Object obj = json.get(key);
            return obj instanceof JSONArray ? (JSONArray) obj : null;
        } catch (Exception e) {
            log.warn("Error getting JSON array for key {}: {}", key, e.getMessage());
            return null;
        }
    }

    private boolean isValidAttraction(AttractionList attraction) {
        return attraction != null
                && attraction.getContentId() != null
                && attraction.getTitle() != null
                && !attraction.getTitle().trim().isEmpty();
    }

    private AttractionList createAttractionFromJson(JSONObject json, long id) {
        try {
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
        } catch (Exception e) {
            log.error("Error creating attraction from JSON: {}", e.getMessage());
            return null;
        }
    }

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
        try {
            log.info("Deleting all attraction data...");
            attractionListRepository.deleteAll();
            log.info("Successfully deleted all attraction data");
        } catch (Exception e) {
            log.error("Error deleting attraction data: {}", e.getMessage());
        }
    }

    @Async
    public void updateDetailsAsync() {
        try {
            log.info("Starting detail updates for attractions...");
            List<AttractionList> attractionsNeedingUpdate = attractionListRepository.findByOverviewIsNullOrHomepageIsNull();
            int totalCount = attractionsNeedingUpdate.size();

            if (totalCount == 0) {
                log.info("No attractions need detail updates");
                return;
            }

            for (int i = 0; i < attractionsNeedingUpdate.size(); i += BATCH_SIZE) {
                int end = Math.min(i + BATCH_SIZE, attractionsNeedingUpdate.size());
                List<AttractionList> batch = attractionsNeedingUpdate.subList(i, end);

                updateBatchDetails(batch);

                log.info("Updated details for batch {}/{}, size: {}",
                        i + BATCH_SIZE, totalCount, batch.size());

                Thread.sleep(100);
            }

            log.info("Successfully completed updating details for all attractions");
        } catch (Exception e) {
            log.error("Error in updateDetailsAsync: {}", e.getMessage());
        }
    }

    private void updateBatchDetails(List<AttractionList> attractions) {
        List<AttractionList> updatedAttractions = new ArrayList<>();

        for (AttractionList attraction : attractions) {
            try {
                String detailDataBody = fetchCommonData(attraction.getContentId(), attraction.getContentTypeId());
                if (detailDataBody != null && !detailDataBody.trim().isEmpty()) {
                    updateAttractionWithDetail(attraction, detailDataBody);
                    updatedAttractions.add(attraction);
                }
                Thread.sleep(50);
            } catch (Exception e) {
                log.warn("Skipping attraction {}: {}", attraction.getContentId(), e.getMessage());
            }
        }

        if (!updatedAttractions.isEmpty()) {
            try {
                attractionListRepository.saveAll(updatedAttractions);
            } catch (Exception e) {
                log.error("Error saving updated attractions: {}", e.getMessage());
            }
        }
    }

    private String fetchCommonData(Long contentId, Long contentTypeId) {
        try {
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
        } catch (Exception e) {
            log.error("Error fetching common data: {}", e.getMessage());
            return null;
        }
    }

    private void updateAttractionWithDetail(AttractionList attraction, String detailDataBody) {
        try {
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(detailDataBody);

            JSONObject response = getJsonObject(jsonObject, "response");
            if (response == null) return;

            JSONObject body = getJsonObject(response, "body");
            if (body == null) return;

            JSONObject items = getJsonObject(body, "items");
            if (items == null) return;

            JSONArray itemArray = getJsonArray(items, "item");
            if (itemArray == null || itemArray.isEmpty()) return;

            JSONObject detailItem = (JSONObject) itemArray.get(0);
            String homepage = (String) detailItem.get("homepage");
            String overview = (String) detailItem.get("overview");

            attraction.setHomepage(homepage);
            attraction.setOverview(overview);
        } catch (Exception e) {
            log.error("Error parsing detail data for contentId {}: {}",
                    attraction.getContentId(), e.getMessage());
        }
    }
}