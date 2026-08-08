require('dotenv').config();

// ✅ KEEP MAIL LOGS
console.log("MAIL_USER:", process.env.MAIL_USER);
console.log("MAIL_PASS:", process.env.MAIL_PASS ? "Loaded" : "Missing");

const express = require('express');
const mongoose = require('mongoose');
const fs = require('fs');
const path = require('path');
const cors = require('cors');

const app = express();

// ===================== MIDDLEWARE =====================
app.use(cors());
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

// ===================== CREATE UPLOADS FOLDER =====================
const uploadsDir = path.join(__dirname, 'uploads');

if (!fs.existsSync(uploadsDir)) {
    fs.mkdirSync(uploadsDir, { recursive: true });
    console.log("📁 Uploads folder created");
}

// ===================== SERVE STATIC FILES =====================
app.use('/uploads', express.static(uploadsDir));

// ===================== MONGODB CONNECTION =====================
const mongoString = process.env.DATABASE_URL;

if (!mongoString) {
    console.error("❌ DATABASE_URL is missing in .env file");
    process.exit(1);
}

mongoose.connect(mongoString)
    .then(() => {
        console.log("✅ MongoDB Connected Successfully");
    })
    .catch((error) => {
        console.error("❌ MongoDB Connection Error:", error);
        process.exit(1);
    });

// ===================== ROUTES =====================
app.get('/', (req, res) => {
    res.send("🚀 Food Donation API Running Successfully");
});

// Controllers
const userController = require('./controller/User_Controller');
const foodController = require('./controller/food_controller');

// Route Mounting
app.use('/user', userController);
app.use('/food', foodController);

// ===================== 404 HANDLER =====================
app.use((req, res) => {
    res.status(404).json({
        success: false,
        message: "Route not found"
    });
});

// ===================== START SERVER =====================
const PORT = process.env.PORT || 3000;
const HOST = '0.0.0.0';

app.listen(PORT, HOST, () => {
    console.log(`🚀 Server running at: http://${HOST}:${PORT}`);
});