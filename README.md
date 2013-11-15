DigestE-jmeter
==============

Extension for Jmeter support Autentification Digest with SHA.

Inputs:
=======
username - Readable username
password - Readable Password.

Description:
============
- Encode the pssword Digest SHA
- Create Http Header with username:realm:encodedPassword
 

Protocol:
=========
This authentification is a challange.

Client  Server
   |      |
   |----->| POST
   |<-----| 401 Unauthorized WWW-Auth Header with key for Challange
   |----->| POST Autentification Header with response encoded
   |<-----| 200 OK
   
   
