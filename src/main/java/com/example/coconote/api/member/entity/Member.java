package com.example.coconote.api.member.entity;

import com.example.coconote.common.BaseEntity;
import jakarta.persistence.*;

import lombok.*;
import org.hibernate.validator.constraints.UniqueElements;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="member_id")
    private long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String nickname;
}