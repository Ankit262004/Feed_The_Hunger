const mongoose = require('mongoose');

const userSchema = new mongoose.Schema({
    email: {
        required: true,
        type: String,
        unique: true,
        trim: true,
        match: [/^\S+@\S+\.\S+$/, 'Please enter a valid email address']
    },
    fullName: {
        required: true,
        type: String,
        trim: true
    },
    password: {
        required: true,
        type: String
    },
    location: {
        required: true,
        type: String,
        trim: true
    },
    userType: {
        required: true,
        type: String,
        enum: ['donor', 'receiver', 'admin', 'volunteer']
    }
}, { timestamps: true });

module.exports = mongoose.model('User', userSchema);
