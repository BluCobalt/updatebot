FROM ibm-semeru-runtimes:open-17-jre-jammy
ADD run /updater/
WORKDIR /updater/
ADD build/libs/updater-2.0.0-all.jar /updater/
CMD java -Dupdater.config=/updater/meta.json -Xtune:virtualized -jar /updater/updater-2.0.0-all.jar
