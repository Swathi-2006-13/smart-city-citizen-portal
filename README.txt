SMART CITY BACKEND

1. Create MySQL database:
   CREATE DATABASE smart_city;

2. Edit:
   src/main/resources/application.properties
   and set spring.datasource.password.

3. Put your existing frontend inside:
   src/main/resources/static/
   Required:
   index.html
   home.html
   login.html
   register.html
   services.html
   request.html
   track.html
   contact.html
   css/style.css
   css/login.css
   js/script.js
   images/logo.png
   images/city1.jpg ... city5.jpg

4. Run:
   mvn clean spring-boot:run

5. Open:
   http://localhost:8080/login.html

Backend APIs:
POST /api/auth/register
POST /api/auth/login
POST /api/auth/logout
POST /api/complaints
GET  /api/complaints/track/{complaintId}
GET  /api/complaints/mine
POST /api/contact

The login uses an HTTP session, so no JWT is required for this first integration.
