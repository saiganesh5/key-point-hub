package org.ganesh.keypointhub.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.gridfs.model.GridFSFile;
import net.lingala.zip4j.model.ZipParameters;
import org.bson.types.ObjectId;
import org.ganesh.keypointhub.entity.Pose;
import org.ganesh.keypointhub.repository.PoseRepository;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.scheduling.annotation.Scheduled;
//import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.util.List;

import net.lingala.zip4j.ZipFile;

@Service
public class DataZipScheduler {
    private final PoseRepository poseRepository;
    private final GridFsTemplate gridFsTemplate;
    private  final EmailService emailService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    public DataZipScheduler(PoseRepository poseRepository, GridFsTemplate gridFsTemplate, EmailService emailService) {
        this.poseRepository = poseRepository;
        this.gridFsTemplate = gridFsTemplate;
        this.emailService = emailService;
    }
    
    /* Runs every day at 2 AM*/
    @Scheduled(cron = "0 0 2 * * *")
    public void zipDailyData() throws Exception{
        String date  = LocalDate.now().toString();
        String zipName = "daily-export-"+date+".zip";
        
        File zipFile = new File("exports/" + zipName);
        zipFile.getParentFile().mkdirs();
        
        ZipFile zip = new ZipFile(zipFile);
        ZipParameters defaultParams = new ZipParameters();

        List<Pose> poses = poseRepository.findAll();

        /*-----------Add poses.json-----------*/

        File posesJson = File.createTempFile("poses-", ".json");
        objectMapper.writeValue(posesJson, poses);
        zip.addFile(posesJson, defaultParams);


        /*---------Add images------------*/
        for (Pose pose : poses){
            GridFSFile file = gridFsTemplate.findOne(
                    Query.query(Criteria.where("_id")
                            .is(new ObjectId(pose.getImageFileId())))
            );
            
            if(file==null) continue;
            
            
            GridFsResource resource = gridFsTemplate.getResource(file);
            
            File imageFile = File.createTempFile(
                    "pose-" + pose.getId(), ".img"
            );
            
            try(FileOutputStream fos = new FileOutputStream(imageFile)){
                resource.getInputStream().transferTo(fos);
            }

            ZipParameters params = new ZipParameters();
            params.setFileNameInZip("images/pose_"+pose.getId()+".jpg");
            
            zip.addFile(imageFile, params);
            imageFile.delete();
        }
        System.out.println("Daily ZIP created: "+zipFile.getAbsolutePath());
    }





}
