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
        enum: ['vegetarian', 'non-vegetarian', 'both']
    },
    description: {
        type: String,
        maxlength: 300,
        trim: true
    },
    image: {
        type: String,
        required: true
    },
    donorId: {
        type: mongoose.Schema.Types.ObjectId,
        ref: 'User',
        required: true,
        validate: {
            validator: async function (value) {
                const user = await mongoose.model('User').findById(value);
                return user && user.userType === 'donor';
            },
            message: 'Invalid donorId: must reference a user with userType "donor".'
        }
    },
    location: {
        type: String,
        required: true,
        trim: true
    },
    status: {
        type: String,
        enum: ['available', 'claimed', 'expired'],
        default: 'available'
    },
    postedAt: {
        type: Date,
        default: Date.now
    }
}, { timestamps: true });

module.exports = mongoose.model('Food', foodSchema);
