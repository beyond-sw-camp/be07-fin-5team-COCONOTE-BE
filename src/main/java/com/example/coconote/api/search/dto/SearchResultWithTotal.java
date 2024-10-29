package com.example.coconote.api.search.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SearchResultWithTotal<T> {
    private List<T> results;  // 실제 검색 결과 리스트
    private long total;       // 총 검색 결과 개수
}
