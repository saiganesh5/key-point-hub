package org.ganesh.keypointhub.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "keypoints")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KeyPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int landmarkIndex;

    private double x;
    private double y;
    private double z;
    private double visibility;

    @ManyToOne
    @JoinColumn(name = "pose_id")
    private Pose pose;
}
