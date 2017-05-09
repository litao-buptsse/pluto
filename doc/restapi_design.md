# REST API Design


## REST API

| ID   | Type | Description | Resource | Http Method | URL Params | Request Data (json) | Reponse Data (json) |
| ---- | ---- | ----------- | -------- | ----------- | ---------- | ------------------- | ------------------- |
| 1    | Job  | 创建任务        | /jobs    | POST        |            | $job                |                     |


## Model

### Job

```
{
  name,
  userId,
  submitType, // TAR, DOCKER
  baseImage, // for type TAR
  tarLocation, // for type TAR
  startCommand, // for type TAR, optional, default start.sh
  dockerImage, // for type DOCKER
  dockerCommand, // for type DOCKER
  resourceDemand
}
```