const mongoose = require('mongoose');

const userSchema = new mongoose.Schema({

    // =========================================================
    // EMAIL
    // =========================================================

    email: {
        required: true,
        type: String,
        unique: true,
        trim: true,
       match: [/^\S+@\S+\.\S+$/, 'Please enter a valid email address']
    },

    // =========================================================
    // FULL NAME
    // =========================================================

    fullName: {
        required: true,
        type: String,
        trim: true
    },

    // =========================================================
    // PASSWORD
    // =========================================================

    password: {
        required: true,
        type: String
    },

    // =========================================================
    // LOCATION / ADDRESS
    // =========================================================

    location: {
        required: true,
        type: String,
        trim: true
    },

    // =========================================================
    // GPS LOCATION
    // =========================================================

    latitude: {
        type: Number,
        default: null
    },

    longitude: {
        type: Number,
        default: null
    },

    // =========================================================
    // USER TYPE
    // =========================================================

    userType: {
        required: true,
        type: String,
        enum: [
            'donor',
            'receiver',
            'admin',
            'volunteer'
        ]
    },

    // =========================================================
    // FCM TOKEN
    // =========================================================

    fcmToken: {
        type: String,
        default: null
    },

    // =========================================================
    // OTP RESET FIELDS
    // =========================================================

    resetOtp: {
        type: String,
        default: null
    },

    resetOtpExpiry: {
        type: Date,
        default: null
    },

    otpVerified: {
        type: Boolean,
        default: false
    }

}, {
    timestamps: true
});

module.exports = mongoose.model('User', userSchema);