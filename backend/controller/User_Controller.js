const express = require('express');
const router = express.Router();
const User = require('../model/User');
const bcrypt = require('bcrypt');
const jwt = require('jsonwebtoken');
require('dotenv').config();

// Welcome route
router.get('/', (req, res) => {
    res.send("Welcome to Food Donation User Service");
});

// Register a user
router.post('/registeruser', async (req, res) => {
    try {
        const { email, fullName, password, location, userType } = req.body;

        if (!email || !fullName || !password || !location || !userType) {
            return res.status(400).json({ error: 'All fields are required' });
        }

        const existingUser = await User.findOne({ email });
        if (existingUser) {
            return res.status(400).json({ error: 'Email already exists' });
        }

        const hashedPassword = await bcrypt.hash(password, 10);

        const newUser = new User({
            email,
            fullName,
            password: hashedPassword,
            location,
            userType
        });

        const savedUser = await newUser.save();
        res.status(201).json({ ...savedUser._doc, password: undefined });

    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

router.post('/login', async (req, res) => {
    try {
        const { email, password } = req.body;

        if (!email || !password) {
            return res.status(400).json({ message: "Email and password are required", success: false });
        }

        const user = await User.findOne({ email });
        if (!user) {
            return res.status(400).json({ message: "Incorrect email", success: false });
        }

        const isMatch = await bcrypt.compare(password, user.password);
        if (!isMatch) {
            return res.status(400).json({ message: "Incorrect password", success: false });
        }

        const token = jwt.sign({ userId: user._id }, process.env.SECRET_KEY, {
            expiresIn: '1d'
        });

        const userData = {
            email: user.email,
            fullName: user.fullName,
            location: user.location,
            userType: user.userType
        };

        res.status(200)
            .cookie('token', token, { maxAge: 24 * 60 * 60 * 1000, httpOnly: true })
            .json({
                message: `Welcome back ${user.fullName}`,
                user: userData,
                success: true
            });

    } catch (error) {
        res.status(500).json({ message: "Internal server error" });
    }
});

// Get user profile by ID
router.get('/profile/:id', async (req, res) => {
    try {
        const user = await User.findById(req.params.id);
        if (!user) return res.status(404).json({ message: 'User not found' });

        res.json({ ...user._doc, password: undefined, confirmPassword: undefined });
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
});

// Get all users
router.get('/getallusers', async (req, res) => {
    try {
        const users = await User.find();
        const filteredUsers = users.map(user => ({
            ...user._doc,
            password: undefined,
            confirmPassword: undefined
        }));
        res.json(filteredUsers);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

// Filter by name
router.get('/filterbyname/:name', async (req, res) => {
    try {
        const users = await User.find({ fullName: { $regex: req.params.name, $options: 'i' } });
        const filteredUsers = users.map(user => ({
            ...user._doc,
            password: undefined,
            confirmPassword: undefined
        }));
        res.json(filteredUsers);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

// Filter by userType
router.get('/filterbyusertype/:type', async (req, res) => {
    try {
        const users = await User.find({ userType: req.params.type });
        const filteredUsers = users.map(user => ({
            ...user._doc,
            password: undefined,
            confirmPassword: undefined
        }));
        res.json(filteredUsers);
    } catch (err) {
        res.status(500).json({ error: err.message });
    }
});

// Update user
router.patch('/update/:id', async (req, res) => {
    try {
        const updatedData = req.body;

        if (updatedData.password) {
            updatedData.password = await bcrypt.hash(updatedData.password, 10);
            updatedData.confirmPassword = updatedData.password;
        }

        const updatedUser = await User.findByIdAndUpdate(req.params.id, updatedData, { new: true });

        res.json({ ...updatedUser._doc, password: undefined, confirmPassword: undefined });
    } catch (error) {
        res.status(400).json({ message: error.message });
    }
});

// Delete user
router.delete('/delete/:id', async (req, res) => {
    try {
        const user = await User.findByIdAndDelete(req.params.id);
        if (!user) return res.status(404).json({ message: "User not found" });

        res.send(`User ${user.fullName} has been deleted.`);
    } catch (error) {
        res.status(400).json({ message: error.message });
    }
});

module.exports = router;
