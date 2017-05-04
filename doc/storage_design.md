# Storage Design

## SQL

### Job

| Field            | Description                            |
| ---------------- | -------------------------------------- |
| id               | 主键                                     |
| name             | 任务名称                                   |
| userId           | 用户ID                                   |
| submitType       | 提交类型，取值范围：TAR、DOCKER                   |
| baseImage        | 基础镜像（only for TAR提交类型）                 |
| tarLocation      | tar文件下载地址（only for TAR提交类型）            |
| startScript      | 启动脚本，可选的，默认为start.sh（only for TAR提交类型） |
| dockerImage      | docker镜像（only for DOCKER提交类型）          |
| dockerCommand    | docker启动命令（only for DOCKER提交类型）        |
| resourceDecomand | 资源需求字段                                 |
| state            | 任务状态，取值范围：WAIT、LOCK、RUN、FAIL、SUCC      |
| startTime        | 任务开始时间                                 |
| endTime          | 任务结束时间                                 |


## HDFS