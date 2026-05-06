const mongoose = require('mongoose');

const foodSchema = new mongoose.Schema({
    foodName: {
        type: String,
        required: true,
        trim: true
    },
    quantity: {
        type: Number,
        required: true,
        min: [1, 'Quantity must be at least 1']
    },
    expiryDate: {
        type: Date,
        required: true
    },
    foodType: {
        type: String,
        required: true,
        enum: ['veg', 'nonveg', 'both']
    },
    description: {
        type: String,
        maxlength: 300,
        trim: true,
        default: ""
    },
    image: {
        type: String,
        required: true
    },
    donorId: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'User',
        required: true
    },

    // 📍 LOCATION (TEXT)
    location: {
        type: String,
        required: true,
        trim: true
    },

    // 📍 NEW: GPS COORDINATES
    latitude: {
        type: Number,
        required: true
    },
    longitude: {
        type: Number,
        required: true
    },

    status: {
        type: String,
        enum: ['pending', 'accepted', 'rejected', 'expired'],
        default: 'pending'
    },

    handledBy: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'User',
        default: null
    },

    handledAt: {
        type: Date,
        default: null
    },

    postedAt: {
        type: Date,
        default: Date.now
    }

}, { timestamps: true });

module.exports = mongoose.model('Food', foodSchema);