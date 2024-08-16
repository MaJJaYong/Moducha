package com.example.restea.user.service;

import com.example.restea.common.dto.PaginationDTO;
import com.example.restea.common.dto.ResponseDTO;
import com.example.restea.record.dto.RecordListResponse;
import com.example.restea.record.entity.Record;
import com.example.restea.record.repository.RecordRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class UserMyPageRecordService {

    private final RecordRepository recordRepository;

    /**
     * 내가 작성한 기록 List를 최신순으로 불러오는 메소드
     *
     * @param userId  유저Id
     * @param page    요청받은 페이지 Number
     * @param perPage 페이지 컨텐츠 개수
     * @return RecordListResponse의 List를 Data로 가지는 ResponseDTO
     */
    public ResponseDTO<List<RecordListResponse>> getRecordList(Integer userId, Integer page, Integer perPage) {
        Page<Record> records = fetchActiveRecords(userId, page, perPage);
        List<RecordListResponse> data = createResponseFormRecords(records.getContent());
        Long count = calculateCount(userId);

        PaginationDTO pagination = PaginationDTO.of(count.intValue(), page, perPage);

        return ResponseDTO.of(data, pagination);
    }

    /**
     * 최신순 내림차순으로 기록 조회
     *
     * @param page    페이지
     * @param perPage 페이지 당 컨텐츠 개수
     * @return 컨텐츠 Page 객체
     */
    private @NotNull Page<Record> fetchActiveRecords(Integer userId, Integer page, Integer perPage) {

        PageRequest pageRequest = PageRequest.of(page - 1, perPage, Sort.by("createdDate").descending());
        Page<Record> records = recordRepository.findAllByUserId(userId, pageRequest);

        if (records.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NO_CONTENT);
        }

        return records;
    }

    /**
     * 기록 Page 객체 생성
     *
     * @param records 기록
     * @return 기록 Response List
     */
    private List<RecordListResponse> createResponseFormRecords(List<Record> records) {
        return records.stream()
                .map(RecordListResponse::of)
                .toList();
    }

    /**
     * @param userId userId
     * @return userId에 해당하는 총 기록 개수
     */
    private Long calculateCount(Integer userId) {
        return recordRepository.countByUserId(userId);
    }
}
