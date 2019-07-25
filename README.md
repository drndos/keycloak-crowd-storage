keycloak-crowd-storage: User Storage SPI Crowd Library
========================================================

What is it?
-----------

This library implements user storage provider using the User Storage SPI.  This provider is backed by REST Crowd integration.  Once you deploy and enable this provider for a specific realm, you will be able to login to that realm using the users defined in the crowd server.

For now only basic read-only functions are implemented.

System Requirements
-------------------

You need to have <span>Keycloak</span> running.

All you need to build this project is Java 8.0 (Java SDK 1.8) or later and Maven 3.1.1 or later.


Build and Deploy the library
-------------------------------

To deploy this provider you must have <span>Keycloak</span> running in standalone or standalone-ha mode. Then type the follow maven command:

   ````
   mvn clean install wildfly:deploy
   ````
If you want to play with and modify the example, simply rerun the maven deploy command above and the new version will be hot deployed.

Enable the Provider for a Realm
-------------------------------
Login to the <span>Keycloak</span> Admin Console and got to the User Federation tab.   You should now see your deployed providers in the add-provider list box.

For the `crowd` provider, you will have to specify Crowd server base URL, application name and password.
