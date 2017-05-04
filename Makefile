PROJECT_NAME=$(shell cat pom.xml  | grep '<artifactId>' | head -n 1 | awk -F'<artifactId>' '{print $$2}' | awk -F'</artifactId>' '{print $$1}')
PROJECT_VERSION=$(shell cat pom.xml  | grep '<version>' | head -n 1 | awk -F'<version>' '{print $$2}' | awk -F'</version>' '{print $$1}')

IMAGE_MAIN_NAME='clouddev/$(PROJECT_NAME)'
IMAGE_VERSION=$(PROJECT_VERSION)
IMAGE=$(IMAGE_MAIN_NAME):$(IMAGE_VERSION)

REGISTRY='registry.docker.dev.sogou-inc.com:5000'

JAR=$(PROJECT_NAME)-$(PROJECT_VERSION).jar

ifdef NO_CACHE
	BUILD_PARAM='--no-cache=true'
else
	BUILD_PARAM=
endif

all: build

clean:
	rm -fr pluto.tar.gz pluto
	mvn clean

build:
	mvn package

dist: build
	mkdir -p pluto pluto/bin pluto/conf pluto/lib
	cp target/$(JAR) pluto/lib
	cp conf/* pluto/conf
	cp -r bin/* pluto/bin
	tar -czvf pluto.tar.gz pluto
	rm -fr pluto

docker-build: dist
	tar -xzvf pluto.tar.gz
	docker build $(BUILD_PARAM) -t $(IMAGE_MAIN_NAME) .
	docker tag -f $(IMAGE_MAIN_NAME) $(REGISTRY)/$(IMAGE)
	rm -fr pluto pluto.tar.gz

docker-push: docker-build
	docker push $(REGISTRY)/$(IMAGE)
