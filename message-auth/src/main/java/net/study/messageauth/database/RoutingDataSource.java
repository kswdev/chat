package net.study.messageauth.database;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
public class RoutingDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        String datasourceKey = TransactionSynchronizationManager.isCurrentTransactionReadOnly() ? "replica" : "source";
        log.info("Routing to datasource: {}", datasourceKey);
        return datasourceKey;
    }
}
