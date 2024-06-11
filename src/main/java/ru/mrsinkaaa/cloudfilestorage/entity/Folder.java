package ru.mrsinkaaa.cloudfilestorage.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "folders")
public class Folder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String folderName;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    private String minioObjectId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    @ManyToOne
    @JoinColumn(name = "folder_id")
    private Folder parentFolder;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}


