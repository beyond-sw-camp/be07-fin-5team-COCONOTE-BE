package com.example.coconote.common;


import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass
public abstract class BaseEntity {
    @CreationTimestamp //해당 어노테이션을 통해 자동으로 현재시간을 넣어줌
    private LocalDateTime createdTime;
    @UpdateTimestamp
    private LocalDateTime updatedTime;

    @Enumerated(EnumType.STRING)
    private IsDeleted isDeleted = IsDeleted.N;
    private LocalDateTime deletedTime;
}// 삭제 필요하면 어노테이션 찾아서 하세여
