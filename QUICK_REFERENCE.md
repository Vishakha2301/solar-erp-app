# Solar ERP API - Quick Reference Card

## 🚀 Quick Start (3 Steps)

### 1. Start Database & Application
```bash
cd D:\SolarApp\solar-erp-app
setup-and-run.bat
```

### 2. Open Postman
- Import: `Solar_ERP_Postman_Collection.json`
- Base URL: `http://localhost:8080`

### 3. Test API
- Run "Authentication > Login" first
- Then test other endpoints

---

## 🔑 Default Login
```
Username: admin
Password: admin
```

---

## 📋 Customer API Quick Reference

### Create Individual Customer
```
POST /api/v1/customers
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
      "siteLabel": "Home",
      "address": "123 Main Street",
      "city": "Mumbai",
      "state": "Maharashtra",
      "pincode": "400001",
      "isDefault": true
    }
  ]
}
```

### Create Company Customer
```
POST /api/v1/customers
{
  "customerType": "COMPANY",
  "name": "Solar Solutions Ltd",
  "companyName": "Solar Solutions Ltd",
  "phone": "+91-2240123456",
  "email": "info@solarsolutions.com",
  "address": "Corporate Office",
  "city": "Mumbai",
  "state": "Maharashtra",
  "pincode": "400001",
  "gstNumber": "27AABCS1234A1Z0",
  "sites": [
    {
      "siteLabel": "Head Office",
      "address": "Corporate Office",
      "city": "Mumbai",
      "state": "Maharashtra",
      "pincode": "400001",
      "isDefault": true
    }
  ]
}
```

### Create Society Customer
```
POST /api/v1/customers
{
  "customerType": "SOCIETY",
  "name": "Green Valley Housing Society",
  "companyName": "Green Valley Housing Society",
  "phone": "+91-2234567890",
  "email": "admin@greenvalley.com",
  "address": "Society Complex",
  "city": "Thane",
  "state": "Maharashtra",
  "pincode": "400605",
  "gstNumber": "27AABCS5678B1Z0",
  "sites": [...]
}
```

### All Customer Endpoints
| Operation | Endpoint | Method |
|-----------|----------|--------|
| Get All | `/api/v1/customers` | GET |
| Get By ID | `/api/v1/customers/{id}` | GET |
| Search | `/api/v1/customers/search?name=John` | GET |
| Create | `/api/v1/customers` | POST |
| Update | `/api/v1/customers/{id}` | PUT |
| Delete | `/api/v1/customers/{id}` | DELETE |

---

## 📦 Material API Quick Reference

### Material Categories
- `PANEL` - Solar Panels
- `INVERTER` - Inverters
- `CABLE` - Cables
- `STRUCTURE` - Mounting Structures
- `ELECTRICAL` - Electrical Components
- `OTHER` - Other

### Create Solar Panel
```
POST /api/v1/materials
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

### Create Inverter
```
POST /api/v1/materials
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

### Create Cable
```
POST /api/v1/materials
{
  "category": "CABLE",
  "componentKey": "SOLAR_CABLE",
  "brandName": "Havells",
  "modelName": "PV Rated Cable 4MM",
  "specification": "4mm² DC Solar Cable",
  "unit": "Meter",
  "warranty": "5 Years",
  "hsnCode": "8544.30"
}
```

### All Material Endpoints
| Operation | Endpoint | Method |
|-----------|----------|--------|
| Get All | `/api/v1/materials` | GET |
| Get By ID | `/api/v1/materials/{id}` | GET |
| Get Categories | `/api/v1/materials/categories` | GET |
| Get By Category | `/api/v1/materials/category/{category}` | GET |
| Get By Component | `/api/v1/materials/component/{key}` | GET |
| Search | `/api/v1/materials/search?brandName=...` | GET |
| Create | `/api/v1/materials` | POST |
| Update | `/api/v1/materials/{id}` | PUT |
| Delete | `/api/v1/materials/{id}` | DELETE |

---

## ☀️ Sizing API Quick Reference

### Create Sizing Estimate (Known Monthly Average)
```
POST /api/v1/sizing/estimates
{
  "customerId": "<uuid>",
  "state": "Rajasthan",
  "availableRoofAreaSqm": 100,
  "panelWattageWp": 550,
  "connectionType": "RESIDENTIAL",
  "phaseType": "SINGLE",
  "knownMonthlyAverageKwh": 350,
  "growthBufferPercent": 10
}
```

### Create Sizing Estimate (Monthly Readings)
```
POST /api/v1/sizing/estimates
{
  "customerId": "<uuid>",
  "state": "Gujarat",
  "availableRoofAreaSqm": 150,
  "panelWattageWp": 550,
  "connectionType": "COMMERCIAL",
  "phaseType": "THREE",
  "monthlyConsumptionReadingsKwh": [400, 420, 380, 350, 300, 280, 290, 310, 360, 410, 430, 390],
  "growthBufferPercent": 15
}
```

### All Sizing Endpoints
| Operation | Endpoint | Method |
|-----------|----------|--------|
| Get All | `/api/v1/sizing/estimates` | GET |
| Get By ID | `/api/v1/sizing/estimates/{id}` | GET |
| By Customer | `/api/v1/sizing/estimates/customer/{customerId}` | GET |
| Create | `/api/v1/sizing/estimates` | POST |
| Convert to Costing | `/api/v1/sizing/estimates/{id}/costing` | POST |
| Delete | `/api/v1/sizing/estimates/{id}` | DELETE |

### Connection Types
- `RESIDENTIAL` — household (default)
- `COMMERCIAL` — commercial premises
- `INDUSTRIAL` — industrial load

### Phase Types
- `SINGLE` — single-phase connection
- `THREE` — three-phase connection

### Supported States
Rajasthan, Gujarat, Madhya Pradesh, Maharashtra, Karnataka, Tamil Nadu, Andhra Pradesh, Telangana, Uttar Pradesh, Bihar, West Bengal, Odisha, Punjab, Haryana

> **Note:** Material catalog must have `unitPrice` and `gstRate` populated for the BOM to show indicative costs. The `indicativeMaterialCost` in the response is ex-GST.

---

## 🧪 Testing Sequence

1. **Login** → Get JWT token
2. **Create Customer** (Individual/Company/Society)
3. **Get All Customers** → Verify created customer
4. **Search Customers** → Search by name
5. **Get Material Categories** → List all categories
6. **Create Materials** (Panel, Inverter, Cable, etc.) — set `unitPrice` + `gstRate` for sizing BOM
7. **Get All Materials** → Verify created materials
8. **Search Materials** → Search by brand name
9. **Create Sizing Estimate** → Verify BOM and recommended capacity
10. **Convert Estimate to Costing** → Opens costing form pre-filled with material subtotal
11. **Update Customer/Material** → Test update operation
12. **Delete (Deactivate)** → Test deactivation

---

## 🔒 Authentication

All endpoints (except `/api/v1/auth/login`) require JWT token in header:

```
Authorization: Bearer <jwt_token>
```

**Auto-saved in Postman as:** `{{jwt_token}}`

---

## 📊 HTTP Status Codes

| Code | Meaning |
|------|---------|
| 200 | Success (GET, PUT) |
| 201 | Created (POST) |
| 204 | No Content (DELETE) |
| 400 | Bad Request |
| 401 | Unauthorized |
| 404 | Not Found |
| 500 | Server Error |

---

## 🐛 Troubleshooting

### PostgreSQL not starting
```bash
docker-compose ps
docker-compose logs postgres
docker-compose restart postgres
```

### Spring Boot won't start
```bash
# Check logs
mvn clean install -DskipTests
mvn spring-boot:run -pl solar-erp-app
```

### JWT token expired
→ Run Login request again to get new token

### 404 Resource Not Found
→ Verify resource ID exists (create it first if needed)

### 400 Bad Request
→ Check JSON format, required fields, enum values

---

## 📱 Environment Variables in Postman

Set these automatically or manually:

```
base_url = http://localhost:8080
jwt_token = (auto-set after login)
customer_id = (auto-set after create customer)
material_id = (auto-set after create material)
```

---

## 💡 Tips

- Always login first
- Use environment variables `{{variable_name}}`
- Check Postman Console for detailed errors (Ctrl+Alt+C)
- Look at response for errors and data structure
- Save IDs from responses for subsequent operations

---

**For detailed guide, see:** `POSTMAN_TESTING_GUIDE.md`

---

**Happy Testing! 🎉**

