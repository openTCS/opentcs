#
# Remember to update version.yml when you change the image contents here!
#
FROM adoptopenjdk:13-jdk-hotspot

RUN DEBIAN_FRONTEND=noninteractive apt-get update && DEBIAN_FRONTEND=noninteractive apt-get install -y \
  bash \
  nodejs \
  npm \
  sed \
  && rm -rf /var/lib/apt/lists/*

RUN npm install -g violations-command-line@1.25.3

CMD ["/bin/bash"]
