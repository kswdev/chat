package net.study.messagesystem.config;

import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import net.study.messagesystem.database.RoutingDataSource;
import net.study.messagesystem.database.ShardContext;
import net.study.messagesystem.domain.channel.ChannelId;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.DatabasePopulator;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

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
    @ConfigurationProperties(prefix = "spring.datasource.source-message1.hikari")
    public DataSource sourceMessage1DataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.replica-message1.hikari")
    public DataSource replicaMessage1DataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.source-message2.hikari")
    public DataSource sourceMessage2DataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.replica-message2.hikari")
    public DataSource replicaMessage2DataSource() {
        return DataSourceBuilder.create().type(HikariDataSource.class).build();
    }

    @Bean
    public DataSource routingDataSource(
            @Qualifier("sourceDataSource") DataSource sourceDataSource,
            @Qualifier("replicaDataSource") DataSource replicaDataSource,
            @Qualifier("sourceMessage1DataSource") DataSource sourceMessage1DataSource,
            @Qualifier("replicaMessage1DataSource") DataSource replicaMessage1DataSource,
            @Qualifier("sourceMessage2DataSource") DataSource sourceMessage2DataSource,
            @Qualifier("replicaMessage2DataSource") DataSource replicaMessage2DataSource
    ) throws SQLException {
        RoutingDataSource routingDataSource = new RoutingDataSource();
        Map<Object, Object> targetDataSource = Map.of(
                "source", sourceDataSource,
                "replica", replicaDataSource,
                "sourceMessage1", sourceMessage1DataSource,
                "replicaMessage1", replicaMessage1DataSource,
                "sourceMessage2", sourceMessage2DataSource,
                "replicaMessage2", replicaMessage2DataSource
        );

        routingDataSource.setTargetDataSources(targetDataSource);

        try (Connection ignored = replicaDataSource.getConnection()) {
            log.info("init ReplicaConnectionPool.");
        }

        try (Connection ignored = replicaMessage1DataSource.getConnection()) {
            log.info("init ReplicaMessage1ConnectionPool.");
        }

        try (Connection ignored = replicaMessage2DataSource.getConnection()) {
            log.info("init ReplicaMessage2ConnectionPool.");
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

    @Bean
    public DataSourceInitializer sourceDataSourceInitializer(@Qualifier("sourceDataSource") DataSource sourceDataSource) {
        return dataSourceInitializer(sourceDataSource, null);
    }

    @Bean
    public DataSourceInitializer sourceMessage1DataSourceInitializer(@Qualifier("sourceMessage1DataSource") DataSource sourceMessage1DataSource) {
        return dataSourceInitializer(sourceMessage1DataSource, new ChannelId(1L));
    }

    @Bean
    public DataSourceInitializer sourceMessage2DataSourceInitializer(@Qualifier("sourceMessage2DataSource") DataSource sourceMessage2DataSource) {
        return dataSourceInitializer(sourceMessage2DataSource, new ChannelId(2L));
    }


    private DataSourceInitializer dataSourceInitializer(DataSource dataSource, ChannelId channelId) {
        DataSourceInitializer dataSourceInitializer = new DataSourceInitializer();
        dataSourceInitializer.setDataSource(dataSource);

        ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator();

        if (channelId == null) {
            databasePopulator.addScript(new ClassPathResource("schema.sql"));
        } else {
            databasePopulator.addScript(new ClassPathResource("message.sql"));
            ShardContext.setChannelId(channelId.id());
        }

        DatabasePopulator wrapper = connection -> {
            try {
                databasePopulator.populate(connection);
            } finally {
                ShardContext.clear();
            }
        };

        dataSourceInitializer.setDatabasePopulator(wrapper);
        return dataSourceInitializer;
    }
}
