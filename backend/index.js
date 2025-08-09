// index.js
require('dotenv').config();

const express = require('express');
const mongoose = require('mongoose');
const fs = require('fs');
const app = express();
app.use(express.urlencoded({ extended: true }));
// Ensure uploads folder exists
const uploadsDir = './uploads';
if (!fs.existsSync(uploadsDir)) {
    fs.mkdirSync(uploadsDir);
}

// MongoDB Connection
const mongoString = process.env.DATABASE_URL;
mongoose.connect(mongoString, { useNewUrlParser: true, useUnifiedTopology: true });

const database = mongoose.connection;
database.on('error', (error) => {
    console.error(" MongoDB Connection Error:", error);
});
database.once('connected', () => {
    console.log(' Database Connected');
});

// Middleware
app.use(express.json());
app.use(express.urlencoded({ extended: true }));
app.use('/uploads', express.static('uploads')); // serve uploaded files

// Routes
app.get('/', (req, res) => {
    res.send(" Welcome to the Food Donation Home Page!");
});

//Import controllers (correct paths)
const userController = require('./controller/User_Controller');
const foodController = require('./controller/food_controller');

//  Use routes
app.use('/user', userController);
app.use('/food', foodController);

// Start Server
const PORT = process.env.PORT || 3000;
app.listen(PORT, () => {
    console.log(` Server started at: http://localhost:${PORT}`);
});
