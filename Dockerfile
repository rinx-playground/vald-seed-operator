FROM clojure:lein-buster as builder
LABEL maintainer "Rintaro Okamura <rintaro.okamura@gmail.com>"


ENV DEBIAN_FRONTEND noninteractive

RUN set -eux \
    && apt-get update \
    && apt-get install -y \
    git \
    openssh-client \
    && apt-get clean -y \
    && rm -rf /var/lib/apt/lists/*

RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app

COPY project.clj /usr/src/app/
RUN lein deps
COPY . /usr/src/app
RUN mv "$(lein uberjar | sed -n 's/^Created \(.*standalone\.jar\)/\1/p')" app-standalone.jar

FROM alpine:edge
LABEL maintainer "Rintaro Okamura <rintaro.okamura@gmail.com>"

RUN set -eux \
    && apk update \
    && apk --no-cache add openjdk11-jre

RUN mkdir -p /usr/src/app
WORKDIR /usr/src/app

COPY --from=builder /usr/src/app/app-standalone.jar /usr/src/app/app-standalone.jar

ENTRYPOINT ["/usr/bin/java"]
CMD ["-Djava.security.policy=/usr/src/app/java.policy", "-jar", "app-standalone.jar"]
