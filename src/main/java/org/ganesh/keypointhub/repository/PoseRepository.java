package org.ganesh.keypointhub.repository;

import org.ganesh.keypointhub.entity.Pose;
import org.springframework.data.jpa.repository.JpaRepository;
public interface PoseRepository extends JpaRepository<Pose, Long> {

}
