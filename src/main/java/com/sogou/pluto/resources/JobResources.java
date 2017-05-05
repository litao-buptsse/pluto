package com.sogou.pluto.resources;

import com.sogou.pluto.Config;
import com.sogou.pluto.model.Job;

import java.sql.SQLException;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

/**
 * Created by Tao Li on 6/1/16.
 */
@Path("/jobs")
public class JobResources {
  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  public void submitJob(Job job) throws SQLException {
    Config.JOB_DAO.createJob(job);
  }
}