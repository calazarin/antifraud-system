# Anti Fraud System
 
This application is a RESTFul API developed for Hyperskill (by JetBrains Academy) course 
Spring Security for Java Backend Developers.

## Build and run

1. Build `./gradlew build`
2. Run `./gradlew bootRun`

You also might want to run it in your IDEA; just import it, build and run `AntiFraudApplication.java`

## OpenAPI

After starting the app, if you want to check the Swagger UI, please access the link below:

``http://localhost:28852/antifraud/swagger-ui/index.html``

## About Spring Security and User Roles

### User Roles

This API has 3 different roles:
1. Administrator
2. Merchant
3. Support

This application has some rules about those user roles:
1. First created user will be Administrator by default;
2. Other new users will be created with `Merchant` role. 

If you want to update any user role, you can use the `PUT: /api/auth/role` endpoint available in the `UserController` - for more details please 
check our Swagger. 

### Authorization

Following below a table illustrating the endpoints and the respective user role necessary to hit it:

|                                         | Administrator | Merchant | Support | 
|-----------------------------------------|---------------|----------|---------|
| GET: /api/auth/list                     | X             |          | X       | 
| DELETE: /api/user/**                    | X             |          |         |
| POST: /api/antifraud/transaction        |               | X        |         |
| PUT: /api/antifraud/transaction         |               |          | X       |
| DELETE: /api/antifraud/suspicious-ip/** |               |          | X       |
| /api/antifraud/suspicious-ip            |               |          | X       |
| /api/antifraud/stolencard               |               |          | X       |
| /api/antifraud/stolencard/**            |               |          | X       |
| PUT: /api/auth/access                   | X             |          |         |
| PUT: /api/auth/role                     | X             |          |         |
| GET: /api/antifraud/history/**          |               |          | X       | 

## Transaction Validation

A transaction is validated following some rules (for more details you also can check TransactionService.java):
1. Maximum allowed limit (by default is 200, but it can be changed hitting `PUT: /api/antifraud/transaction`);
2. Maximum manual processing limit (by default is 1500, but it can be changed hitting `PUT: /api/antifraud/transaction`);
3. Invalid IP address:
   1. An IP can be registered as a suspicious one;
4. Invalid card number:
   1. card number can be registered as a stolen card;
5. Several transactions with the same card number and distinct IP addresses:
   1. When having 2 transactions: status is `manual processing`;
   2. When having 3 or more transactions: status is `prohibited`;
6. Several transactions with the same card number and distinct regions:
   1. When having 3 transactions: status is `manual processing`;
   2. When having more than 3 transactions: status is `prohibited`;
