/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.streaming.connectors.elasticsearch.table;

import org.apache.flink.annotation.Internal;
import org.apache.flink.configuration.ReadableConfig;
import org.apache.flink.connector.base.DeliveryGuarantee;
import org.apache.flink.connector.elasticsearch.sink.FlushBackoffType;
import org.apache.flink.table.api.ValidationException;

import org.apache.http.HttpHost;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.apache.flink.streaming.connectors.elasticsearch.table.Elasticsearch7ConnectorOptions.BULK_FLUSH_BACKOFF_DELAY_OPTION;
import static org.apache.flink.streaming.connectors.elasticsearch.table.Elasticsearch7ConnectorOptions.BULK_FLUSH_BACKOFF_MAX_RETRIES_OPTION;
import static org.apache.flink.streaming.connectors.elasticsearch.table.Elasticsearch7ConnectorOptions.BULK_FLUSH_BACKOFF_TYPE_OPTION;
import static org.apache.flink.streaming.connectors.elasticsearch.table.Elasticsearch7ConnectorOptions.BULK_FLUSH_INTERVAL_OPTION;
import static org.apache.flink.streaming.connectors.elasticsearch.table.Elasticsearch7ConnectorOptions.BULK_FLUSH_MAX_ACTIONS_OPTION;
import static org.apache.flink.streaming.connectors.elasticsearch.table.Elasticsearch7ConnectorOptions.BULK_FLUSH_MAX_SIZE_OPTION;
import static org.apache.flink.streaming.connectors.elasticsearch.table.Elasticsearch7ConnectorOptions.CONNECTION_PATH_PREFIX_OPTION;
import static org.apache.flink.streaming.connectors.elasticsearch.table.Elasticsearch7ConnectorOptions.DELIVERY_GUARANTEE_OPTION;
import static org.apache.flink.streaming.connectors.elasticsearch.table.Elasticsearch7ConnectorOptions.HOSTS_OPTION;
import static org.apache.flink.streaming.connectors.elasticsearch.table.Elasticsearch7ConnectorOptions.INDEX_OPTION;
import static org.apache.flink.streaming.connectors.elasticsearch.table.Elasticsearch7ConnectorOptions.KEY_DELIMITER_OPTION;
import static org.apache.flink.streaming.connectors.elasticsearch.table.Elasticsearch7ConnectorOptions.PASSWORD_OPTION;
import static org.apache.flink.streaming.connectors.elasticsearch.table.Elasticsearch7ConnectorOptions.USERNAME_OPTION;
import static org.apache.flink.util.Preconditions.checkNotNull;

/** Elasticsearch 7 specific configuration. */
@Internal
final class Elasticsearch7Configuration {
    protected final ReadableConfig config;

    Elasticsearch7Configuration(ReadableConfig config) {
        this.config = checkNotNull(config);
    }

    public int getBulkFlushMaxActions() {
        return config.get(BULK_FLUSH_MAX_ACTIONS_OPTION);
    }

    public long getBulkFlushMaxByteSize() {
        return config.get(BULK_FLUSH_MAX_SIZE_OPTION).getBytes();
    }

    public long getBulkFlushInterval() {
        return config.get(BULK_FLUSH_INTERVAL_OPTION).toMillis();
    }

    public DeliveryGuarantee getDeliveryGuarantee() {
        return config.get(DELIVERY_GUARANTEE_OPTION);
    }

    public Optional<String> getUsername() {
        return config.getOptional(USERNAME_OPTION);
    }

    public Optional<String> getPassword() {
        return config.getOptional(PASSWORD_OPTION);
    }

    public Optional<FlushBackoffType> getBulkFlushBackoffType() {
        return config.getOptional(BULK_FLUSH_BACKOFF_TYPE_OPTION);
    }

    public Optional<Integer> getBulkFlushBackoffRetries() {
        return config.getOptional(BULK_FLUSH_BACKOFF_MAX_RETRIES_OPTION);
    }

    public Optional<Long> getBulkFlushBackoffDelay() {
        return config.getOptional(BULK_FLUSH_BACKOFF_DELAY_OPTION).map(Duration::toMillis);
    }

    public String getIndex() {
        return config.get(INDEX_OPTION);
    }

    public String getKeyDelimiter() {
        return config.get(KEY_DELIMITER_OPTION);
    }

    public Optional<String> getPathPrefix() {
        return config.getOptional(CONNECTION_PATH_PREFIX_OPTION);
    }

    public List<HttpHost> getHosts() {
        return config.get(HOSTS_OPTION).stream()
                .map(Elasticsearch7Configuration::validateAndParseHostsString)
                .collect(Collectors.toList());
    }

    private static HttpHost validateAndParseHostsString(String host) {
        try {
            HttpHost httpHost = HttpHost.create(host);
            if (httpHost.getPort() < 0) {
                throw new ValidationException(
                        String.format(
                                "Could not parse host '%s' in option '%s'. It should follow the format 'http://host_name:port'. Missing port.",
                                host, HOSTS_OPTION.key()));
            }

            if (httpHost.getSchemeName() == null) {
                throw new ValidationException(
                        String.format(
                                "Could not parse host '%s' in option '%s'. It should follow the format 'http://host_name:port'. Missing scheme.",
                                host, HOSTS_OPTION.key()));
            }
            return httpHost;
        } catch (Exception e) {
            throw new ValidationException(
                    String.format(
                            "Could not parse host '%s' in option '%s'. It should follow the format 'http://host_name:port'.",
                            host, HOSTS_OPTION.key()),
                    e);
        }
    }
}
