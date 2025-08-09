const express = require('express');
const router = express.Router();
const Food = require('../model/Food');
const User = require('../model/User');
const upload = require('../middleware/multer');

// Home route
router.get('/', (req, res) => {
    res.send("Welcome to Food Donation - Food Service");
});

// Add food (Donor only)
router.post('/add', upload.single('image'), async (req, res) => {
    try {
        const { foodName, quantity, expiryDate, foodType, description, donorId, location } = req.body;

        if (!req.file) {
            return res.status(400).json({ error: 'Image file is required' });
        }

        const newFood = new Food({
            foodName,
            quantity,
            expiryDate,
            foodType,
            description,
            image: req.file.filename,
            donorId,
            location
        });

        const savedFood = await newFood.save();
        res.status(201).json(savedFood);
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// Get all food
router.get('/getall', async (req, res) => {
    try {
        const foods = await Food.find().populate('donorId', 'fullName email');
        const result = foods.map(food => ({
            ...food._doc,
            image: `${req.protocol}://${req.get('host')}/uploads/${food.image}`
        }));
        res.json(result);
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// Get food by ID
router.get('/get/:id', async (req, res) => {
    try {
        const food = await Food.findById(req.params.id).populate('donorId', 'fullName email');
        if (!food) return res.status(404).json({ message: 'Food not found' });

        res.json({
            ...food._doc,
            image: `${req.protocol}://${req.get('host')}/uploads/${food.image}`
        });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// Update food
router.patch('/update/:id', upload.single('image'), async (req, res) => {
    try {
        const updates = { ...req.body };
        if (req.file) {
            updates.image = req.file.filename;
        }

        const updated = await Food.findByIdAndUpdate(req.params.id, updates, { new: true });
        if (!updated) return res.status(404).json({ message: 'Food not found' });

        res.json(updated);
    } catch (error) {
        res.status(400).json({ error: error.message });
    }
});

// Delete food
router.delete('/delete/:id', async (req, res) => {
    try {
        const deleted = await Food.findByIdAndDelete(req.params.id);
        if (!deleted) return res.status(404).json({ message: 'Food not found' });

        res.send(`Food "${deleted.foodName}" deleted successfully.`);
    } catch (error) {
        res.status(400).json({ error: error.message });
    }
});

// Filter by foodType
router.get('/filterbytype/:type', async (req, res) => {
    try {
        const type = req.params.type.toLowerCase();
        const allowed = ['vegetarian', 'non-vegetarian', 'both'];
        if (!allowed.includes(type)) {
            return res.status(400).json({ error: 'Invalid food type' });
        }

        const foods = await Food.find({ foodType: type });
        const result = foods.map(f => ({
            ...f._doc,
            image: `${req.protocol}://${req.get('host')}/uploads/${f.image}`
        }));
        res.json(result);
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// Match food to receiver based on preference
router.get('/match/:userId', async (req, res) => {
    try {
        const user = await User.findById(req.params.userId);
        if (!user || user.userType !== 'receiver') {
            return res.status(400).json({ error: 'Invalid receiver user' });
        }

        const pref = user.foodPreference;
        const query = pref === 'both' ? {} : { foodType: { $in: [pref, 'both'] } };

        const foods = await Food.find(query).populate('donorId', 'fullName email');
        const result = foods.map(f => ({
            ...f._doc,
            image: `${req.protocol}://${req.get('host')}/uploads/${f.image}`
        }));
        res.json(result);
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

// Expire outdated food
router.patch('/expire', async (req, res) => {
    try {
        const now = new Date();
        const result = await Food.updateMany(
            { expiryDate: { $lt: now }, status: 'available' },
            { $set: { status: 'expired' } }
        );
        res.json({ message: `${result.modifiedCount} food items marked as expired.` });
    } catch (error) {
        res.status(500).json({ error: error.message });
    }
});

module.exports = router;
