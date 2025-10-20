## Spring Boot - Management Stock

## System Requirements

- Java openjdk : Version 17.0.2
- Spring Boot : version 3.4.1
- Database : h2
- Maven : Apache Maven 3.9.3
- Editor : Intellij IDEA 2023.1.1 Community Edition
- Postman

## Dependencies

- spring-boot-starter-web
- spring-boot-starter-validation
- spring-boot-starter-data-jpa
- h2
- lombok

---
### Endpoint: Item
Base URL: localhost:8080/api/items

Method  | Endpoint        | Description
--------|-----------------|------------------------------
GET     | /api/items      | Get All Item
GET     | /api/items/{id} | Get Item By Id
POST    | /api/items      | Create Item
PUT     | /api/items/{id} | Update Item By Id
DELETE  | /api/items/{id} | Delete Item By Id


### Endpoint: Inventory
Base URL: localhost:8080/api/inventory

Method  | Endpoint                     | Description
--------|------------------------------|------------------------------
GET     | /api/inventory     | Get All Inventory
GET     | /api/inventory/{id} | Get Inventory By Id
POST    | /api/inventory      | Create Inventory
PUT     | /api/inventory/{id} | Update Inventory By Id
DELETE  | /api/inventory/{id} | Delete Inventory By Id


### Endpoint: Orders
Base URL: localhost:8080/api/orders

Method  | Endpoint         | Description
--------|------------------|------------------------------
GET     | /api/orders      | Get All Order
GET     | /api/orders/{id} | Get Order By Id
POST    | /api/orders      | Create Order
PUT     | /api/orders/{id} | Update Order By Id
DELETE  | /api/orders/{id} | Delete Order By Id

