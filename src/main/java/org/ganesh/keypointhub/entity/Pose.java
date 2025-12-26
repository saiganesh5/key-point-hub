package org.ganesh.keypointhub.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name="poses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Pose {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "pose",cascade = CascadeType.ALL,orphanRemoval = true)
    private List<KeyPoint> keypoints = new ArrayList<>();
    @Column(name = "image_file_id")
    private String imageFileId;

}
