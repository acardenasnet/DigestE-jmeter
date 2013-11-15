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
<code><br>
Client  Server<br>
   |      |<br>
   |----->| POST<br>
   |<-----| 401 Unauthorized WWW-Auth Header with key for Challange<br>
   |----->| POST Autentification Header with response encoded<br>
   |<-----| 200 OK<br>
</code>
