# Solar ERP API Testing Checklist

## Pre-Testing Setup ✓

### Environment Setup
- [ ] Docker Desktop installed and running
- [ ] Java 21 or higher installed
- [ ] Maven installed
- [ ] Postman installed
- [ ] PostgreSQL container available

### Application Startup
- [ ] Run `setup-and-run.bat` OR manually start:
  - [ ] PostgreSQL: `docker-compose up -d`
  - [ ] Build: `mvn clean install -DskipTests`
  - [ ] Run: `mvn spring-boot:run -pl solar-erp-app`
- [ ] Application accessible at `http://localhost:8080`
- [ ] PostgreSQL accessible at `localhost:5432`

### Postman Setup
- [ ] Postman opened
- [ ] Imported `Solar_ERP_Postman_Collection.json`
- [ ] (Optional) Imported `Solar_ERP_Environment.json`
- [ ] Base URL set to `http://localhost:8080`
- [ ] Collection variables visible in environment

---

## Authentication Testing ✓

### Login Endpoint
- [ ] Test: `POST /api/v1/auth/login`
- [ ] Credentials: admin / admin
- [ ] Expected Status: `200 OK`
- [ ] Response contains: `token`, `username`, `role`
- [ ] JWT token auto-saved to `{{jwt_token}}`
- [ ] Postman Console shows: "Token saved to environment"

---

## Customer API Testing ✓

### Create Individual Customer
- [ ] Test: `POST /api/v1/customers`
- [ ] Request body: Individual customer type
- [ ] Expected Status: `201 Created`
- [ ] Response contains: `id`, `name`, `customerType`, `sites`
- [ ] Customer ID auto-saved to `{{customer_id}}`

### Create Company Customer
- [ ] Test: `POST /api/v1/customers`
- [ ] Request body: Company customer type
- [ ] Expected Status: `201 Created`
- [ ] Response contains: `companyName`, multiple `sites`
- [ ] Customer ID auto-saved to `{{company_customer_id}}`

### Create Society Customer
- [ ] Test: `POST /api/v1/customers`
- [ ] Request body: Society customer type
- [ ] Expected Status: `201 Created`
- [ ] Response contains: Society details and sites

### Get All Customers
- [ ] Test: `GET /api/v1/customers`
- [ ] Expected Status: `200 OK`
- [ ] Response is an array
- [ ] Response includes previously created customers
- [ ] Each customer has: `id`, `name`, `active`, `sites`

### Get Customer by ID
- [ ] Test: `GET /api/v1/customers/{{customer_id}}`
- [ ] Expected Status: `200 OK`
- [ ] Response matches created customer
- [ ] Response includes: `id`, `name`, `sites`, `createdAt`

### Search Customers
- [ ] Test: `GET /api/v1/customers/search?name=John`
- [ ] Expected Status: `200 OK`
- [ ] Response is an array
- [ ] Response contains customers matching search term
- [ ] Try different search terms

### Update Customer
- [ ] Test: `PUT /api/v1/customers/{{customer_id}}`
- [ ] Request body: Updated customer data
- [ ] Expected Status: `200 OK`
- [ ] Response reflects updated values
- [ ] Verify with Get Customer by ID

### Deactivate Customer
- [ ] Test: `DELETE /api/v1/customers/{{customer_id}}`
- [ ] Expected Status: `204 No Content`
- [ ] Verify deactivation with Get All Customers
- [ ] Deactivated customer should show `active: false`

---

## Material API Testing ✓

### Get Material Categories
- [ ] Test: `GET /api/v1/materials/categories`
- [ ] Expected Status: `200 OK`
- [ ] Response is an array
- [ ] Response includes all 6 categories:
  - [ ] PANEL - Solar Panel
  - [ ] INVERTER - Inverter
  - [ ] CABLE - Cable
  - [ ] STRUCTURE - Mounting Structure
  - [ ] ELECTRICAL - Electrical Components
  - [ ] OTHER - Other

### Create Solar Panel Material
- [ ] Test: `POST /api/v1/materials`
- [ ] Category: `PANEL`
- [ ] Expected Status: `201 Created`
- [ ] Response contains all fields from request
- [ ] Material ID auto-saved to `{{material_id}}`

### Create Inverter Material
- [ ] Test: `POST /api/v1/materials`
- [ ] Category: `INVERTER`
- [ ] Expected Status: `201 Created`
- [ ] Response includes inverter details

### Create Cable Material
- [ ] Test: `POST /api/v1/materials`
- [ ] Category: `CABLE`
- [ ] Expected Status: `201 Created`
- [ ] Response includes cable details

### Create Mounting Structure Material
- [ ] Test: `POST /api/v1/materials`
- [ ] Category: `STRUCTURE`
- [ ] Expected Status: `201 Created`
- [ ] Response includes structure details

### Create Electrical Components Material
- [ ] Test: `POST /api/v1/materials`
- [ ] Category: `ELECTRICAL`
- [ ] Expected Status: `201 Created`
- [ ] Response includes electrical details

### Get All Materials
- [ ] Test: `GET /api/v1/materials`
- [ ] Expected Status: `200 OK`
- [ ] Response is an array
- [ ] Response includes all created materials
- [ ] Each material has: `id`, `category`, `brandName`, `modelName`

### Get Material by ID
- [ ] Test: `GET /api/v1/materials/{{material_id}}`
- [ ] Expected Status: `200 OK`
- [ ] Response matches created material
- [ ] Response includes: `id`, `category`, `specification`, `createdAt`

### Get Materials by Category (PANEL)
- [ ] Test: `GET /api/v1/materials/category/PANEL`
- [ ] Expected Status: `200 OK`
- [ ] Response is an array
- [ ] Response includes only PANEL materials

### Get Materials by Category (INVERTER)
- [ ] Test: `GET /api/v1/materials/category/INVERTER`
- [ ] Expected Status: `200 OK`
- [ ] Response includes only INVERTER materials

### Get Materials by Component Key
- [ ] Test: `GET /api/v1/materials/component/ROOF_MOUNTED`
- [ ] Expected Status: `200 OK`
- [ ] Response is an array (may be empty if none match)

### Search Materials
- [ ] Test: `GET /api/v1/materials/search?brandName=Luminous`
- [ ] Expected Status: `200 OK`
- [ ] Response includes materials matching brand name
- [ ] Try different brand names

### Update Material
- [ ] Test: `PUT /api/v1/materials/{{material_id}}`
- [ ] Request body: Updated material data
- [ ] Expected Status: `200 OK`
- [ ] Response reflects updated values
- [ ] Verify with Get Material by ID

### Deactivate Material
- [ ] Test: `DELETE /api/v1/materials/{{material_id}}`
- [ ] Expected Status: `204 No Content`
- [ ] Verify deactivation with Get All Materials
- [ ] Deactivated material should show `active: false`

---

## Error Handling Testing ✓

### 401 Unauthorized
- [ ] Test any endpoint without JWT token
- [ ] Expected Status: `401 Unauthorized`
- [ ] Solution: Run Login endpoint first

### 404 Not Found
- [ ] Test: `GET /api/v1/customers/invalid-id`
- [ ] Expected Status: `404 Not Found`
- [ ] Response includes error message

### 400 Bad Request
- [ ] Test: Create customer with missing required fields
- [ ] Expected Status: `400 Bad Request`
- [ ] Response includes validation error details

### Invalid Enum Values
- [ ] Test: Create material with invalid category
- [ ] Expected Status: `400 Bad Request`
- [ ] Response indicates invalid enum value

---

## Database Verification ✓

### Connect to Database
- [ ] Use PostgreSQL client or tool
- [ ] Connection: `localhost:5432/solar_erp`
- [ ] Credentials: postgres/postgres

### Verify Tables
- [ ] Table: `users` - Contains admin account
- [ ] Table: `customers` - Contains created customers
- [ ] Table: `customer_sites` - Contains customer sites
- [ ] Table: `materials` - Contains created materials

### Query Data
- [ ] `SELECT * FROM customers;` - Shows created customers
- [ ] `SELECT * FROM materials;` - Shows created materials
- [ ] Verify counts match Postman responses

---

## Performance Testing ✓

### Get All (Pagination)
- [ ] Test: `GET /api/v1/customers`
- [ ] Response time: < 500ms (with small dataset)
- [ ] Test: `GET /api/v1/materials`
- [ ] Response time: < 500ms (with small dataset)

### Search Operations
- [ ] Test: Search customers - Response time < 300ms
- [ ] Test: Search materials - Response time < 300ms
- [ ] Test: Filter by category - Response time < 300ms

### Create Operations
- [ ] Test: Create customer - Response time < 1s
- [ ] Test: Create material - Response time < 1s
- [ ] Verify database insert

---

## Data Validation Testing ✓

### Required Fields
- [ ] Customer: `customerType`, `name`, `phone` required
- [ ] Customer Site: `siteLabel` required
- [ ] Material: `category`, `brandName`, `modelName` required

### Field Length Validation
- [ ] Test: Very long strings in fields
- [ ] Test: Empty strings for required fields

### Enum Validation
- [ ] Customer Type: Only INDIVIDUAL, COMPANY, SOCIETY
- [ ] Material Category: Only valid categories
- [ ] Invalid values return 400 Bad Request

---

## Documentation Verification ✓

### Postman Collection
- [ ] All endpoints present
- [ ] All methods correct (GET, POST, PUT, DELETE)
- [ ] All request bodies match API
- [ ] All headers correct (including Authorization)

### Request Examples
- [ ] Individual customer example works
- [ ] Company customer example works
- [ ] Society customer example works
- [ ] All material type examples work

### Response Examples
- [ ] Responses match actual API responses
- [ ] Field names match documentation
- [ ] Data types are correct

---

## Final Validation ✓

- [ ] All 6 test sections above passed
- [ ] No authentication errors
- [ ] No validation errors
- [ ] Database synchronized with API
- [ ] All CRUD operations working:
  - [ ] CREATE (POST)
  - [ ] READ (GET)
  - [ ] UPDATE (PUT)
  - [ ] DELETE (Deactivate)

---

## Issues Encountered & Resolution

### Issue: PostgreSQL Connection Failed
- [ ] Status: ☐ Not encountered ☐ Encountered & Fixed ☐ Ongoing
- [ ] Resolution:
  ```bash
  docker-compose restart postgres
  docker-compose logs postgres
  ```

### Issue: JWT Token Not Auto-saving
- [ ] Status: ☐ Not encountered ☐ Encountered & Fixed ☐ Ongoing
- [ ] Resolution: Check Postman test scripts in Login request

### Issue: 404 Not Found on Created Resource
- [ ] Status: ☐ Not encountered ☐ Encountered & Fixed ☐ Ongoing
- [ ] Resolution: Verify resource ID saved correctly in environment

### Issue: Maven Build Failed
- [ ] Status: ☐ Not encountered ☐ Encountered & Fixed ☐ Ongoing
- [ ] Resolution:
  ```bash
  mvn clean install -DskipTests -X
  ```

### Other Issues:
```


```

---

## Test Summary

**Date:** ________________

**Tested By:** ________________

**Total Tests:** 50+

**Passed:** ______ | **Failed:** ______ | **Skipped:** ______

**Overall Status:** ☐ PASS ☐ FAIL ☐ PARTIAL

**Notes:**
```


```

---

**Completion Time:** ________________ minutes

**Next Steps:**
- [ ] Document all findings
- [ ] Report any failures
- [ ] Create regression test suite
- [ ] Performance baseline established

---

**Testing Complete! ✨**

