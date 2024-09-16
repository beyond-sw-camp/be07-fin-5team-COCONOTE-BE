package com.example.coconote.api.channel.entity;

import com.example.coconote.api.drive.entity.Folder;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class Channel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    // 폴더들과의 관계 (일대다 관계)
    @OneToMany(mappedBy = "channel", cascade = CascadeType.ALL)
    private List<Folder> folders;
}
