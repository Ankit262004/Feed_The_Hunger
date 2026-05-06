const express = require('express');
const router = express.Router();
const Food = require('../model/Food');
const upload = require('../middleware/multer');
const admin = require('../firebaseadmin');
const User = require('../model/User');

// =========================
// GET ALL FOOD
// =========================
router.get('/', async (req, res) => {
    try {
        const foods = await Food.find();

        const result = foods.map(food => ({
            ...food._doc,
            image: food.image
                ? `${req.protocol}://${req.get('host')}/uploads/${food.image}`
                : null
        }));

        res.json(result);
    } catch (error) {
        console.error("GET ALL ERROR:", error);
        res.status(500).json({ error: error.message });
    }
});

// =========================
// ADD FOOD + NOTIFICATION TO VOLUNTEERS
// =========================
router.post('/add', upload.single('image'), async (req, res) => {
    try {
        console.log("BODY:", req.body);
        console.log("FILE:", req.file);

        let {
            userId,
            foodName,
            quantity,
            expiryDate,
            foodType,
            description,
            location,
            latitude,
            longitude
        } = req.body;

        if (!userId) {
            return res.status(400).json({
                success: false,
                message: "userId is required"
            });
        }

        if (!foodName || !quantity || !expiryDate || !foodType || !location) {
            return res.status(400).json({
                success: false,
                message: "foodName, quantity, expiryDate, foodType, location are required"
            });
        }

        if (latitude === undefined || longitude === undefined || latitude === "" || longitude === "") {
            return res.status(400).json({
                success: false,
                message: "latitude and longitude are required"
            });
        }

        if (!req.file) {
            return res.status(400).json({
                success: false,
                message: "Image is required"
            });
        }

        quantity = Number(quantity);
        if (isNaN(quantity) || quantity <= 0) {
            return res.status(400).json({
                success: false,
                message: "Quantity must be a valid number"
            });
        }

        latitude = Number(latitude);
        longitude = Number(longitude);

        if (isNaN(latitude) || isNaN(longitude)) {
            return res.status(400).json({
                success: false,
                message: "latitude and longitude must be valid numbers"
            });
        }

        const parsedDate = new Date(expiryDate);
        if (isNaN(parsedDate.getTime())) {
            return res.status(400).json({
                success: false,
                message: "Invalid expiryDate format"
            });
        }

        foodType = foodType.toLowerCase().trim();
        if (!["veg", "nonveg", "both"].includes(foodType)) {
            return res.status(400).json({
                success: false,
                message: "foodType must be veg, nonveg, or both"
            });
        }

        const newFood = new Food({
            donorId: userId,
            foodName,
            quantity,
            expiryDate: parsedDate,
            foodType,
            description: description || "",
            location: location.trim(),
            latitude,
            longitude,
            image: req.file.filename,
            status: "pending"
        });

        const savedFood = await newFood.save();

        const uploader = await User.findById(userId);

        if (uploader) {
            const uploaderType = (uploader.userType || "").trim().toLowerCase();

            console.log("Uploader userType:", uploader.userType);

            let targetUsers = [];

            // ✅ Donor uploads food -> notify ONLY volunteers
            if (uploaderType === "donor") {
                targetUsers = await User.find({
                    userType: "volunteer",
                    fcmToken: { $ne: null }
                });
            }

            console.log("Target volunteers found:", targetUsers.length);

            const tokens = targetUsers
                .map(user => user.fcmToken)
                .filter(token => typeof token === "string" && token.trim().length > 0);

            console.log("Filtered volunteer tokens:", tokens);

            if (tokens.length > 0) {
                try {
                    const response = await admin.messaging().sendEachForMulticast({
                        tokens,
                        notification: {
                            title: "New Food Available",
                            body: `${foodName} at ${location}`
                        },
                        data: {
                            type: "new_food",
                            foodId: savedFood._id.toString(),
                            latitude: savedFood.latitude.toString(),
                            longitude: savedFood.longitude.toString(),
                            location: savedFood.location
                        }
                    });

                    console.log("✅ ADD notification sent:", response);
                } catch (err) {
                    console.error("❌ ADD FCM ERROR:", err);
                }
            } else {
                console.log("⚠️ No valid volunteer FCM tokens found");
            }
        } else {
            console.log("⚠️ Uploader not found");
        }

        res.status(201).json({
            success: true,
            message: "Food added successfully",
            data: {
                ...savedFood._doc,
                image: savedFood.image
                    ? `${req.protocol}://${req.get('host')}/uploads/${savedFood.image}`
                    : null
            }
        });

    } catch (error) {
        console.error("ADD FOOD ERROR:", error);
        res.status(500).json({
            success: false,
            message: "Internal Server Error",
            error: error.message
        });
    }
});

// =========================
// USER PROFILE + TOTAL UPLOADED
// =========================
router.get('/profile/:userId', async (req, res) => {
    try {
        const userId = req.params.userId;

        const user = await User.findById(userId)
            .select("fullName email location userType");

        const totalUploaded = await Food.countDocuments({
            donorId: userId
        });

        res.json({
            success: true,
            user,
            totalUploaded
        });

    } catch (error) {
        console.error("PROFILE ERROR:", error);
        res.status(500).json({ error: error.message });
    }
});

// =========================
// FILTER FOOD BY TYPE
// =========================
router.get('/type/:type', async (req, res) => {
    try {
        const type = req.params.type.toLowerCase();

        const foods = await Food.find({ foodType: type });

        const result = foods.map(food => ({
            ...food._doc,
            image: food.image
                ? `${req.protocol}://${req.get('host')}/uploads/${food.image}`
                : null
        }));

        res.json(result);

    } catch (error) {
        console.error("FILTER TYPE ERROR:", error);
        res.status(500).json({ error: error.message });
    }
});

// =========================
// AUTO EXPIRE FOOD
// =========================
router.patch('/expire', async (req, res) => {
    try {
        const now = new Date();

        const result = await Food.updateMany(
            { expiryDate: { $lt: now }, status: 'pending' },
            { $set: { status: 'expired' } }
        );

        res.json({
            success: true,
            message: `${result.modifiedCount} items expired`
        });

    } catch (error) {
        console.error("EXPIRE ERROR:", error);
        res.status(500).json({ error: error.message });
    }
});

// =========================
// UPDATE STATUS + NOTIFY DONOR
// =========================
router.patch('/status/:id', async (req, res) => {
    try {
        const { status, volunteerId } = req.body;

        if (!["accepted", "rejected", "pending", "expired"].includes(status)) {
            return res.status(400).json({
                success: false,
                message: "Invalid status"
            });
        }

        const updateData = { status };

        if ((status === "accepted" || status === "rejected") && volunteerId) {
            updateData.handledBy = volunteerId;
            updateData.handledAt = new Date();
        }

        if (status === "pending") {
            updateData.handledBy = null;
            updateData.handledAt = null;
        }

        const updatedFood = await Food.findByIdAndUpdate(
            req.params.id,
            updateData,
            { new: true }
        );

        if (!updatedFood) {
            return res.status(404).json({
                success: false,
                message: "Food not found"
            });
        }

        if (status === "accepted" || status === "rejected") {
            const donor = await User.findById(updatedFood.donorId);

            if (donor && donor.fcmToken && donor.fcmToken.trim() !== "") {
                const title = status === "accepted" ? "Food Accepted" : "Food Rejected";
                const body = status === "accepted"
                    ? `Your food "${updatedFood.foodName}" was accepted`
                    : `Your food "${updatedFood.foodName}" was rejected`;

                console.log("STATUS TOKEN:", donor.fcmToken);

                try {
                    const response = await admin.messaging().send({
                        token: donor.fcmToken,
                        notification: {
                            title,
                            body
                        },
                        data: {
                            type: status,
                            foodId: updatedFood._id.toString()
                        }
                    });

                    console.log("✅ STATUS notification sent:", response);
                } catch (err) {
                    console.error("❌ STATUS FCM ERROR:", err);
                }
            } else {
                console.log("⚠️ No valid donor FCM token for status notification");
            }
        }

        res.json({
            success: true,
            message: "Status updated successfully",
            data: updatedFood
        });

    } catch (error) {
        console.error("STATUS ERROR:", error);
        res.status(500).json({ error: error.message });
    }
});

// =========================
// DELETE FOOD + NOTIFY DONOR
// =========================
router.delete('/delete/:id', async (req, res) => {
    try {
        const deletedBy = req.body.deletedBy || req.query.deletedBy;

        const food = await Food.findById(req.params.id);

        if (!food) {
            return res.status(404).json({
                success: false,
                message: "Food not found"
            });
        }

        const donorId = food.donorId;
        const foodName = food.foodName;
        const foodId = food._id.toString();

        let deletedByText = "volunteer";

        if (deletedBy === "admin") {
            deletedByText = "admin";
        } else if (deletedBy === "volunteer") {
            deletedByText = "volunteer";
        }

        await Food.findByIdAndDelete(req.params.id);

        const donor = await User.findById(donorId);

        if (donor && donor.fcmToken && donor.fcmToken.trim() !== "") {
            console.log("DELETE TOKEN:", donor.fcmToken);
            console.log("DELETED BY:", deletedByText);

            try {
                const response = await admin.messaging().send({
                    token: donor.fcmToken,
                    notification: {
                        title: "Request Deleted",
                        body: `Your food "${foodName}" was deleted by ${deletedByText}`
                    },
                    data: {
                        type: "deleted",
                        foodId,
                        deletedBy: deletedByText
                    }
                });

                console.log("✅ DELETE notification sent:", response);
            } catch (err) {
                console.error("❌ DELETE FCM ERROR:", err);
            }
        } else {
            console.log("⚠️ No valid donor FCM token for delete notification");
        }

        res.json({
            success: true,
            message: "Food deleted successfully"
        });

    } catch (error) {
        console.error("DELETE ERROR:", error);
        res.status(500).json({
            success: false,
            error: error.message
        });
    }
});

// =========================
// GET ACCEPTED FOOD FOR VOLUNTEER
// =========================
router.get('/accepted/:volunteerId', async (req, res) => {
    try {
        const { volunteerId } = req.params;

        const foods = await Food.find({
            status: 'accepted',
            handledBy: volunteerId
        });

        const result = foods.map(food => ({
            ...food._doc,
            image: food.image
                ? `${req.protocol}://${req.get('host')}/uploads/${food.image}`
                : null
        }));

        res.json(result);

    } catch (error) {
        console.error("ACCEPTED FOOD ERROR:", error);
        res.status(500).json({ error: error.message });
    }
});

// =========================
// GET FOOD BY ID
// =========================
router.get('/:id', async (req, res) => {
    try {
        const food = await Food.findById(req.params.id);

        if (!food) {
            return res.status(404).json({
                success: false,
                message: "Food not found"
            });
        }

        res.json({
            ...food._doc,
            image: food.image
                ? `${req.protocol}://${req.get('host')}/uploads/${food.image}`
                : null
        });

    } catch (error) {
        console.error("GET BY ID ERROR:", error);
        res.status(500).json({ error: error.message });
    }
});

module.exports = router;