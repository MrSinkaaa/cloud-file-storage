package ru.mrsinkaaa.cloudfilestorage.entity;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String username;
    private String password;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "owner", fetch = FetchType.LAZY)
    private List<File> files;

    @OneToMany(mappedBy = "owner", fetch = FetchType.LAZY)
    private List<Folder> folders;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

}
