# Solar ERP API Testing Guide - Postman

## Overview
This guide explains how to test the **Customer API** and **Material API** using the provided Postman collection.

---

## Prerequisites

1. **Java 21+** installed
2. **PostgreSQL** running (see Docker setup below)
3. **Postman** installed ([Download](https://www.postman.com/downloads/))
4. **Spring Boot Application** running on `http://localhost:8080`

---

## Setup Instructions

### 1. Start PostgreSQL using Docker Compose

```bash
# Navigate to the app directory
cd D:\SolarApp\solar-erp-app\solar-erp-app

# Start PostgreSQL
docker-compose up -d
```

This starts PostgreSQL on `localhost:5432` with:
- Database: `solar_erp`
- Username: `postgres`
- Password: `postgres`

### 2. Build and Run the Spring Boot Application

```bash
# Navigate to project root
cd D:\SolarApp\solar-erp-app

# Build the project
mvn clean install

# Run the application
mvn spring-boot:run -pl solar-erp-app
```

The application will start on `http://localhost:8080`

### 3. Import Collection in Postman

1. Open Postman
2. Click **Import** button (top left)
3. Select the file: `Solar_ERP_Postman_Collection.json`
4. Click **Import**

---

## Testing Flow

### Step 1: Authenticate (Get JWT Token)

1. In Postman, navigate to **Authentication** > **Login**
2. Default credentials:
   - Username: `admin`
   - Password: `admin`
3. Click **Send**
4. The JWT token will automatically be saved to the environment variable `{{jwt_token}}`

**Response Example:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "username": "admin",
  "role": "ADMIN"
}
```

---

### Step 2: Test Customer API

#### 2.1 Create Customer (Individual)

1. Go to **Customer API** > **Create Customer (Individual)**
2. Click **Send**
3. The customer ID will be saved to `{{customer_id}}`

**Request Body:**
```json
{
  "customerType": "INDIVIDUAL",
  "name": "John Doe",
  "phone": "+91-9876543210",
  "email": "john@example.com",
  "address": "123 Main Street",
  "city": "Mumbai",
  "state": "Maharashtra",
  "pincode": "400001",
  "gstNumber": "27AABCT1234A1Z0",
  "sites": [
    {
      "siteLabel": "Residence",
      "address": "123 Main Street",
      "city": "Mumbai",
      "state": "Maharashtra",
      "pincode": "400001",
      "isDefault": true
    }
  ]
}
```

**Response Status:** `201 Created`

---

#### 2.2 Create Customer (Company)

1. Go to **Customer API** > **Create Customer (Company)**
2. Click **Send**
3. This creates a company customer with multiple sites

**Customer Types:**
- `INDIVIDUAL` - Single person
- `COMPANY` - Business entity
- `SOCIETY` - Residential society

---

#### 2.3 Get All Customers

1. Go to **Customer API** > **Get All Customers**
2. Click **Send**

**Response Status:** `200 OK`

---

#### 2.4 Get Customer by ID

1. Go to **Customer API** > **Get Customer by ID**
2. Ensure `{{customer_id}}` variable is set (from Create Customer)
3. Click **Send**

---

#### 2.5 Search Customers

1. Go to **Customer API** > **Search Customers**
2. Update the `name` query parameter as needed
3. Click **Send**

---

#### 2.6 Update Customer

1. Go to **Customer API** > **Update Customer**
2. Modify the request body as needed
3. Ensure `{{customer_id}}` is set
4. Click **Send**

**Response Status:** `200 OK`

---

#### 2.7 Deactivate Customer

1. Go to **Customer API** > **Deactivate Customer**
2. Ensure `{{customer_id}}` is set
3. Click **Send**

**Response Status:** `204 No Content`

---

### Step 3: Test Material API

#### 3.1 Get Material Categories

1. Go to **Material API** > **Get Material Categories**
2. Click **Send**

**Available Categories:**
- `PANEL` - Solar Panels
- `INVERTER` - Inverters
- `CABLE` - Cables
- `STRUCTURE` - Mounting Structures
- `ELECTRICAL` - Electrical Components
- `OTHER` - Other materials

---

#### 3.2 Create Material (Solar Panel)

1. Go to **Material API** > **Create Material (Solar Panel)**
2. Click **Send**
3. The material ID will be saved to `{{material_id}}`

**Request Body:**
```json
{
  "category": "PANEL",
  "componentKey": "ROOF_MOUNTED",
  "brandName": "Luminous",
  "modelName": "LSQUARE-550",
  "specification": "550W Monocrystalline Solar Panel",
  "unit": "Piece",
  "warranty": "25 Years",
  "hsnCode": "8541.40"
}
```

**Response Status:** `201 Created`

---

#### 3.3 Create Material (Inverter)

1. Go to **Material API** > **Create Material (Inverter)**
2. Click **Send**

**Request Body:**
```json
{
  "category": "INVERTER",
  "componentKey": "GRID_TIED",
  "brandName": "ABB",
  "modelName": "UNO-3.3-TL-OUTD",
  "specification": "3.3 kW Grid Tied Inverter",
  "unit": "Piece",
  "warranty": "10 Years",
  "hsnCode": "8504.40"
}
```

---

#### 3.4 Create Material (Cable)

1. Go to **Material API** > **Create Material (Cable)**
2. Click **Send**

---

#### 3.5 Get All Materials

1. Go to **Material API** > **Get All Materials**
2. Click **Send**

---

#### 3.6 Get Materials by Category

1. Go to **Material API** > **Get Materials by Category (PANEL)**
2. Click **Send**

To search other categories, replace `PANEL` with:
- `INVERTER`
- `CABLE`
- `STRUCTURE`
- `ELECTRICAL`
- `OTHER`

---

#### 3.7 Search Materials

1. Go to **Material API** > **Search Materials**
2. Update the `brandName` query parameter
3. Click **Send**

---

#### 3.8 Get Materials by Component Key

1. Go to **Material API** > **Get Materials by Component Key**
2. Update the component key as needed (e.g., `ROOF_MOUNTED`, `GRID_TIED`)
3. Click **Send**

---

#### 3.9 Update Material

1. Go to **Material API** > **Update Material**
2. Ensure `{{material_id}}` is set
3. Modify the request body
4. Click **Send**

**Response Status:** `200 OK`

---

#### 3.10 Deactivate Material

1. Go to **Material API** > **Deactivate Material**
2. Ensure `{{material_id}}` is set
3. Click **Send**

**Response Status:** `204 No Content`

---

## Environment Variables

The collection uses the following environment variables:

| Variable | Description | Auto-set |
|----------|-------------|----------|
| `base_url` | Application URL | No (default: `http://localhost:8080`) |
| `jwt_token` | JWT authentication token | Yes (after login) |
| `customer_id` | Last created customer ID | Yes (after create) |
| `company_customer_id` | Last created company customer ID | Yes (after create) |
| `material_id` | Last created material ID | Yes (after create) |
| `username` | Logged-in username | Yes (after login) |
| `role` | User role | Yes (after login) |

---

## Request/Response Examples

### Customer Response

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "customerType": "INDIVIDUAL",
  "name": "John Doe",
  "companyName": null,
  "phone": "+91-9876543210",
  "email": "john@example.com",
  "address": "123 Main Street",
  "city": "Mumbai",
  "state": "Maharashtra",
  "pincode": "400001",
  "gstNumber": "27AABCT1234A1Z0",
  "active": true,
  "createdAt": "2026-04-06T10:30:00Z",
  "createdBy": "550e8400-e29b-41d4-a716-446655440001",
  "sites": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440002",
      "siteLabel": "Residence",
      "address": "123 Main Street",
      "city": "Mumbai",
      "state": "Maharashtra",
      "pincode": "400001",
      "isDefault": true,
      "createdAt": "2026-04-06T10:30:00Z"
    }
  ]
}
```

### Material Response

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440003",
  "category": {
    "name": "PANEL",
    "label": "Solar Panel"
  },
  "componentKey": "ROOF_MOUNTED",
  "brandName": "Luminous",
  "modelName": "LSQUARE-550",
  "specification": "550W Monocrystalline Solar Panel",
  "unit": "Piece",
  "warranty": "25 Years",
  "hsnCode": "8541.40",
  "active": true,
  "createdAt": "2026-04-06T10:30:00Z",
  "createdBy": "550e8400-e29b-41d4-a716-446655440001"
}
```

---

## Common Issues & Solutions

### Issue: 401 Unauthorized

**Solution:** 
- First, run the **Login** request to get JWT token
- Verify token is saved in environment variables
- Check token hasn't expired

### Issue: 404 Not Found

**Solution:**
- Verify the resource ID (customer_id, material_id) exists
- Try getting all records first to confirm data exists

### Issue: 400 Bad Request

**Solution:**
- Check required fields in request body
- Validate enum values (CustomerType, MaterialCategory)
- Ensure proper JSON format

### Issue: Database Connection Error

**Solution:**
```bash
# Check PostgreSQL is running
docker-compose ps

# Restart if needed
docker-compose restart postgres
```

---

## API Documentation

### Customer Endpoints

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/v1/customers` | ✓ | Get all customers |
| GET | `/api/v1/customers/{id}` | ✓ | Get customer by ID |
| GET | `/api/v1/customers/search?name=...` | ✓ | Search customers by name |
| POST | `/api/v1/customers` | ✓ | Create new customer |
| PUT | `/api/v1/customers/{id}` | ✓ | Update customer |
| DELETE | `/api/v1/customers/{id}` | ✓ | Deactivate customer |

### Material Endpoints

| Method | Endpoint | Auth | Description |
|--------|----------|------|-------------|
| GET | `/api/v1/materials` | ✓ | Get all materials |
| GET | `/api/v1/materials/{id}` | ✓ | Get material by ID |
| GET | `/api/v1/materials/categories` | ✓ | Get all categories |
| GET | `/api/v1/materials/category/{category}` | ✓ | Get materials by category |
| GET | `/api/v1/materials/component/{componentKey}` | ✓ | Get materials by component |
| GET | `/api/v1/materials/search?brandName=...` | ✓ | Search materials |
| POST | `/api/v1/materials` | ✓ | Create new material |
| PUT | `/api/v1/materials/{id}` | ✓ | Update material |
| DELETE | `/api/v1/materials/{id}` | ✓ | Deactivate material |

---

## Tips & Best Practices

1. **Always authenticate first** - Run the Login request before other requests
2. **Use environment variables** - Makes requests reusable
3. **Test GET before other operations** - Verify data exists
4. **Check response codes** - 200/201 for success, 4xx for errors
5. **Save IDs from responses** - Use them for subsequent operations
6. **Validate input** - Follow request body structure
7. **Test pagination** - For GET all endpoints with many records

---

## Additional Resources

- [Solar ERP GitHub Repository](https://github.com/yourrepo)
- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Postman Documentation](https://learning.postman.com/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)

---

## Support

For issues or questions:
1. Check logs in terminal where Spring Boot is running
2. Verify PostgreSQL is running: `docker-compose ps`
3. Check Postman console for detailed error messages (Ctrl+Alt+C)

---

**Happy Testing! 🚀**

