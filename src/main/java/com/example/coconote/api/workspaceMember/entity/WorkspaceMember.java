package com.example.coconote.api.workspaceMember.entity;

import com.example.coconote.common.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Getter
@Builder
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class WorkspaceMember extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String nickname;

    private String field;

    private String position;

    private String profileImage;

    private Enum wsRole;


}
