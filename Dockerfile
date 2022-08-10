FROM adoptopenjdk/openjdk11-openj9:alpine-jre
ADD run /updater/
WORKDIR /updater/
ADD build/libs/updater-0.03-all.jar /updater/
CMD java -Dupdater.config=/updater/meta.json -jar /updater/updater-0.03-all.jar
