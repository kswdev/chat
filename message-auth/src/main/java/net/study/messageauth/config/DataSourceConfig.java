package net.study.messageauth.config;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import net.study.messageauth.database.RoutingDataSource;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

@Slf4j
@Configuration
public class DataSourceConfig {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.source.hikari")
    public DataSource sourceDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.replica.hikari")
    public DataSource replicaDataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean
    public DataSource routingDataSource(
            @Qualifier("sourceDataSource") DataSource sourceDataSource,
            @Qualifier("replicaDataSource") DataSource replicaDataSource
    ) throws SQLException {
        RoutingDataSource routingDataSource = new RoutingDataSource();

        Map<Object, Object> targetDataSource = Map.of("source", sourceDataSource,
                                                      "replica", replicaDataSource);

        routingDataSource.setTargetDataSources(targetDataSource);
        routingDataSource.setDefaultTargetDataSource(sourceDataSource);

        try (Connection connection = replicaDataSource.getConnection()) {
            log.info("init ReplicaConnectionPool.");
        }

        return routingDataSource;
    }

    @Bean
    @Primary
    public DataSource lazyConnectionDataSource(
            @Qualifier("routingDataSource") DataSource dataSource
    ) {
        return new LazyConnectionDataSourceProxy(dataSource);
    }
}
