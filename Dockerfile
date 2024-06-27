FROM gradle:8.7.0-jdk21

WORKDIR /app

COPY /app .

RUN ./gradlew installDist

CMD ./build/install/app/bin/app --spring.profiles.active=production
