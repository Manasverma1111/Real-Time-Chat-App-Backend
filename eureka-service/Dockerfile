# Use Eclipse Temurin OpenJDK 17 as the base image
FROM eclipse-temurin:17-jdk

# Set working directory inside the container
WORKDIR /app

# Copy all project files into the container
COPY . .

# Give executable permission to Maven wrapper
RUN chmod +x mvnw

# Build the Spring Boot application and skip tests during Docker build
RUN ./mvnw clean package -DskipTests

# Expose Eureka server port
EXPOSE 8761

# Start the Spring Boot Eureka server application
CMD ["java", "-jar", "target/*.jar"]