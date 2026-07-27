package com.shopmanagement.ipdservice.accommodation.client;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * JSON DTOs for accommodation-service responses (no JPA in ipd-service).
 */
public final class AccommodationDtos {

    private AccommodationDtos() {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class BedOccupancyDto {
        private Long id;
        private Long bedId;
        private String admissionNo;
        private String status;
        private LocalDateTime allocatedAt;
        private LocalDateTime releasedAt;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Long getBedId() {
            return bedId;
        }

        public void setBedId(Long bedId) {
            this.bedId = bedId;
        }

        public String getAdmissionNo() {
            return admissionNo;
        }

        public void setAdmissionNo(String admissionNo) {
            this.admissionNo = admissionNo;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public LocalDateTime getAllocatedAt() {
            return allocatedAt;
        }

        public void setAllocatedAt(LocalDateTime allocatedAt) {
            this.allocatedAt = allocatedAt;
        }

        public LocalDateTime getReleasedAt() {
            return releasedAt;
        }

        public void setReleasedAt(LocalDateTime releasedAt) {
            this.releasedAt = releasedAt;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AccommodationBedDto {
        private Long id;
        private Long roomNodeId;
        private String bedCode;
        private String status;
        private BigDecimal dailyCharge;
        private String category;
        private boolean isolationFlag;
        private String isolationType;
        private String allowedGender;
        private Integer minAgeYears;
        private Integer maxAgeYears;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Long getRoomNodeId() {
            return roomNodeId;
        }

        public void setRoomNodeId(Long roomNodeId) {
            this.roomNodeId = roomNodeId;
        }

        public String getBedCode() {
            return bedCode;
        }

        public void setBedCode(String bedCode) {
            this.bedCode = bedCode;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public BigDecimal getDailyCharge() {
            return dailyCharge;
        }

        public void setDailyCharge(BigDecimal dailyCharge) {
            this.dailyCharge = dailyCharge;
        }

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public boolean isIsolationFlag() {
            return isolationFlag;
        }

        public void setIsolationFlag(boolean isolationFlag) {
            this.isolationFlag = isolationFlag;
        }

        public String getIsolationType() {
            return isolationType;
        }

        public void setIsolationType(String isolationType) {
            this.isolationType = isolationType;
        }

        public String getAllowedGender() {
            return allowedGender;
        }

        public void setAllowedGender(String allowedGender) {
            this.allowedGender = allowedGender;
        }

        public Integer getMinAgeYears() {
            return minAgeYears;
        }

        public void setMinAgeYears(Integer minAgeYears) {
            this.minAgeYears = minAgeYears;
        }

        public Integer getMaxAgeYears() {
            return maxAgeYears;
        }

        public void setMaxAgeYears(Integer maxAgeYears) {
            this.maxAgeYears = maxAgeYears;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AccommodationNodeDto {
        private Long id;
        private Long parentId;
        private String nodeType;
        private String code;
        private String name;
        private boolean active = true;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public Long getParentId() {
            return parentId;
        }

        public void setParentId(Long parentId) {
            this.parentId = parentId;
        }

        public String getNodeType() {
            return nodeType;
        }

        public void setNodeType(String nodeType) {
            this.nodeType = nodeType;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean isActive() {
            return active;
        }

        public void setActive(boolean active) {
            this.active = active;
        }
    }
}
