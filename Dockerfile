FROM openjdk:17-jdk-alpine
ARG JAR_FILE=target/*.jar
COPY ./target/ocrservice-0.0.1.jar app.jar

#install tesseract
RUN apk update && apk add tesseract-ocr

#copy libtesseract.so to the current location(todo this is bound to change according to the latest version in alpine linux)
RUN cp /usr/lib/libtesseract.so.4.0.1 /libtesseract.so

#configure jar to use the tesseract
RUN jar uf app.jar libtesseract.so

ENTRYPOINT ["java","-jar","/app.jar"]

#/usr/lib/libtesseract.so.4.0.1
#find / -type f -iname libtesseract.*
#cp /usr/lib/libtesseract.so.4.0.1 /libtesseract.so
