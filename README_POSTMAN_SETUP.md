# Solar ERP - Postman Testing Setup Summary

## 📁 Files Created

I've created the following files to help you test the Customer and Material APIs:

### 1. **Solar_ERP_Postman_Collection.json**
   - Complete Postman collection with all API endpoints
   - Includes authentication, customer, and material endpoints
   - Pre-configured with environment variables
   - Auto-saving of JWT tokens and resource IDs

### 2. **Solar_ERP_Environment.json**
   - Postman environment configuration
   - Default values for base URL and credentials
   - Import this alongside the collection for better organization

### 3. **POSTMAN_TESTING_GUIDE.md**
   - Comprehensive step-by-step testing guide
   - Setup instructions
   - Detailed API documentation
   - Troubleshooting section
   - Example requests and responses

### 4. **QUICK_REFERENCE.md**
   - Quick start guide
   - Sample request bodies for common operations
   - HTTP status codes reference
   - Troubleshooting tips

### 5. **setup-and-run.bat**
   - Windows batch script to automatically:
     - Start PostgreSQL using Docker
     - Build the Spring Boot application
     - Run the application
   - Just double-click to run!

---

## 🚀 Getting Started (3 Easy Steps)

### Step 1: Setup & Start Application
```bash
cd D:\SolarApp\solar-erp-app
setup-and-run.bat
```
This will:
- Start PostgreSQL container
- Build the project
- Run Spring Boot application on http://localhost:8080

### Step 2: Import Postman Collection
1. Open Postman
2. Click **Import** button
3. Select `Solar_ERP_Postman_Collection.json`
4. (Optional) Import `Solar_ERP_Environment.json` for environment configuration

### Step 3: Start Testing
1. Go to **Authentication** → **Login**
2. Click **Send** (uses default admin/admin)
3. JWT token auto-saves
4. Test other endpoints!

---

## 📚 API Endpoints Overview

### Authentication
```
POST /api/v1/auth/login
- No authentication required
- Returns JWT token
```

### Customer API
```
GET    /api/v1/customers                    - Get all customers
GET    /api/v1/customers/{id}               - Get customer by ID
GET    /api/v1/customers/search?name=...    - Search customers
POST   /api/v1/customers                    - Create customer
PUT    /api/v1/customers/{id}               - Update customer
DELETE /api/v1/customers/{id}               - Deactivate customer
```

### Material API
```
GET    /api/v1/materials                    - Get all materials
GET    /api/v1/materials/{id}               - Get material by ID
GET    /api/v1/materials/categories         - Get all categories
GET    /api/v1/materials/category/{cat}     - Get by category
GET    /api/v1/materials/component/{key}    - Get by component key
GET    /api/v1/materials/search?brand=...   - Search materials
POST   /api/v1/materials                    - Create material
PUT    /api/v1/materials/{id}               - Update material
DELETE /api/v1/materials/{id}               - Deactivate material
```

---

## 🔑 Default Credentials
```
Username: admin
Password: admin
```

---

## 📊 Customer Types
- `INDIVIDUAL` - Single person
- `COMPANY` - Business entity  
- `SOCIETY` - Residential society

---

## 📦 Material Categories
- `PANEL` - Solar Panels
- `INVERTER` - Inverters
- `CABLE` - Cables
- `STRUCTURE` - Mounting Structures
- `ELECTRICAL` - Electrical Components
- `OTHER` - Other materials

---

## 🧪 Recommended Testing Sequence

1. **Authentication**
   - Login → Get JWT token

2. **Customer API**
   - Create Individual Customer
   - Create Company Customer
   - Get All Customers
   - Search Customers
   - Get Customer by ID
   - Update Customer
   - Deactivate Customer

3. **Material API**
   - Get Material Categories
   - Create Solar Panel
   - Create Inverter
   - Create Cable
   - Get All Materials
   - Search Materials
   - Get by Category
   - Update Material
   - Deactivate Material

---

## ✨ Key Features of the Collection

✅ **Automatic JWT Token Management**
   - Login once, token auto-saves for all requests
   - No need to manually copy-paste tokens

✅ **Auto-saving Resource IDs**
   - Create a customer → ID auto-saves to `{{customer_id}}`
   - Create a material → ID auto-saves to `{{material_id}}`
   - Use these in subsequent operations

✅ **Pre-configured URLs**
   - Base URL: `{{base_url}}` → http://localhost:8080
   - All endpoints use variables for easy switching

✅ **Sample Data**
   - Multiple examples for different customer types
   - Various material categories with realistic data
   - Ready-to-use request bodies

✅ **Test Scripts**
   - Auto-validates responses
   - Saves data to environment for next requests
   - Error handling built-in

---

## 🐳 Docker Setup

### Start PostgreSQL
```bash
cd D:\SolarApp\solar-erp-app\solar-erp-app
docker-compose up -d
```

### Check Status
```bash
docker-compose ps
```

### View Logs
```bash
docker-compose logs postgres
```

### Stop PostgreSQL
```bash
docker-compose down
```

---

## 📋 Database Details
- **Host**: localhost
- **Port**: 5432
- **Database**: solar_erp
- **Username**: postgres
- **Password**: postgres

---

## 🛠️ Manual Setup (Without Batch Script)

### 1. Start PostgreSQL
```bash
cd D:\SolarApp\solar-erp-app\solar-erp-app
docker-compose up -d
```

### 2. Build Project
```bash
cd D:\SolarApp\solar-erp-app
mvn clean install -DskipTests
```

### 3. Run Application
```bash
mvn spring-boot:run -pl solar-erp-app
```

### 4. Access Application
```
http://localhost:8080
```

---

## 🔍 Postman Tips

### View Console for Errors
- Press `Ctrl + Alt + C` to open Postman Console
- See detailed request/response information

### Save Environment Variables
- Click ⚙️ environment icon
- Add/edit variables as needed

### Pre-request Scripts
- Click "Pre-request Script" tab to add scripts
- Run before each request (e.g., timestamp generation)

### Post-response Scripts
- Click "Tests" tab (response) to see auto-save logic
- Modify to customize behavior

---

## ❓ FAQ

**Q: Do I need to run setup-and-run.bat?**
A: No, but it's convenient. You can also manually start Docker and Maven.

**Q: What if PostgreSQL is already running?**
A: docker-compose will try to use existing instance, which is fine.

**Q: Can I test from a different machine?**
A: Yes, change `base_url` to your machine IP (e.g., `http://192.168.1.100:8080`)

**Q: How long is JWT token valid?**
A: Check your application configuration (default is usually several hours)

**Q: Can I modify request bodies?**
A: Yes! Postman bodies are fully editable. Adjust as needed.

---

## 📞 Support & Troubleshooting

### Common Issues

**PostgreSQL Connection Error**
```bash
# Check if running
docker-compose ps
# Restart if needed
docker-compose restart postgres
```

**Maven Build Failure**
```bash
# Clean build
mvn clean install -DskipTests -X
```

**Port 8080 Already in Use**
```
Kill process or modify application.yaml port
```

**JWT Token Expired**
→ Run Login endpoint again to get new token

---

## 📖 Documentation Files

- **POSTMAN_TESTING_GUIDE.md** - Detailed guide (⭐ Start here for comprehensive info)
- **QUICK_REFERENCE.md** - Quick reference card (⭐ Quick lookup)
- This file - Overview and setup summary

---

## ✅ Verification Checklist

Before testing:

- [ ] Docker installed and running
- [ ] Java 21+ installed
- [ ] PostgreSQL container started (`docker-compose up -d`)
- [ ] Spring Boot app running on `http://localhost:8080`
- [ ] Postman collection imported
- [ ] Environment variables configured
- [ ] Login endpoint tested successfully
- [ ] JWT token received and saved

---

## 🎯 Next Steps

1. **Import Collection** in Postman
2. **Run setup-and-run.bat** or manually start app
3. **Test Login** endpoint
4. **Follow POSTMAN_TESTING_GUIDE.md** for detailed testing
5. **Reference QUICK_REFERENCE.md** for quick lookups

---

**All files are ready! Start with Step 1 above and you'll be testing in minutes! 🚀**

---

## 📝 Notes

- Postman collection includes all request/response examples
- Environment variables auto-populate after first requests
- Test scripts automatically save IDs for next operations
- All endpoints require JWT authentication (except login)
- Default admin account works out of the box

---

**Happy Testing! 🎉**

