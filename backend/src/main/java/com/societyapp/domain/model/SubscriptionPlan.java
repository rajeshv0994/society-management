package com.societyapp.domain.model;

import java.math.BigDecimal;

public class SubscriptionPlan {
    private String code;
    private String name;
    private String approvalMode;
    private Integer maxFlats;
    private BigDecimal priceMonthly;
    private BigDecimal pricePerSpace;
    private Integer trialDurationMonths;
    private Integer trialDurationValue;
    private String trialDurationUnit;
    private Integer maxStaff;
    private BigDecimal pricePerStaff;

    @java.lang.SuppressWarnings("all")
    public String getCode() {
        return this.code;
    }

    @java.lang.SuppressWarnings("all")
    public String getName() {
        return this.name;
    }

    @java.lang.SuppressWarnings("all")
    public String getApprovalMode() {
        return this.approvalMode;
    }

    @java.lang.SuppressWarnings("all")
    public Integer getMaxFlats() {
        return this.maxFlats;
    }

    @java.lang.SuppressWarnings("all")
    public BigDecimal getPriceMonthly() {
        return this.priceMonthly;
    }

    @java.lang.SuppressWarnings("all")
    public BigDecimal getPricePerSpace() {
        return this.pricePerSpace;
    }

    @java.lang.SuppressWarnings("all")
    public Integer getTrialDurationMonths() {
        return this.trialDurationMonths;
    }

    @java.lang.SuppressWarnings("all")
    public Integer getTrialDurationValue() {
        return this.trialDurationValue;
    }

    @java.lang.SuppressWarnings("all")
    public String getTrialDurationUnit() {
        return this.trialDurationUnit;
    }

    @java.lang.SuppressWarnings("all")
    public Integer getMaxStaff() {
        return this.maxStaff;
    }

    @java.lang.SuppressWarnings("all")
    public BigDecimal getPricePerStaff() {
        return this.pricePerStaff;
    }

    @java.lang.SuppressWarnings("all")
    public void setCode(final String code) {
        this.code = code;
    }

    @java.lang.SuppressWarnings("all")
    public void setName(final String name) {
        this.name = name;
    }

    @java.lang.SuppressWarnings("all")
    public void setApprovalMode(final String approvalMode) {
        this.approvalMode = approvalMode;
    }

    @java.lang.SuppressWarnings("all")
    public void setMaxFlats(final Integer maxFlats) {
        this.maxFlats = maxFlats;
    }

    @java.lang.SuppressWarnings("all")
    public void setPriceMonthly(final BigDecimal priceMonthly) {
        this.priceMonthly = priceMonthly;
    }

    @java.lang.SuppressWarnings("all")
    public void setPricePerSpace(final BigDecimal pricePerSpace) {
        this.pricePerSpace = pricePerSpace;
    }

    @java.lang.SuppressWarnings("all")
    public void setTrialDurationMonths(final Integer trialDurationMonths) {
        this.trialDurationMonths = trialDurationMonths;
    }

    @java.lang.SuppressWarnings("all")
    public void setTrialDurationValue(final Integer trialDurationValue) {
        this.trialDurationValue = trialDurationValue;
    }

    @java.lang.SuppressWarnings("all")
    public void setTrialDurationUnit(final String trialDurationUnit) {
        this.trialDurationUnit = trialDurationUnit;
    }

    @java.lang.SuppressWarnings("all")
    public void setMaxStaff(final Integer maxStaff) {
        this.maxStaff = maxStaff;
    }

    @java.lang.SuppressWarnings("all")
    public void setPricePerStaff(final BigDecimal pricePerStaff) {
        this.pricePerStaff = pricePerStaff;
    }

    @java.lang.SuppressWarnings("all")
    public SubscriptionPlan() {}
}
