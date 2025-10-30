# FROM maven:3.6.3-openjdk-11-slim as build

# LABEL COMPANY="Open Foris"
# LABEL MAINTAINER="openforisinitiative@gmail.com"
# LABEL APPLICATION="Open Foris Collect"

# WORKDIR /app
# COPY . .
# RUN apt-get update && apt-get install -y git
# RUN mvn package

# FROM tomcat:9.0.65-jdk11-openjdk-slim

# COPY --from=build /app/collect-webapp/target/collect.war /usr/local/tomcat/webapps/

# COPY collect-installation/docker/tomcat/conf/Catalina/localhost/collect.xml $CATALINA_HOME/conf/Catalina/localhost/collect.xml

# COPY collect-installation/docker/tomcat/bin/setenv.sh $CATALINA_HOME/bin/setenv.sh
# RUN chmod +x $CATALINA_HOME/bin/setenv.sh

# ADD https://repo1.maven.org/maven2/org/xerial/sqlite-jdbc/3.46.1.0/sqlite-jdbc-3.46.1.0.jar $CATALINA_HOME/lib/
# ADD https://repo1.maven.org/maven2/org/postgresql/postgresql/42.7.4/postgresql-42.7.4.jar $CATALINA_HOME/lib/
# ADD https://repo1.maven.org/maven2/com/h2database/h2/2.3.232/h2-2.3.232.jar $CATALINA_HOME/lib/

# EXPOSE 8080

FROM maven:3.6.3-openjdk-11-slim AS build

WORKDIR /app
COPY . .

# Fix Debian Buster repositories
RUN sed -i 's|http://deb.debian.org/debian|http://archive.debian.org/debian|g' /etc/apt/sources.list && \
    sed -i 's|http://security.debian.org/debian-security|http://archive.debian.org/debian-security|g' /etc/apt/sources.list && \
    sed -i '/buster-updates/d' /etc/apt/sources.list && \
    echo "Acquire::Check-Valid-Until false;" > /etc/apt/apt.conf.d/99no-check-valid-until

RUN apt-get update && apt-get install -y git
RUN mvn package