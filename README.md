# AuthService

How to start the AuthService application
---

1. Run `mvn clean install` to build your application
1. Start application with `java -jar target/auth-service-1.0-SNAPSHOT.jar server config.yml`
1. To check that your application is running enter url `http://localhost:8080`

Health Check
---

To see your applications health enter url `http://localhost:8081/healthcheck`

Working With Submodule
---
1. Clone the repo `git clone git@github.com:arifluthfi16/movie-library-migration.git`
2. Run `git submodule update --init --recursive` on root dir to initialize the sub module
3. You can pull the sub module while you are in the parent dir using `git pull --recurse-submodules`
