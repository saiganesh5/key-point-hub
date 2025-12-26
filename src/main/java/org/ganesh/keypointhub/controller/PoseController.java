//package org.ganesh.keypointhub.controller;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.mongodb.client.gridfs.model.GridFSFile;
//import org.bson.types.ObjectId;
//import org.ganesh.keypointhub.entity.Pose;
//import org.ganesh.keypointhub.entity.KeyPoint;
//import org.ganesh.keypointhub.repository.PoseRepository;
//import org.springframework.core.io.InputStreamResource;
//import org.springframework.core.io.Resource;
//import org.springframework.data.mongodb.core.query.Criteria;
//import org.springframework.data.mongodb.core.query.Query;
//import org.springframework.data.mongodb.gridfs.GridFsResource;
//import org.springframework.data.mongodb.gridfs.GridFsTemplate;
//import org.springframework.http.*;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.util.LinkedMultiValueMap;
//import org.springframework.util.MultiValueMap;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.client.RestTemplate;
//import org.springframework.web.multipart.MultipartFile;
//import org.springframework.web.server.ResponseStatusException;
//
//import java.time.LocalDateTime;
//import java.util.*;
//
//@RestController
//@RequestMapping("/api/poses")
//public class PoseController {
//
//    private final GridFsTemplate gridFsTemplate;
//    private final PoseRepository poseRepository;
//    private final RestTemplate restTemplate = new RestTemplate();
//    private final ObjectMapper objectMapper = new ObjectMapper();
//
//    public PoseController(GridFsTemplate gridFsTemplate, PoseRepository poseRepository) {
//        this.gridFsTemplate = gridFsTemplate;
//        this.poseRepository = poseRepository;
//    }
//
//    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    @Transactional
//    public ResponseEntity<String> uploadAndExtract(
//            @RequestPart("file") MultipartFile file
//    ) {
//        try {
//            /* ---------- Forward file to MediaPipe ---------- */
//
//            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
//
//            HttpHeaders fileHeaders = new HttpHeaders();
//            fileHeaders.setContentType(
//                    MediaType.parseMediaType(Objects.requireNonNull(file.getContentType()))
//            );
//            fileHeaders.setContentDispositionFormData(
//                    "file",
//                    file.getOriginalFilename()
//            );
//
//            HttpEntity<Resource> fileEntity = new HttpEntity<>(
//                    new InputStreamResource(file.getInputStream()),
//                    fileHeaders
//            );
//
//            body.add("file", fileEntity);
//
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
//
//            HttpEntity<MultiValueMap<String, Object>> requestEntity =
//                    new HttpEntity<>(body, headers);
//
//            String MEDIAPIPE_URL = "http://127.0.0.1:8000/extract-pose";
//
//            ResponseEntity<String> response =
//                    restTemplate.postForEntity(MEDIAPIPE_URL, requestEntity, String.class);
//
//            /* ---------- Persist keypoints in MySQL ---------- */
//
//            JsonNode root = objectMapper.readTree(response.getBody());
//            JsonNode keypointsNode = root.get("keypoints");
//
//            Pose pose = new Pose();
//            pose.setCreatedAt(LocalDateTime.now());
//
//            List<KeyPoint> keypoints = new ArrayList<>();
//
//            for (JsonNode kp : keypointsNode) {
//                KeyPoint keypoint = new KeyPoint();
//                keypoint.setLandmarkIndex(kp.get("index").asInt());
//                keypoint.setX(kp.get("x").asDouble());
//                keypoint.setY(kp.get("y").asDouble());
//                keypoint.setZ(kp.get("z").asDouble());
//                keypoint.setVisibility(kp.get("visibility").asDouble());
//                keypoint.setPose(pose);
//                keypoints.add(keypoint);
//            }
//
//            pose.setKeypoints(keypoints);
//
//            /* ---------- Store image in MongoDB (GridFS) ---------- */
//
//            ObjectId fileId = gridFsTemplate.store(
//                    file.getInputStream(),
//                    file.getOriginalFilename(),
//                    file.getContentType()
//            );
//
//            pose.setImageFileId(fileId.toHexString());
//
//            poseRepository.save(pose);
//
//            return ResponseEntity
//                    .status(response.getStatusCode())
//                    .body(response.getBody());
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return ResponseEntity
//                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("{\"error\":\"" + e.getMessage() + "\"}");
//        }
//    }
//    @GetMapping("/{id}")
//    public ResponseEntity<Pose> getPose(@PathVariable Long id) {
//        return poseRepository.findById(id)
//                .map(ResponseEntity::ok)
//                .orElse(ResponseEntity.notFound().build());
//    }
//
//    @GetMapping("/api/poses")
//    public List<Pose> getAllPoses() {
//        return poseRepository.findAll();
//    }
//    @GetMapping("/{id}/image")
//    public ResponseEntity<Resource> getPoseImage(@PathVariable Long id) {
//        Pose pose = poseRepository.findById(id)
//                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
//
//        GridFSFile file = gridFsTemplate.findOne(
//                Query.query(Criteria.where("_id").is(new ObjectId(pose.getImageFileId())))
//        );
//
//        GridFsResource resource = gridFsTemplate.getResource(file);
//
//        return ResponseEntity.ok()
//                .contentType(MediaType.parseMediaType(resource.getContentType()))
//                .body(resource);
//    }
//    @PutMapping("/{id}")
//    public ResponseEntity<Pose> updatePose(
//            @PathVariable Long id,
//            @RequestBody Pose updatedPose
//    ) {
//        return poseRepository.findById(id).map(pose -> {
//            pose.setKeypoints(updatedPose.getKeypoints());
//            return ResponseEntity.ok(poseRepository.save(pose));
//        }).orElse(ResponseEntity.notFound().build());
//    }
//
//
//
//}

package org.ganesh.keypointhub.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.bson.types.ObjectId;
import org.ganesh.keypointhub.entity.KeyPoint;
import org.ganesh.keypointhub.entity.Pose;
import org.ganesh.keypointhub.repository.PoseRepository;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/poses")
public class PoseController {

    private final GridFsTemplate gridFsTemplate;
    private final PoseRepository poseRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String mediapipeServiceUrl;

    public PoseController(GridFsTemplate gridFsTemplate, PoseRepository poseRepository, @org.springframework.beans.factory.annotation.Value("${mediapipe.service.url}") String mediapipeServiceUrl) {
        this.gridFsTemplate = gridFsTemplate;
        this.poseRepository = poseRepository;
        this.mediapipeServiceUrl = mediapipeServiceUrl;
    }

    /* ===================== CREATE ===================== */

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Transactional
    public ResponseEntity<String> uploadAndExtract(
            @RequestPart("file") MultipartFile file
    ) {
        try {
            /* ---------- Forward file to MediaPipe ---------- */

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

            HttpHeaders fileHeaders = new HttpHeaders();
            fileHeaders.setContentType(
                    MediaType.parseMediaType(Objects.requireNonNull(file.getContentType()))
            );
            fileHeaders.setContentDispositionFormData(
                    "file",
                    file.getOriginalFilename()
            );

            HttpEntity<Resource> fileEntity = new HttpEntity<>(
                    new InputStreamResource(file.getInputStream()),
                    fileHeaders
            );

            body.add("file", fileEntity);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            HttpEntity<MultiValueMap<String, Object>> requestEntity =
                    new HttpEntity<>(body, headers);

//            String MEDIAPIPE_URL = "http://127.0.0.1:8000/extract-pose";
            // String MEDIAPIPE_URL = "http://host.docker.internal:8000/extract-pose";


            ResponseEntity<String> response =
                    restTemplate.postForEntity(mediapipeServiceUrl, requestEntity, String.class);

            /* ---------- Persist keypoints in MySQL ---------- */

            JsonNode root = objectMapper.readTree(response.getBody());
            JsonNode keypointsNode = root.get("keypoints");

            Pose pose = new Pose();
            pose.setCreatedAt(LocalDateTime.now());

            List<KeyPoint> keypoints = new ArrayList<>();

            for (JsonNode kp : keypointsNode) {
                KeyPoint keypoint = new KeyPoint();
                keypoint.setLandmarkIndex(kp.get("index").asInt());
                keypoint.setX(kp.get("x").asDouble());
                keypoint.setY(kp.get("y").asDouble());
                keypoint.setZ(kp.get("z").asDouble());
                keypoint.setVisibility(kp.get("visibility").asDouble());
                keypoint.setPose(pose);
                keypoints.add(keypoint);
            }

            pose.setKeypoints(keypoints);

            /* ---------- Store image in MongoDB (GridFS) ---------- */

            ObjectId fileId = gridFsTemplate.store(
                    file.getInputStream(),
                    file.getOriginalFilename(),
                    file.getContentType()
            );

            pose.setImageFileId(fileId.toHexString());

            poseRepository.save(pose);

            return ResponseEntity
                    .status(response.getStatusCode())
                    .body(response.getBody());

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\":\"" + e.getMessage() + "\"}");
        }
    }

    /* ===================== READ ===================== */

    @GetMapping("/{id}")
    public ResponseEntity<Pose> getPose(@PathVariable Long id) {
        return poseRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public List<Pose> getAllPoses() {
        return poseRepository.findAll();
    }

    @GetMapping("/{id}/image")
    public ResponseEntity<Resource> getPoseImage(@PathVariable Long id) {

        Pose pose = poseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        GridFSFile file = gridFsTemplate.findOne(
                Query.query(Criteria.where("_id").is(new ObjectId(pose.getImageFileId())))
        );

        if (file == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Image not found");
        }

        GridFsResource resource = gridFsTemplate.getResource(file);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(resource.getContentType()))
                .body(resource);
    }

    /* ===================== UPDATE ===================== */

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<Pose> updatePose(
            @PathVariable Long id,
            @RequestBody Pose updatedPose
    ) {
        return poseRepository.findById(id).map(pose -> {

            for (KeyPoint kp : updatedPose.getKeypoints()) {
                kp.setPose(pose);
            }

            pose.setKeypoints(updatedPose.getKeypoints());
            return ResponseEntity.ok(poseRepository.save(pose));

        }).orElse(ResponseEntity.notFound().build());
    }

    /* ===================== DELETE ===================== */

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> deletePose(@PathVariable Long id) {

        Pose pose = poseRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        gridFsTemplate.delete(
                Query.query(Criteria.where("_id").is(new ObjectId(pose.getImageFileId())))
        );

        poseRepository.delete(pose);

        return ResponseEntity.noContent().build();
    }
}

