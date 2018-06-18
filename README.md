# salesstats
This is a spring-boot application with a main class [```Application```](src/main/java/com/ebay/salesstatsnicolasr/Application.java). To start the server, run the main class in your favorite IDE.
To run the tests, do a ```mvn test``` from the command line (or run them in your IDE). There is also one [```LoadTest```](src/test/java/com/ebay/salesstatsnicolasr/LoadTest.java), which takes 90 seconds to complete and which is not included in the default test suite. You can run it from the IDE directly or from the command line: ```mvn test -P SlowTests```.
