package com.ccdd.outbox.repository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Optional;

@Repository
public class IdempotencyRepository {

    private final JdbcTemplate jdbcTemplate;

    public IdempotencyRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public static class IdempotencyRecord {
        private String idempotencyKey;
        private String tenantId;
        private String requestUri;
        private String requestHash;
        private int responseStatus;
        private String responseBodyJson;
        private Instant expiresAt;

        public IdempotencyRecord() {
        }

        public IdempotencyRecord(String idempotencyKey, String tenantId, String requestUri, String requestHash, 
                                 int responseStatus, String responseBodyJson, Instant expiresAt) {
            this.idempotencyKey = idempotencyKey;
            this.tenantId = tenantId;
            this.requestUri = requestUri;
            this.requestHash = requestHash;
            this.responseStatus = responseStatus;
            this.responseBodyJson = responseBodyJson;
            this.expiresAt = expiresAt;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private String idempotencyKey;
            private String tenantId;
            private String requestUri;
            private String requestHash;
            private int responseStatus;
            private String responseBodyJson;
            private Instant expiresAt;

            public Builder idempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; return this; }
            public Builder tenantId(String tenantId) { this.tenantId = tenantId; return this; }
            public Builder requestUri(String requestUri) { this.requestUri = requestUri; return this; }
            public Builder requestHash(String requestHash) { this.requestHash = requestHash; return this; }
            public Builder responseStatus(int responseStatus) { this.responseStatus = responseStatus; return this; }
            public Builder responseBodyJson(String responseBodyJson) { this.responseBodyJson = responseBodyJson; return this; }
            public Builder expiresAt(Instant expiresAt) { this.expiresAt = expiresAt; return this; }

            public IdempotencyRecord build() {
                return new IdempotencyRecord(idempotencyKey, tenantId, requestUri, requestHash, responseStatus, responseBodyJson, expiresAt);
            }
        }

        public String getIdempotencyKey() { return idempotencyKey; }
        public String getTenantId() { return tenantId; }
        public String getRequestUri() { return requestUri; }
        public String getRequestHash() { return requestHash; }
        public int getResponseStatus() { return responseStatus; }
        public String getResponseBodyJson() { return responseBodyJson; }
        public Instant getExpiresAt() { return expiresAt; }
    }

    public Optional<IdempotencyRecord> findValidRecord(String tenantId, String idempotencyKey) {
        String sql = """
            SELECT idempotency_key, tenant_id, request_uri, request_hash, response_status, response_body_json, expires_at
            FROM sys_idempotency_records
            WHERE tenant_id = ? AND idempotency_key = ? AND expires_at > CURRENT_TIMESTAMP
        """;
        try {
            IdempotencyRecord record = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> IdempotencyRecord.builder()
                    .idempotencyKey(rs.getString("idempotency_key"))
                    .tenantId(rs.getString("tenant_id"))
                    .requestUri(rs.getString("request_uri"))
                    .requestHash(rs.getString("request_hash"))
                    .responseStatus(rs.getInt("response_status"))
                    .responseBodyJson(rs.getString("response_body_json"))
                    .expiresAt(rs.getTimestamp("expires_at").toInstant())
                    .build(), tenantId, idempotencyKey);
            return Optional.ofNullable(record);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    public void save(String idempotencyKey, String tenantId, String requestUri, String requestHash, int status, String responseBodyJson, Instant expiresAt) {
        String sql = """
            INSERT INTO sys_idempotency_records (idempotency_key, tenant_id, request_uri, request_hash, response_status, response_body_json, expires_at)
            VALUES (?, ?, ?, ?, ?, ?::jsonb, ?)
            ON CONFLICT (idempotency_key) DO NOTHING
        """;
        jdbcTemplate.update(sql, idempotencyKey, tenantId, requestUri, requestHash, status, responseBodyJson, Timestamp.from(expiresAt));
    }
}
