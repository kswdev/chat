package net.study.messagesystem.database;

import lombok.extern.slf4j.Slf4j;
import net.study.messagesystem.domain.channel.ChannelId;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
public class RoutingDataSource extends AbstractRoutingDataSource {

    @Override
    protected Object determineCurrentLookupKey() {
        String datasourceKey = TransactionSynchronizationManager.isCurrentTransactionReadOnly() ? "replica" : "source";

        Long channelId = ShardContext.getChannelId();
        if (channelId != null) {
            datasourceKey += channelId % 2 == 0 ? "Message2" : "Message1";
        }
        log.info("Routing to datasource: {}", datasourceKey);
        return datasourceKey;
    }
}
