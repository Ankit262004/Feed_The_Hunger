const express = require('express');
const router = express.Router();
const User = require('../model/User');
const Food = require('../model/Food');
const bcrypt = require('bcrypt');
const jwt = require('jsonwebtoken');
const sendOtpEmail = require('../utils/sendMail');
require('dotenv').config();

// Temporary OTP store
const otpStore = new Map();

const createOtpKey = (email, userType) => `${email}_${userType}`;

const findOtpByEmail = (email) => {
    for (let [key, value] of otpStore.entries()) {
        if (key.startsWith(`${email}_`)) {
            return { otpKey: key, otpData: value };
        }
    }
    return { otpKey: null, otpData: null };
};

// ===================== HOME =====================
router.get('/', (req, res) => {
    return res.send("🚀 User Service Running Successfully");
});

// ===================== REGISTER USER =====================
router.post('/registeruser', async (req, res) => {
    try {
        console.log("📩 REQUEST BODY:", req.body);

        let { email, fullName, password, location, userType, fcmToken } = req.body;

        email = email?.trim().toLowerCase();
        fullName = fullName?.trim();
        password = password?.trim();
        location = location?.trim();
        userType = userType?.trim().toLowerCase();
        fcmToken = fcmToken?.trim();

        if (!email || !fullName || !password || !location || !userType) {
            return res.status(400).json({
                success: false,
                message: "All fields are required"
            });
        }

        const allowedTypes = ['donor', 'receiver'];
        if (!allowedTypes.includes(userType)) {
            return res.status(400).json({
                success: false,
                message: "Invalid userType. Only donor or receiver allowed."
            });
        }

        const existingUser = await User.findOne({ email, userType });

        if (existingUser) {
            return res.status(400).json({
                success: false,
                message: `${userType} already registered with this email`
            });
        }

        const hashedPassword = await bcrypt.hash(password, 10);

        const newUser = new User({
            email,
            fullName,
            password: hashedPassword,
            location,
            userType,
            fcmToken: fcmToken || null
        });

        const savedUser = await newUser.save();

        console.log("✅ USER REGISTERED:", savedUser._id.toString());

        return res.status(201).json({
            success: true,
            message: `${userType} registered successfully`,
            user: {
                _id: savedUser._id,
                email: savedUser.email,
                fullName: savedUser.fullName,
                location: savedUser.location,
                userType: savedUser.userType,
                fcmToken: savedUser.fcmToken
            }
        });

    } catch (error) {
        console.error("❌ REGISTER ERROR:", error);

        if (error.code === 11000) {
            return res.status(400).json({
                success: false,
                message: "Email already registered for this role"
            });
        }

        return res.status(500).json({
            success: false,
            message: "Internal server error",
            error: error.message
        });
    }
});

// ===================== VOLUNTEER REGISTER =====================
router.post('/volunteer/register', async (req, res) => {
    try {
        let { email, fullName, password, location, fcmToken } = req.body;

        email = email?.trim().toLowerCase();
        fullName = fullName?.trim();
        password = password?.trim();
        location = location?.trim();
        fcmToken = fcmToken?.trim();

        if (!email || !fullName || !password || !location) {
            return res.status(400).json({
                success: false,
                message: "All fields are required"
            });
        }

        const existingVolunteer = await User.findOne({
            email,
            userType: 'volunteer'
        });

        if (existingVolunteer) {
            return res.status(400).json({
                success: false,
                message: "Volunteer already registered with this email"
            });
        }

        const hashedPassword = await bcrypt.hash(password, 10);

        const newVolunteer = new User({
            email,
            fullName,
            password: hashedPassword,
            location,
            userType: 'volunteer',
            fcmToken: fcmToken || null
        });

        const savedVolunteer = await newVolunteer.save();

        return res.status(201).json({
            success: true,
            message: "Volunteer registered successfully",
            user: {
                _id: savedVolunteer._id,
                email: savedVolunteer.email,
                fullName: savedVolunteer.fullName,
                location: savedVolunteer.location,
                userType: savedVolunteer.userType,
                fcmToken: savedVolunteer.fcmToken
            }
        });

    } catch (error) {
        console.error("❌ VOLUNTEER REGISTER ERROR:", error);

        if (error.code === 11000) {
            return res.status(400).json({
                success: false,
                message: "Volunteer already registered with this email"
            });
        }

        return res.status(500).json({
            success: false,
            message: "Internal server error",
            error: error.message
        });
    }
});

// ===================== LOGIN USER / VOLUNTEER =====================
router.post('/login', async (req, res) => {
    try {
        let { email, password, fcmToken } = req.body;

        email = email?.trim().toLowerCase();
        password = password?.trim();
        fcmToken = fcmToken?.trim();

        if (!email || !password) {
            return res.status(400).json({
                success: false,
                message: "Email and password required"
            });
        }

        const user = await User.findOne({
            email,
            userType: { $in: ['donor', 'receiver', 'volunteer'] }
        });

        if (!user) {
            return res.status(404).json({
                success: false,
                message: "User not found"
            });
        }

        const isMatch = await bcrypt.compare(password, user.password);

        if (!isMatch) {
            return res.status(400).json({
                success: false,
                message: "Invalid password"
            });
        }

        if (fcmToken) {
            user.fcmToken = fcmToken;
            await user.save();
            console.log("✅ FCM Token updated on login:", fcmToken);
        }

        const token = jwt.sign(
            { userId: user._id, userType: user.userType },
            process.env.SECRET_KEY,
            { expiresIn: "1d" }
        );

        return res.status(200).json({
            success: true,
            message: "Login successful",
            token,
            user: {
                _id: user._id,
                email: user.email,
                fullName: user.fullName,
                location: user.location,
                userType: user.userType,
                fcmToken: user.fcmToken
            }
        });

    } catch (error) {
        console.error("❌ LOGIN ERROR:", error);

        return res.status(500).json({
            success: false,
            message: "Server error",
            error: error.message
        });
    }
});

// ===================== COMMON FORGOT PASSWORD: SEND OTP =====================
router.post('/forgot-password/send-otp', async (req, res) => {
    try {
        let { email } = req.body;

        email = email?.trim().toLowerCase();

        if (!email) {
            return res.status(400).json({
                success: false,
                message: "Email is required"
            });
        }

        const user = await User.findOne({
            email,
            userType: { $in: ['donor', 'receiver', 'volunteer'] }
        });

        if (!user) {
            return res.status(404).json({
                success: false,
                message: "Email not registered"
            });
        }

        const userType = user.userType;
        const otpKey = createOtpKey(email, userType);

        const otp = Math.floor(100000 + Math.random() * 900000).toString();
        const expiryTime = Date.now() + 5 * 60 * 1000;

        otpStore.set(otpKey, {
            otp,
            expiryTime,
            verified: false,
            userType
        });

        console.log("✅ USER OTP:", otp, "FOR:", email);

        await sendOtpEmail(process.env.MAIL_USER, otp);

        return res.status(200).json({
            success: true,
            message: "OTP sent successfully"
        });

    } catch (error) {
        console.error("❌ SEND OTP ERROR:", error);

        return res.status(500).json({
            success: false,
            message: "Server error while sending OTP",
            error: error.message
        });
    }
});

// ===================== COMMON FORGOT PASSWORD: VERIFY OTP =====================
router.post('/forgot-password/verify-otp', async (req, res) => {
    try {
        let { email, otp } = req.body;

        email = email?.trim().toLowerCase();
        otp = otp?.trim();

        if (!email || !otp) {
            return res.status(400).json({
                success: false,
                message: "Email and OTP are required"
            });
        }

        const { otpKey, otpData } = findOtpByEmail(email);

        if (!otpData) {
            return res.status(404).json({
                success: false,
                message: "No OTP found. Please request a new OTP."
            });
        }

        if (Date.now() > otpData.expiryTime) {
            otpStore.delete(otpKey);
            return res.status(400).json({
                success: false,
                message: "OTP expired. Please request a new OTP."
            });
        }

        if (otpData.otp !== otp) {
            return res.status(400).json({
                success: false,
                message: "Invalid OTP"
            });
        }

        otpData.verified = true;
        otpStore.set(otpKey, otpData);

        return res.status(200).json({
            success: true,
            message: "OTP verified successfully"
        });

    } catch (error) {
        console.error("❌ VERIFY OTP ERROR:", error);

        return res.status(500).json({
            success: false,
            message: "Server error while verifying OTP",
            error: error.message
        });
    }
});

// ===================== COMMON FORGOT PASSWORD: RESET PASSWORD =====================
router.post('/forgot-password/reset-password', async (req, res) => {
    try {
        let { email, newPassword, confirmPassword } = req.body;

        email = email?.trim().toLowerCase();
        newPassword = newPassword?.trim();
        confirmPassword = confirmPassword?.trim();

        if (!email || !newPassword || !confirmPassword) {
            return res.status(400).json({
                success: false,
                message: "Email, new password and confirm password are required"
            });
        }

        if (newPassword !== confirmPassword) {
            return res.status(400).json({
                success: false,
                message: "Passwords do not match"
            });
        }

        const { otpKey, otpData } = findOtpByEmail(email);

        if (!otpData) {
            return res.status(400).json({
                success: false,
                message: "No OTP session found. Please request OTP again."
            });
        }

        if (Date.now() > otpData.expiryTime) {
            otpStore.delete(otpKey);
            return res.status(400).json({
                success: false,
                message: "OTP expired. Please request a new OTP."
            });
        }

        if (!otpData.verified) {
            return res.status(403).json({
                success: false,
                message: "OTP not verified"
            });
        }

        const user = await User.findOne({
            email,
            userType: otpData.userType
        });

        if (!user) {
            return res.status(404).json({
                success: false,
                message: "User not found. Password update not allowed."
            });
        }

        const hashedPassword = await bcrypt.hash(newPassword, 10);
        user.password = hashedPassword;
        await user.save();

        otpStore.delete(otpKey);

        return res.status(200).json({
            success: true,
            message: "Password reset successful"
        });

    } catch (error) {
        console.error("❌ RESET PASSWORD ERROR:", error);

        return res.status(500).json({
            success: false,
            message: "Server error while resetting password",
            error: error.message
        });
    }
});

// ===================== ADMIN REGISTER =====================
router.post('/admin/register', async (req, res) => {
    try {
        let { email, fullName, password, location, fcmToken } = req.body;

        email = email?.trim().toLowerCase();
        fullName = fullName?.trim();
        password = password?.trim();
        location = location?.trim();
        fcmToken = fcmToken?.trim();

        if (!email || !fullName || !password || !location) {
            return res.status(400).json({
                success: false,
                message: "All fields are required"
            });
        }

        const existingAdmin = await User.findOne({ email, userType: 'admin' });

        if (existingAdmin) {
            return res.status(400).json({
                success: false,
                message: "Admin already registered with this email"
            });
        }

        const hashedPassword = await bcrypt.hash(password, 10);

        const newAdmin = new User({
            email,
            fullName,
            password: hashedPassword,
            location,
            userType: 'admin',
            fcmToken: fcmToken || null
        });

        const savedAdmin = await newAdmin.save();

        return res.status(201).json({
            success: true,
            message: "Admin registered successfully",
            admin: {
                _id: savedAdmin._id,
                email: savedAdmin.email,
                fullName: savedAdmin.fullName,
                location: savedAdmin.location,
                userType: savedAdmin.userType,
                fcmToken: savedAdmin.fcmToken
            }
        });

    } catch (error) {
        console.error("❌ ADMIN REGISTER ERROR:", error);

        if (error.code === 11000) {
            return res.status(400).json({
                success: false,
                message: "Admin already registered with this email"
            });
        }

        return res.status(500).json({
            success: false,
            message: "Internal server error",
            error: error.message
        });
    }
});

// ===================== ADMIN LOGIN =====================
router.post('/admin/login', async (req, res) => {
    try {
        let { email, password, fcmToken } = req.body;

        email = email?.trim().toLowerCase();
        password = password?.trim();
        fcmToken = fcmToken?.trim();

        if (!email || !password) {
            return res.status(400).json({
                success: false,
                message: "Email and password required"
            });
        }

        const admin = await User.findOne({
            email,
            userType: 'admin'
        });

        if (!admin) {
            return res.status(404).json({
                success: false,
                message: "Admin not found"
            });
        }

        const isMatch = await bcrypt.compare(password, admin.password);

        if (!isMatch) {
            return res.status(401).json({
                success: false,
                message: "Invalid password"
            });
        }

        if (fcmToken) {
            admin.fcmToken = fcmToken;
            await admin.save();
            console.log("✅ ADMIN FCM Token updated:", fcmToken);
        }

        const token = jwt.sign(
            { userId: admin._id, userType: admin.userType },
            process.env.SECRET_KEY,
            { expiresIn: "1d" }
        );

        return res.status(200).json({
            success: true,
            message: "Admin login successful",
            token,
            admin: {
                _id: admin._id,
                email: admin.email,
                fullName: admin.fullName,
                location: admin.location,
                userType: admin.userType,
                fcmToken: admin.fcmToken
            }
        });

    } catch (error) {
        console.error("❌ ADMIN LOGIN ERROR:", error);

        return res.status(500).json({
            success: false,
            message: error.message
        });
    }
});

// ===================== ADMIN SEND OTP =====================
router.post('/admin/send-otp', async (req, res) => {
    try {
        let { email } = req.body;

        email = email?.trim().toLowerCase();

        if (!email) {
            return res.status(400).json({
                success: false,
                message: "Email is required"
            });
        }

        const admin = await User.findOne({ email, userType: 'admin' });

        if (!admin) {
            return res.status(404).json({
                success: false,
                message: "Admin email not registered"
            });
        }

        const otpKey = createOtpKey(email, 'admin');

        const otp = Math.floor(100000 + Math.random() * 900000).toString();
        const expiryTime = Date.now() + 5 * 60 * 1000;

        otpStore.set(otpKey, {
            otp,
            expiryTime,
            verified: false,
            userType: 'admin'
        });

        console.log("✅ ADMIN OTP:", otp, "FOR:", email);

        await sendOtpEmail(process.env.MAIL_USER, otp);

        return res.status(200).json({
            success: true,
            message: "OTP sent successfully to email"
        });

    } catch (error) {
        console.error("❌ ADMIN SEND OTP ERROR:", error);

        return res.status(500).json({
            success: false,
            message: "Server error while sending OTP",
            error: error.message
        });
    }
});

// ===================== ADMIN VERIFY OTP =====================
router.post('/admin/verify-otp', async (req, res) => {
    try {
        let { email, otp } = req.body;

        email = email?.trim().toLowerCase();
        otp = otp?.trim();

        if (!email || !otp) {
            return res.status(400).json({
                success: false,
                message: "Email and OTP are required"
            });
        }

        const otpKey = createOtpKey(email, 'admin');
        const otpData = otpStore.get(otpKey);

        if (!otpData) {
            return res.status(404).json({
                success: false,
                message: "No OTP found. Please request a new OTP."
            });
        }

        if (Date.now() > otpData.expiryTime) {
            otpStore.delete(otpKey);
            return res.status(400).json({
                success: false,
                message: "OTP expired. Please request a new OTP."
            });
        }

        if (otpData.otp !== otp) {
            return res.status(400).json({
                success: false,
                message: "Invalid OTP"
            });
        }

        otpData.verified = true;
        otpStore.set(otpKey, otpData);

        return res.status(200).json({
            success: true,
            message: "OTP verified successfully"
        });

    } catch (error) {
        console.error("❌ ADMIN VERIFY OTP ERROR:", error);

        return res.status(500).json({
            success: false,
            message: "Server error while verifying OTP",
            error: error.message
        });
    }
});

// ===================== ADMIN RESET PASSWORD =====================
router.post('/admin/reset-password', async (req, res) => {
    try {
        let { email, newPassword, confirmPassword } = req.body;

        email = email?.trim().toLowerCase();
        newPassword = newPassword?.trim();
        confirmPassword = confirmPassword?.trim();

        if (!email || !newPassword || !confirmPassword) {
            return res.status(400).json({
                success: false,
                message: "Email, new password and confirm password are required"
            });
        }

        if (newPassword !== confirmPassword) {
            return res.status(400).json({
                success: false,
                message: "Passwords do not match"
            });
        }

        const otpKey = createOtpKey(email, 'admin');
        const otpData = otpStore.get(otpKey);

        if (!otpData) {
            return res.status(400).json({
                success: false,
                message: "No OTP session found. Please request OTP again."
            });
        }

        if (Date.now() > otpData.expiryTime) {
            otpStore.delete(otpKey);
            return res.status(400).json({
                success: false,
                message: "OTP expired. Please request a new OTP."
            });
        }

        if (!otpData.verified) {
            return res.status(403).json({
                success: false,
                message: "OTP not verified"
            });
        }

        const admin = await User.findOne({
            email,
            userType: 'admin'
        });

        if (!admin) {
            return res.status(404).json({
                success: false,
                message: "Email not registered in database. Password update not allowed."
            });
        }

        const hashedPassword = await bcrypt.hash(newPassword, 10);
        admin.password = hashedPassword;
        await admin.save();

        otpStore.delete(otpKey);

        return res.status(200).json({
            success: true,
            message: "Password reset successful"
        });

    } catch (error) {
        console.error("❌ ADMIN RESET PASSWORD ERROR:", error);

        return res.status(500).json({
            success: false,
            message: "Server error while resetting password",
            error: error.message
        });
    }
});

// ===================== GET PROFILE =====================
router.get('/profile/:id', async (req, res) => {
    try {
        const userId = req.params.id;

        const user = await User.findById(userId).select("-password");

        if (!user) {
            return res.status(404).json({
                success: false,
                message: "User not found"
            });
        }

        if (user.userType === 'volunteer') {
            const totalAccepted = await Food.countDocuments({
                handledBy: userId,
                status: 'accepted'
            });

            const totalRejected = await Food.countDocuments({
                handledBy: userId,
                status: 'rejected'
            });

            const totalHandled = totalAccepted + totalRejected;

            return res.status(200).json({
                success: true,
                user,
                totalAccepted,
                totalRejected,
                totalHandled
            });
        }

        const totalUploaded = await Food.countDocuments({
            donorId: userId
        });

        return res.status(200).json({
            success: true,
            user,
            totalUploaded
        });

    } catch (error) {
        console.error("PROFILE ERROR:", error);

        return res.status(500).json({
            success: false,
            message: error.message
        });
    }
});

// ===================== GET ALL USERS =====================
router.get('/getallusers', async (req, res) => {
    try {
        const users = await User.find({
            userType: { $in: ['donor', 'receiver'] }
        }).select("-password");

        return res.status(200).json({
            success: true,
            count: users.length,
            users
        });

    } catch (error) {
        return res.status(500).json({
            success: false,
            message: error.message
        });
    }
});

// ===================== GET ALL VOLUNTEERS ONLY =====================
router.get('/getallvolunteers', async (req, res) => {
    try {
        const volunteers = await User.find({ userType: 'volunteer' }).select("-password");

        return res.status(200).json({
            success: true,
            count: volunteers.length,
            volunteers
        });

    } catch (error) {
        console.error("GET VOLUNTEERS ERROR:", error);

        return res.status(500).json({
            success: false,
            message: error.message
        });
    }
});

// ===================== UPDATE USER =====================
router.patch('/update/:id', async (req, res) => {
    try {
        const data = req.body;

        if (data.password) {
            data.password = await bcrypt.hash(data.password, 10);
        }

        const updatedUser = await User.findByIdAndUpdate(
            req.params.id,
            data,
            { new: true }
        ).select("-password");

        if (!updatedUser) {
            return res.status(404).json({
                success: false,
                message: "User not found"
            });
        }

        return res.status(200).json({
            success: true,
            message: "User updated successfully",
            user: updatedUser
        });

    } catch (error) {
        return res.status(500).json({
            success: false,
            message: error.message
        });
    }
});

// ===================== DELETE USER =====================
router.delete('/delete/:id', async (req, res) => {
    try {
        const user = await User.findOneAndDelete({
            _id: req.params.id,
            userType: { $in: ['donor', 'receiver'] }
        });

        if (!user) {
            return res.status(404).json({
                success: false,
                message: "User not found"
            });
        }

        return res.status(200).json({
            success: true,
            message: `User ${user.fullName} deleted successfully`
        });

    } catch (error) {
        return res.status(500).json({
            success: false,
            message: error.message
        });
    }
});

// ===================== DELETE VOLUNTEER ONLY =====================
router.delete('/deletevolunteer/:id', async (req, res) => {
    try {
        const volunteer = await User.findOneAndDelete({
            _id: req.params.id,
            userType: 'volunteer'
        });

        if (!volunteer) {
            return res.status(404).json({
                success: false,
                message: "Volunteer not found"
            });
        }

        return res.status(200).json({
            success: true,
            message: `Volunteer ${volunteer.fullName} deleted successfully`
        });

    } catch (error) {
        console.error("DELETE VOLUNTEER ERROR:", error);

        return res.status(500).json({
            success: false,
            message: error.message
        });
    }
});

// ===================== SAVE FCM TOKEN =====================
router.post('/save-token', async (req, res) => {
    try {
        let { userId, fcmToken } = req.body;

        userId = userId?.trim();
        fcmToken = fcmToken?.trim();

        if (!userId || !fcmToken) {
            return res.status(400).json({
                success: false,
                message: "userId and fcmToken are required"
            });
        }

        const user = await User.findByIdAndUpdate(
            userId,
            { fcmToken: fcmToken },
            { new: true }
        ).select("-password");

        if (!user) {
            return res.status(404).json({
                success: false,
                message: "User not found"
            });
        }

        return res.status(200).json({
            success: true,
            message: "FCM token saved successfully",
            user
        });

    } catch (error) {
        console.error("❌ SAVE TOKEN ERROR:", error);

        return res.status(500).json({
            success: false,
            message: error.message
        });
    }
});

module.exports = router;