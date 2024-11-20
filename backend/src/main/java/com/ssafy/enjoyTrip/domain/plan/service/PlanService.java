package com.ssafy.enjoyTrip.domain.plan.service;

import com.ssafy.enjoyTrip.domain.plan.dto.detail.*;
import com.ssafy.enjoyTrip.domain.plan.dto.plan.PlanCreateRequest;
import com.ssafy.enjoyTrip.domain.plan.dto.plan.PlanResponseDto;
import com.ssafy.enjoyTrip.domain.plan.dto.plan.PlanUpdateRequest;
import com.ssafy.enjoyTrip.domain.plan.entity.Plan;
import com.ssafy.enjoyTrip.domain.plan.entity.PlanDetail;
import com.ssafy.enjoyTrip.domain.plan.repository.PlanDetailRepository;
import com.ssafy.enjoyTrip.domain.plan.repository.PlanRepository;
import com.ssafy.enjoyTrip.global.s3.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PlanService {
    private final PlanRepository planRepository;
    private final PlanDetailRepository planDetailRepository;
    private final S3Service s3Service;

    // 여행 계획 생성(상세 포함)
    public void createPlan(PlanCreateRequest request, MultipartFile image, String memberId) {
        String imageUrl = null;

        try {
            validatePlanCreateRequest(request);

            if (image != null && !image.isEmpty()) {
                imageUrl = s3Service.uploadFile(image);
            }

            Plan plan = Plan.builder()
                    .planTitle(request.getPlanTitle())
                    .areaCode(request.getAreaCode())
                    .planProfileImg(imageUrl)
                    .memberId(memberId)
                    .startDate(request.getStartDate())
                    .endDate(request.getEndDate())
                    .build();
            Plan savedPlan = planRepository.save(plan);

            // 상세 계획 저장 로직을 별도 메서드로 분리
            saveDetailPlans(request.getDayPlans(), savedPlan.getPlanId());

        } catch (Exception e) {
            if (imageUrl != null) {
                try {
                    s3Service.deleteFile(imageUrl);
                } catch (Exception ex) {
                    log.error("이미지 삭제 실패", ex);
                }
            }
            log.error("여행 계획 생성 실패", e);
            throw new RuntimeException("여행 계획 생성 중 오류가 발생했습니다.");
        }
    }

    private void saveDetailPlans(List<DayPlanRequest> dayPlans, Long planId) {
        for (DayPlanRequest dayPlan : dayPlans) {
            List<PlanDetail> dayDetails = dayPlan.getDetails().stream()
                    .map(detail -> PlanDetail.builder()
                            .planId(planId)
                            .attractionId(detail.getAttractionId())
                            .day(dayPlan.getDay())
                            .sequence(detail.getSequence())
                            .memo(detail.getMemo())
                            .build())
                    .collect(Collectors.toList());

            planDetailRepository.saveAll(dayDetails);
            log.info("{}일차 계획 저장 완료: {} 개의 여행지",
                    dayPlan.getDay(), dayDetails.size());
        }
    }

    // 여행 계획 수정
    public void updatePlan(PlanUpdateRequest request, MultipartFile image,  String planId, String memberId) {
        try {
            // 1. Plan 존재 여부 및 권한 확인
            Plan plan = planRepository.findById(Long.parseLong(planId))
                    .orElseThrow(() -> new RuntimeException("계획을 찾을 수 없습니다."));

            if (!plan.getMemberId().equals(memberId)) {
                throw new RuntimeException("수정 권한이 없습니다.");
            }

            // 2. 요청 데이터 검증
            validatePlanUpdateRequest(request);

            // 이미지 처리
            String newImageUrl = null;
            if (image != null && !image.isEmpty()) {
                // 기존 이미지가 있다면 삭제
                if (plan.getPlanProfileImg() != null) {
                    s3Service.deleteFile(plan.getPlanProfileImg());
                }
                // 새 이미지 업로드
                newImageUrl = s3Service.uploadFile(image);
            }

            // 이미지 URL 포함하여 업데이트
            request.setPlanProfileImg(newImageUrl != null ? newImageUrl : plan.getPlanProfileImg());
            // 3. Plan 기본 정보 업데이트
            plan.updatePlan(request);

            // 4. 기존 상세 계획 삭제
            planDetailRepository.deleteByPlanId(Long.parseLong(planId));

            // 5. 새로운 상세 계획 생성
            List<PlanDetail> newDetails = new ArrayList<>();
            for (DayPlanRequest dayPlan : request.getDayPlans()) {
                for (PlanDetailCreateRequest detail : dayPlan.getDetails()) {
                    PlanDetail planDetail = PlanDetail.builder()
                            .planId(Long.parseLong(planId))
                            .attractionId(detail.getAttractionId())
                            .day(dayPlan.getDay())
                            .sequence(detail.getSequence())
                            .memo(detail.getMemo())
                            .build();
                    newDetails.add(planDetail);
                }
            }

            planDetailRepository.saveAll(newDetails);
            log.info("여행 계획 수정 완료 - planId: {}, 총 여행지 수: {}",
                    planId, newDetails.size());
        } catch (Exception e) {
            log.error("여행 계획 수정 실패 - planId: {}", planId, e);
            throw new RuntimeException("여행 계획 수정 중 오류가 발생했습니다.");
        }
    }

    // 여행 계획 삭제
    public void deletePlan(String planId, String memberId) {
        try {
            Plan plan = planRepository.findById(Long.parseLong(planId))
                    .orElseThrow(() -> new RuntimeException("계획을 찾을 수 없습니다."));

            if (!plan.getMemberId().equals(memberId)) {
                throw new RuntimeException("삭제 권한이 없습니다.");
            }

            // 이미지가 있다면 S3에서 삭제
            if (plan.getPlanProfileImg() != null) {
                s3Service.deleteFile(plan.getPlanProfileImg());
            }

            planRepository.delete(plan);
            log.info("여행 계획 삭제 완료 - planId: {}", planId);
        } catch (Exception e) {
            log.error("여행 계획 삭제 실패 - planId: {}", planId, e);
            throw new RuntimeException("여행 계획 삭제 중 오류가 발생했습니다.");
        }
    }

    // 사용자의 모든 여행 계획 조회
    @Transactional(readOnly = true)
    public List<PlanResponseDto> getAllPlans(String memberId) {
        if (memberId == null || memberId.isEmpty()) {
            throw new RuntimeException("유효하지 않은 사용자입니다.");
        }

        try {
            List<Plan> plans = planRepository.findByMemberId(memberId);
            List<PlanResponseDto> responses = plans.stream()
                    .map(PlanResponseDto::fromEntity)
                    .collect(Collectors.toList());

            log.info("사용자의 여행 계획 조회 완료 - memberId: {}, 계획 수: {}",
                    memberId, responses.size());
            return responses;
        } catch (Exception e) {
            log.error("여행 계획 조회 실패 - memberId: {}", memberId, e);
            throw new RuntimeException("여행 계획 조회 중 오류가 발생했습니다.");
        }
    }

    // 특정 여행 계획의 전체 일정 조회
    @Transactional(readOnly = true)
    public PlanDetailsResponseDto getPlanDetails(String planId, String memberId) {
        try {
            Plan plan = planRepository.findById(Long.parseLong(planId))
                    .orElseThrow(() -> new RuntimeException("계획을 찾을 수 없습니다."));

            if (!plan.getMemberId().equals(memberId)) {
                throw new RuntimeException("조회 권한이 없습니다.");
            }

            long totalDays = ChronoUnit.DAYS.between(plan.getStartDate(), plan.getEndDate()) + 1;
            List<PlanDetailDayResponse> dayPlans = new ArrayList<>();

            for (int day = 1; day <= totalDays; day++) {
                LocalDate currentDate = plan.getStartDate().plusDays(day - 1);
                List<PlanDetail> details = planDetailRepository
                        .findByPlanIdAndDayOrderBySequence(plan.getPlanId(), day);

                List<PlanDetailResponseDto> detailResponses = details.stream()
                        .map(PlanDetailResponseDto::fromEntity)
                        .collect(Collectors.toList());

                dayPlans.add(PlanDetailDayResponse.builder()
                        .day(day)
                        .date(currentDate.toString())
                        .details(detailResponses)
                        .build());
            }

            return PlanDetailsResponseDto.fromEntity(plan, dayPlans);
        } catch (Exception e) {
            log.error("여행 계획 상세 조회 실패 - planId: {}", planId, e);
            throw new RuntimeException("여행 계획 상세 조회 중 오류가 발생했습니다.");
        }
    }

    // 특정 여행 계획의 특정 일자 상세 조회
    @Transactional(readOnly = true)
    public List<PlanDetailResponseDto> getPlanDetailsByDay(String planId, Integer day, String memberId) {
        try {
            Plan plan = planRepository.findById(Long.parseLong(planId))
                    .orElseThrow(() -> new RuntimeException("계획을 찾을 수 없습니다."));

            if (!plan.getMemberId().equals(memberId)) {
                throw new RuntimeException("조회 권한이 없습니다.");
            }

            long totalDays = ChronoUnit.DAYS.between(plan.getStartDate(), plan.getEndDate()) + 1;
            if (day < 1 || day > totalDays) {
                throw new IllegalArgumentException("잘못된 여행 일자입니다.");
            }

            List<PlanDetail> details = planDetailRepository
                    .findByPlanIdAndDayOrderBySequence(Long.parseLong(planId), day);

            return details.stream()
                    .map(PlanDetailResponseDto::fromEntity)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("여행 상세 계획 조회 실패 - planId: {}, day: {}", planId, day, e);
            throw new RuntimeException("여행 상세 계획 조회 중 오류가 발생했습니다.");
        }
    }

    // 계획 생성 요청 검증
    private void validatePlanCreateRequest(PlanCreateRequest request) {
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new IllegalArgumentException("종료일이 시작일보다 빠를 수 없습니다.");
        }

        request.getDayPlans().forEach(dayPlan -> {
            int day = dayPlan.getDay();
            if (day < 1 || day > ChronoUnit.DAYS.between(
                    request.getStartDate(), request.getEndDate()) + 1) {
                throw new IllegalArgumentException("잘못된 여행 일자입니다.");
            }

            Set<Integer> sequences = new HashSet<>();
            dayPlan.getDetails().forEach(detail -> {
                if (!sequences.add(detail.getSequence())) {
                    throw new IllegalArgumentException(
                            String.format("%d일차에 중복된 순서가 있습니다.", day)
                    );
                }
            });
        });
    }

    // 계획 수정 요청 검증
    private void validatePlanUpdateRequest(PlanUpdateRequest request) {
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new IllegalArgumentException("종료일이 시작일보다 빠를 수 없습니다.");
        }

        request.getDayPlans().forEach(dayPlan -> {
            int day = dayPlan.getDay();
            if (day < 1 || day > ChronoUnit.DAYS.between(
                    request.getStartDate(), request.getEndDate()) + 1) {
                throw new IllegalArgumentException("잘못된 여행 일자입니다.");
            }

            Set<Integer> sequences = new HashSet<>();
            dayPlan.getDetails().forEach(detail -> {
                if (!sequences.add(detail.getSequence())) {
                    throw new IllegalArgumentException(
                            String.format("%d일차에 중복된 순서가 있습니다.", day)
                    );
                }
            });
        });
    }
}
