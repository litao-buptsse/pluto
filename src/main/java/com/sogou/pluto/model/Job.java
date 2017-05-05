package com.sogou.pluto.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Tao Li on 2016/6/1.
 */
public class Job {
  public static final String STATE_WAIT = "WAIT";
  public static final String STATE_LOCK = "LOCK";
  public static final String STATE_RUN = "RUN";
  public static final String STATE_FAIL = "FAIL";
  public static final String STATE_SUCC = "SUCC";

  private long id;
  private String name;
  private String userId;
  private String baseImage;
  private String tarLocation;
  private String startScript;
  private String state;
  private String startTime;
  private String endTime;
  private String host;
  private String gpuId;

  public Job() {
    // Jackson deserialization
  }

  public Job(long id, String name, String userId,
             String baseImage, String tarLocation, String startScript,
             String state, String startTime, String endTime, String host, String gpuId) {
    this.id = id;
    this.name = name;
    this.userId = userId;
    this.baseImage = baseImage;
    this.tarLocation = tarLocation;
    this.startScript = startScript;
    this.state = state;
    this.startTime = startTime;
    this.endTime = endTime;
    this.host = host;
    this.gpuId = gpuId;
  }

  @JsonProperty
  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  @JsonProperty
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @JsonProperty
  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  @JsonProperty
  public String getBaseImage() {
    return baseImage;
  }

  public void setBaseImage(String baseImage) {
    this.baseImage = baseImage;
  }

  @JsonProperty
  public String getTarLocation() {
    return tarLocation;
  }

  public void setTarLocation(String tarLocation) {
    this.tarLocation = tarLocation;
  }

  @JsonProperty
  public String getStartScript() {
    return startScript;
  }

  public void setStartScript(String startScript) {
    this.startScript = startScript;
  }

  @JsonProperty
  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  @JsonProperty
  public String getStartTime() {
    return startTime;
  }

  public void setStartTime(String startTime) {
    this.startTime = startTime;
  }

  @JsonProperty
  public String getEndTime() {
    return endTime;
  }

  public void setEndTime(String endTime) {
    this.endTime = endTime;
  }

  @JsonProperty
  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  @JsonProperty
  public String getGpuId() {
    return gpuId;
  }

  public void setGpuId(String gpuId) {
    this.gpuId = gpuId;
  }

  @Override
  public String toString() {
    return "Job{" +
        "id=" + id +
        ", name='" + name + '\'' +
        ", userId='" + userId + '\'' +
        ", baseImage='" + baseImage + '\'' +
        ", tarLocation='" + tarLocation + '\'' +
        ", startScript='" + startScript + '\'' +
        ", state='" + state + '\'' +
        ", startTime='" + startTime + '\'' +
        ", endTime='" + endTime + '\'' +
        ", host='" + host + '\'' +
        ", gpuId='" + gpuId + '\'' +
        '}';
  }
}
