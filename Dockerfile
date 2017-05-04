FROM registry.docker.dev.sogou-inc.com:5000/clouddev/bigdatakit:1.1.0

ENV APPROOT /search/pluto
WORKDIR $APPROOT
ADD pluto $APPROOT

RUN mkdir -p logs
CMD bin/start.sh >logs/pluto.out 2>&1
