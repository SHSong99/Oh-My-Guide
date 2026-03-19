FROM apache/spark:3.5.3

USER root

RUN apt-get update && apt-get install -y wget unzip && \
    rm -rf /var/lib/apt/lists/*

RUN wget -q https://archive.apache.org/dist/incubator/livy/0.8.0-incubating/apache-livy-0.8.0-incubating_2.12-bin.zip \
    && unzip -q apache-livy-0.8.0-incubating_2.12-bin.zip -d /opt/ \
    && mv /opt/apache-livy-0.8.0-incubating_2.12-bin /opt/livy \
    && rm apache-livy-0.8.0-incubating_2.12-bin.zip \
    && mkdir -p /opt/livy/logs

ENV LIVY_HOME=/opt/livy
ENV SPARK_HOME=/opt/spark

EXPOSE 8998

CMD ["/opt/livy/bin/livy-server"]
