package org.ganesh.keypointhub.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.convert.MongoConverter;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

@Configuration
public class MongoGridFsConfig {
    @Bean
    public GridFsTemplate gridFsTemplate(
            MongoDatabaseFactory dbFactory,
            MongoConverter converter
    ) {
        return new GridFsTemplate(dbFactory, converter);
    }
}
