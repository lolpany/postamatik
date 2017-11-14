FROM openjdk:9-b181-jre-slim

# Set the working directory to /app
WORKDIR /app

# Copy the current directory contents into the container at /app
ADD ./target/ /app

# Install any needed packages specified in requirements.txt
# RUN pip install -r requirements.txt

# Make port 80 available to the world outside this container
# EXPOSE 80

# Define environment variable
# ENV NAME World

# Run when the container launches
CMD ["java", "-jar", "web-go-1.0-jar-with-dependencies.jar"]