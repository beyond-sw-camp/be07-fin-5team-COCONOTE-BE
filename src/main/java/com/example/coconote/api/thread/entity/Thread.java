package com.example.coconote.api.thread.entity;

import com.example.coconote.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Thread extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="thread_id")
    private long id;
    private String content;
    private String file;
    @ManyToOne(fetch = FetchType.LAZY)
    private Thread thread;

}
