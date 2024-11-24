package com.ssafy.db_server.api.attraction.dbsave;

import com.ssafy.db_server.api.attraction.entity.AttractionList;
import com.ssafy.db_server.api.attraction.repository.AttractionListRepository;
import com.ssafy.db_server.api.config.OpenApiSecretInfo;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
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
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        Instant startTime = Instant.now();
        log.info("Starting attraction data update at: {}", startTime);
        List<AttractionList> attractions = new ArrayList<>();

        try {
            fetchAndSaveBasicData(attractions);
            log.info("Successfully completed basic attraction data save");
            updateDetailsAsync();
            log.info("Started async detail update process");
        } catch (Exception e) {
            log.error("Error occurred while saving attraction data: ", e);
        }

        Instant endTime = Instant.now();
        Duration duration = Duration.between(startTime, endTime);
        log.info("Total execution time: {} hours, {} minutes, {} seconds",
                duration.toHours(),
                duration.toMinutesPart(),
                duration.toSecondsPart());
    }

    private void fetchAndSaveBasicData(List<AttractionList> attractions) {
        long idCounter = 1;
        int totalProcessed = 0;
        int totalSaved = 0;
        Instant batchStartTime = Instant.now();

        Map<Integer, Integer> contentTypeCount = new HashMap<>();
        Map<Integer, Integer> savedContentTypeCount = new HashMap<>();
        for (int contentTypeId : CONTENT_TYPE_IDS) {
            contentTypeCount.put(contentTypeId, 0);
            savedContentTypeCount.put(contentTypeId, 0);
        }

        for (int contentTypeId : CONTENT_TYPE_IDS) {
            log.info("Starting to fetch data for contentTypeId: {}", contentTypeId);
            for (int areaCode : AREA_CODES) {
                try {
                    log.info("Fetching data for contentTypeId: {} and areaCode: {}", contentTypeId, areaCode);
                    Instant areaStartTime = Instant.now();

                    String responseDataBody = fetchAttractionData(contentTypeId, areaCode);
                    if (responseDataBody == null || responseDataBody.trim().isEmpty()) {
                        log.warn("Empty response for contentTypeId: {} and areaCode: {}", contentTypeId, areaCode);
                        continue;
                    }

                    List<AttractionList> areaAttractions = parseAttractionData(responseDataBody, idCounter);
                    contentTypeCount.put(contentTypeId, contentTypeCount.get(contentTypeId) + areaAttractions.size());
                    totalProcessed += areaAttractions.size();

                    if (!areaAttractions.isEmpty()) {
                        List<AttractionList> validAttractions = new ArrayList<>();
                        for (AttractionList attraction : areaAttractions) {
                            if (isValidAttraction(attraction)) {
                                validAttractions.add(attraction);
                            }
                        }

                        if (!validAttractions.isEmpty()) {
                            attractions.addAll(validAttractions);
                            idCounter += validAttractions.size();
                            savedContentTypeCount.put(contentTypeId,
                                    savedContentTypeCount.get(contentTypeId) + validAttractions.size());
                            totalSaved += validAttractions.size();

                            if (attractions.size() >= BATCH_SIZE) {
                                saveBatch(attractions);
                            }
                        }
                    } else {
                        log.warn("No attractions found for contentTypeId: {} and areaCode: {}",
                                contentTypeId, areaCode);
                    }

                    Instant areaEndTime = Instant.now();
                    Duration areaDuration = Duration.between(areaStartTime, areaEndTime);
                    log.info("Processed areaCode: {} and contentTypeId: {} in {} seconds, " +
                                    "attractions found: {}, valid attractions: {}",
                            areaCode, contentTypeId, areaDuration.toSeconds(),
                            areaAttractions.size(), attractions.size());

                    Thread.sleep(100);
                } catch (Exception e) {
                    log.error("Error processing contentTypeId: {} and areaCode: {}: {}",
                            contentTypeId, areaCode, e.getMessage());
                    continue;
                }
            }

            log.info("Completed processing contentTypeId: {}. Total found: {}, Total saved: {}",
                    contentTypeId, contentTypeCount.get(contentTypeId),
                    savedContentTypeCount.get(contentTypeId));
        }

        if (!attractions.isEmpty()) {
            saveBatch(attractions);
        }

        log.info("Final statistics:");
        log.info("Total attractions found: {}", totalProcessed);
        log.info("Total attractions saved: {}", totalSaved);
        log.info("Content type statistics:");
        for (int contentTypeId : CONTENT_TYPE_IDS) {
            log.info("ContentTypeId: {}, Found: {}, Saved: {}", contentTypeId,
                    contentTypeCount.get(contentTypeId),
                    savedContentTypeCount.get(contentTypeId));
        }

        Instant batchEndTime = Instant.now();
        Duration totalDuration = Duration.between(batchStartTime, batchEndTime);
        log.info("Total processing time: {} hours, {} minutes, {} seconds",
                totalDuration.toHours(),
                totalDuration.toMinutesPart(),
                totalDuration.toSecondsPart());
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

            log.debug("Requesting URL: {}", url.replaceAll("serviceKey=[^&]+", "serviceKey=***"));

            ResponseEntity<String> responseData = restTemplate.getForEntity(url, String.class);
            log.debug("Response status: {}", responseData.getStatusCode());

            if (responseData.getBody() == null) {
                log.warn("Null response body received for contentTypeId: {} and areaCode: {}",
                        contentTypeId, areaCode);
                return null;
            }

            JSONParser parser = new JSONParser();
            JSONObject jsonResponse = (JSONObject) parser.parse(responseData.getBody());
            JSONObject response = (JSONObject) jsonResponse.get("response");
            JSONObject body = (JSONObject) response.get("body");

            if (body != null) {
                long totalCount = (Long) body.get("totalCount");
                log.info("Total count for contentTypeId: {} and areaCode: {}: {}",
                        contentTypeId, areaCode, totalCount);
            }

            return responseData.getBody();
        } catch (Exception e) {
            log.error("Error fetching attraction data for contentTypeId: {} and areaCode: {}: {}",
                    contentTypeId, areaCode, e.getMessage(), e);
            return null;
        }
    }

    @Transactional
    private void saveBatch(List<AttractionList> attractions) {
        try {
            log.info("Saving batch of {} attractions...", attractions.size());
            List<AttractionList> savedAttractions = attractionListRepository.saveAll(attractions);
            log.info("Successfully saved {} attractions", savedAttractions.size());
            attractions.clear();
        } catch (Exception e) {
            log.error("Error saving batch: {}", e.getMessage());
            if (!attractions.isEmpty()) {
                AttractionList firstItem = attractions.get(0);
                log.error("First item in failed batch - ContentId: {}, ContentTypeId: {}, AreaCode: {}, SiGunGuCode: {}",
                        firstItem.getContentId(), firstItem.getContentTypeId(),
                        firstItem.getAreaCode(), firstItem.getSiGunGuCode());
            }
            attractions.clear();  // Clear the failed batch to continue with next one
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

            // totalCount 확인 및 로깅
            Long totalCount = (Long) body.get("totalCount");
            if (totalCount != null && totalCount == 0) {
                log.info("API returned totalCount of 0");
                return attractions;
            }

            JSONObject items = getJsonObject(body, "items");
            if (items == null) return attractions;

            JSONArray item = getJsonArray(items, "item");
            if (item == null) return attractions;

            log.info("Found {} items in response", item.size());

            for (int i = 0; i < item.size(); i++) {
                try {
                    JSONObject tmp = (JSONObject) item.get(i);
                    AttractionList attraction = createAttractionFromJson(tmp, startId + i);
                    if (isValidAttraction(attraction)) {
                        attractions.add(attraction);
                    } else {
                        log.warn("Invalid attraction data at index {}", i);
                    }
                } catch (Exception e) {
                    log.warn("Error parsing attraction at index {}: {}", i, e.getMessage());
                    continue;
                }
            }
        } catch (Exception e) {
            log.error("Error parsing attraction data: {}", e.getMessage());
        }

        log.info("Successfully parsed {} attractions", attractions.size());
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
        if (attraction == null) {
            log.debug("Attraction is null");
            return false;
        }

        // ContentId 검증
        if (attraction.getContentId() == null) {
            log.debug("ContentId is null for attraction");
            return false;
        }

        // Title 검증
        if (attraction.getTitle() == null || attraction.getTitle().trim().isEmpty()) {
            log.debug("Title is null or empty for contentId: {}", attraction.getContentId());
            return false;
        }

        // AreaCode와 SiGunGuCode 검증
        if (attraction.getAreaCode() == null) {
            log.debug("AreaCode is null for contentId: {}", attraction.getContentId());
            return false;
        }

        if (attraction.getSiGunGuCode() == null) {
            log.debug("SiGunGuCode is null for contentId: {}", attraction.getContentId());
            return false;
        }

        // gugun_list 테이블 참조 검증
        try {
            boolean exists = attractionListRepository.existsByAreaCodeAndSiGunGuCode(
                    attraction.getAreaCode(),
                    attraction.getSiGunGuCode()
            );

            if (!exists) {
                log.debug("No matching gugun_list entry for contentId: {}, areaCode: {}, sigunguCode: {}",
                        attraction.getContentId(), attraction.getAreaCode(), attraction.getSiGunGuCode());
                return false;
            }
        } catch (Exception e) {
            log.error("Error checking gugun_list existence for contentId: {}: {}",
                    attraction.getContentId(), e.getMessage());
            return false;
        }

        return true;
    }

    private AttractionList createAttractionFromJson(JSONObject json, long id) {
        try {
            Long contentId = safeParseLong(json.get("contentid"));
            Long areaCode = safeParseLong(json.get("areacode"));
            Long siGunGuCode = safeParseLong(json.get("sigungucode"));
            String title = (String) json.get("title");

            // 디버깅을 위한 상세 로깅
            if (contentId == null) {
                log.debug("Failed to parse contentid from: {}", json.get("contentid"));
            }
            if (areaCode == null) {
                log.debug("Failed to parse areacode from: {}", json.get("areacode"));
            }
            if (siGunGuCode == null) {
                log.debug("Failed to parse sigungucode from: {}", json.get("sigungucode"));
            }
            if (title == null) {
                log.debug("Title is null for id: {}", id);
            }

            return new AttractionList(
                    id,
                    contentId,
                    title,
                    safeParseLong(json.get("contenttypeid")),
                    areaCode,
                    siGunGuCode,
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
            log.error("Error creating attraction from JSON: {} - Error: {}", json, e.getMessage());
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
        Instant startTime = Instant.now();
        log.info("Starting detail updates for attractions at: {}", startTime);

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

                Instant batchStartTime = Instant.now();
                updateBatchDetails(batch);
                Instant batchEndTime = Instant.now();

                Duration batchDuration = Duration.between(batchStartTime, batchEndTime);
                log.info("Updated details for batch {}/{}, size: {}, time taken: {} seconds",
                        i + BATCH_SIZE, totalCount, batch.size(), batchDuration.toSeconds());

                Thread.sleep(100);
            }

            Instant endTime = Instant.now();
            Duration totalDuration = Duration.between(startTime, endTime);
            log.info("Successfully completed updating details for all attractions. Total time: {} hours, {} minutes, {} seconds",
                    totalDuration.toHours(),
                    totalDuration.toMinutesPart(),
                    totalDuration.toSecondsPart());
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

            // 데이터 길이 로깅 추가
            if (homepage != null) {
                log.debug("Homepage length for contentId {}: {} characters",
                        attraction.getContentId(), homepage.length());
            }
            if (overview != null) {
                log.debug("Overview length for contentId {}: {} characters",
                        attraction.getContentId(), overview.length());
            }

            attraction.setHomepage(homepage);
            attraction.setOverview(overview);
        } catch (Exception e) {
            log.error("Error parsing detail data for contentId {}: {}",
                    attraction.getContentId(), e.getMessage());
        }
    }
}