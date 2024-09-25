package com.example.coconote.api.search.entity;

import com.example.coconote.common.IsDeleted;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Document;

@Data
@Builder
@Document(indexName = "workspace_members")  // ElasticSearch 인덱스 이름
public class WorkspaceMemberDocument {

    @Id
    private String id;

    private Long workspaceMemberId;

    private Long workspaceId;

    private Long memberId;

    private String memberName;

    private String nickname;

    private String email;  // Member 엔티티의 email을 가져와야 함

    private String field;

    private String position;

    private String profileImage;

    private IsDeleted isDeleted;

    private String wsRole;
}