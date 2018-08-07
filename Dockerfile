FROM openjdk:8


# Install UIMA AS
ADD uima-as-2.10.2-bin.tar.gz /uima-as
RUN mv /uima-as/apache-uima-as-2.10.2/* /uima-as
RUN rm -rf /uima-as/apache-uima-as-2.10.2
RUN mkdir /uima-as/classpath

ENV UIMA_HOME=/uima-as
ENV PATH="/uima-as/bin:${PATH}"
ENV ACTIVEMQ_BASE=/active-mq
ENV UIMA_CLASSPATH=/uima-as/classpath

EXPOSE 61616
EXPOSE 8080

COPY healthcheckBroker.sh /healthcheckBroker.sh
#COPY entryscript.sh /entryscript.sh
