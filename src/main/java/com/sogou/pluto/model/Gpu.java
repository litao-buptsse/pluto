package com.sogou.pluto.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by Tao Li on 2016/6/1.
 */
public class Gpu {
  public static final String STATE_OFFLINE = "OFFLINE";
  public static final String STATE_AVAILABLE = "AVAILABLE";
  public static final String STATE_IN_USE = "IN_USE";

  private long id;
  private String node;
  private int gpuId;
  private String state;

  public Gpu() {
    // Jackson deserialization
  }

  public Gpu(long id, String node, int gpuId, String state) {
    this.id = id;
    this.node = node;
    this.gpuId = gpuId;
    this.state = state;
  }

  @JsonProperty
  public long getId() {
    return id;
  }

  public void setId(long id) {
    this.id = id;
  }

  @JsonProperty
  public String getNode() {
    return node;
  }

  public void setNode(String node) {
    this.node = node;
  }

  @JsonProperty
  public int getGpuId() {
    return gpuId;
  }

  public void setGpuId(int gpuId) {
    this.gpuId = gpuId;
  }

  @JsonProperty
  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  @Override
  public String toString() {
    return "Gpu{" +
        "id=" + id +
        ", node='" + node + '\'' +
        ", gpuId='" + gpuId + '\'' +
        ", state='" + state + '\'' +
        '}';
  }
}
