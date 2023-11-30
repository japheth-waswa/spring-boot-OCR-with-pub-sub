FROM ubuntu:22.04

ARG JAR_FILE=target/*.jar
COPY ./target/ocrservice-0.0.1.jar app.jar

#create assets dir
RUN mkdir assets

RUN apt update
RUN apt upgrade -y

#install jdk,jre 17
RUN apt install openjdk-17-jre -y
RUN apt install openjdk-17-jdk -y

##install tesseract
RUN apt install tesseract-ocr -y

##copy libtesseract.so to the current location(todo this is bound to change according to the latest version in alpine linux)
RUN cp /usr/lib/x86_64-linux-gnu/libtesseract.so.4.0.1 /libtesseract.so
#
##configure jar to use the tesseract
RUN jar uf app.jar libtesseract.so

ENTRYPOINT ["java","-jar","/app.jar"]

#find / -type f -iname libtesseract.*
#cp /usr/lib/x86_64-linux-gnu/libtesseract.so.4.0.1 /libtesseract.so
