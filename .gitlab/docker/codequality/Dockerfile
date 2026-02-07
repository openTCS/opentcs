# SPDX-FileCopyrightText: The openTCS Authors
# SPDX-License-Identifier: MIT

#
# Remember to update version.yml when you change the image contents here!
#
FROM eclipse-temurin:21.0.10_7-jdk-noble

RUN DEBIAN_FRONTEND=noninteractive apt-get update && DEBIAN_FRONTEND=noninteractive apt-get install -y \
  bash \
  nodejs \
  npm \
  sed \
  && rm -rf /var/lib/apt/lists/*

RUN npm install -g violations-command-line@1.25.3

CMD ["/bin/bash"]
