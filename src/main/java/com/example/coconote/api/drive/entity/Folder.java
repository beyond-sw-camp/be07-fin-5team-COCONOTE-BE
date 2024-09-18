package com.example.coconote.api.drive.entity;

import com.example.coconote.api.channel.entity.Channel;
import com.example.coconote.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Folder extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String folderName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_folder_id")
    private Folder parentFolder;

    @OneToMany(mappedBy = "parentFolder")
    private List<Folder> childFolders;

    @ManyToOne(fetch = FetchType.LAZY)
    private Channel channel;


    public void changeFolderName(String folderName) {
        this.folderName = folderName;
    }

    public void moveParentFolder(Folder parentFolder) {
        this.parentFolder = parentFolder;
    }
}
