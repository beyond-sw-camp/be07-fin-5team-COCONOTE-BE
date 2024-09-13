package com.example.coconote.api.member.repository;

import com.example.coconote.api.member.entity.Member;
import com.example.coconote.api.tag.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
}
