package com.example.want.common;


import lombok.Getter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

@Getter
@MappedSuperclass
public abstract class BaseEntity {
    @CreationTimestamp //해당 어노테이션을 통해 자동으로 현재시간을 넣어줌
    private LocalDateTime createdTime;
    @UpdateTimestamp
    private LocalDateTime updatedTime;
}// 삭제 필요하면 어노테이션 찾아서 하세여
