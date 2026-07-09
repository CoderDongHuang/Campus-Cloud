# 校享云 — 通用 Dockerfile（多阶段构建）
# 用法: docker build --build-arg SVC=campus-order -t campus-order .
FROM eclipse-temurin:17-jdk-alpine AS builder
ARG SVC
WORKDIR /app
COPY pom.xml ./
COPY campus-common/pom.xml campus-common/
COPY campus-common/common-core/pom.xml campus-common/common-core/
COPY campus-common/common-security/pom.xml campus-common/common-security/
COPY campus-common/common-mybatis/pom.xml campus-common/common-mybatis/
COPY campus-common/common-mq/pom.xml campus-common/common-mq/
COPY campus-common/common-es/pom.xml campus-common/common-es/
COPY ${SVC}/pom.xml ${SVC}/
RUN mvn dependency:go-offline -pl ${SVC} -am -q 2>/dev/null || true
COPY . .
RUN mvn package -pl ${SVC} -am -DskipTests -q

FROM eclipse-temurin:17-jre-alpine
ARG SVC
WORKDIR /app
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring
COPY --from=builder /app/${SVC}/target/*.jar app.jar
EXPOSE 8080
HEALTHCHECK --interval=30s --timeout=3s CMD wget -qO- http://localhost:8080/actuator/health || exit 1
ENTRYPOINT ["java", "-Xms256m", "-Xmx512m", "-XX:+UseG1GC", "-jar", "app.jar"]
