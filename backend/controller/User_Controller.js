const express = require('express');
const router = express.Router();

const User = require('../model/User');
const Food = require('../model/Food');

const bcrypt = require('bcrypt');
const jwt = require('jsonwebtoken');
const sendOtpEmail = require('../utils/sendMail');

require('dotenv').config();

// =====================================================
// TEMPORARY OTP STORE
// =====================================================

const otpStore = new Map();

const createOtpKey = (email, userType) => {
    return `${email}_${userType}`;
};

const findOtpByEmail = (email) => {

    for (const [key, value] of otpStore.entries()) {

        if (key.startsWith(`${email}_`)) {

            return {
                otpKey: key,
                otpData: value
            };
        }
    }

    return {
        otpKey: null,
        otpData: null
    };
};


// =====================================================
// OTP EMAIL RECEIVER
// =====================================================
// For testing:
// Every OTP is sent to this email.
//
// The ORIGINAL user's email is still stored in otpStore,
// so verification and password reset work for the
// original account.
// =====================================================

const getOtpReceiverEmail = () => {

    const receiver =
        process.env.OTP_RECEIVER_EMAIL
            ?.trim()
            .toLowerCase();

    if (!receiver) {

        throw new Error(
            "OTP_RECEIVER_EMAIL is missing in .env"
        );

    }

    return receiver;
};


// =====================================================
// HOME
// =====================================================

router.get('/', (req, res) => {

    return res.send(
        "🚀 User Service Running Successfully"
    );

});


// =====================================================
// REGISTER USER
// DONOR / RECEIVER
// =====================================================

router.post('/registeruser', async (req, res) => {

    try {

        console.log(
            "📩 REGISTER REQUEST BODY:",
            req.body
        );

        let {
            email,
            fullName,
            password,
            location,
            latitude,
            longitude,
            userType,
            fcmToken
        } = req.body;


        email = email?.trim().toLowerCase();
        fullName = fullName?.trim();
        password = password?.trim();
        location = location?.trim();
        userType = userType?.trim().toLowerCase();
        fcmToken = fcmToken?.trim();


        // =================================================
        // COORDINATES
        // =================================================

        let parsedLatitude = null;
        let parsedLongitude = null;

        if (
            latitude !== undefined &&
            latitude !== null &&
            latitude !== ""
        ) {

            parsedLatitude = Number(latitude);

        }

        if (
            longitude !== undefined &&
            longitude !== null &&
            longitude !== ""
        ) {

            parsedLongitude = Number(longitude);

        }


        // =================================================
        // REQUIRED FIELDS
        // =================================================

        if (
            !email ||
            !fullName ||
            !password ||
            !location ||
            !userType
        ) {

            return res.status(400).json({

                success: false,

                message:
                    "All fields are required"

            });

        }


        // =================================================
        // VALID USER TYPE
        // =================================================

        const allowedTypes = [
            'donor',
            'receiver'
        ];

        if (!allowedTypes.includes(userType)) {

            return res.status(400).json({

                success: false,

                message:
                    "Invalid userType. Only donor or receiver allowed."

            });

        }


        // =================================================
        // VALID LATITUDE
        // =================================================

        if (
            parsedLatitude !== null &&
            (
                Number.isNaN(parsedLatitude) ||
                parsedLatitude < -90 ||
                parsedLatitude > 90
            )
        ) {

            return res.status(400).json({

                success: false,

                message:
                    "Invalid latitude"

            });

        }


        // =================================================
        // VALID LONGITUDE
        // =================================================

        if (
            parsedLongitude !== null &&
            (
                Number.isNaN(parsedLongitude) ||
                parsedLongitude < -180 ||
                parsedLongitude > 180
            )
        ) {

            return res.status(400).json({

                success: false,

                message:
                    "Invalid longitude"

            });

        }


        // =================================================
        // CHECK EXISTING USER
        // =================================================

        const existingUser =
            await User.findOne({

                email,
                userType

            });

        if (existingUser) {

            return res.status(400).json({

                success: false,

                message:
                    `${userType} already registered with this email`

            });

        }


        // =================================================
        // HASH PASSWORD
        // =================================================

        const hashedPassword =
            await bcrypt.hash(password, 10);


        // =================================================
        // CREATE USER
        // =================================================

        const newUser =
            new User({

                email,

                fullName,

                password:
                    hashedPassword,

                location,

                latitude:
                    parsedLatitude,

                longitude:
                    parsedLongitude,

                userType,

                fcmToken:
                    fcmToken || null

            });


        const savedUser =
            await newUser.save();


        console.log(
            "✅ USER REGISTERED:",
            savedUser._id.toString()
        );


        return res.status(201).json({

            success: true,

            message:
                `${userType} registered successfully`,

            user: {

                _id:
                    savedUser._id,

                email:
                    savedUser.email,

                fullName:
                    savedUser.fullName,

                location:
                    savedUser.location,

                latitude:
                    savedUser.latitude,

                longitude:
                    savedUser.longitude,

                userType:
                    savedUser.userType,

                fcmToken:
                    savedUser.fcmToken

            }

        });


    } catch (error) {

        console.error(
            "❌ REGISTER ERROR:",
            error
        );


        if (error.code === 11000) {

            return res.status(400).json({

                success: false,

                message:
                    "Email already registered for this role"

            });

        }


        return res.status(500).json({

            success: false,

            message:
                "Internal server error",

            error:
                error.message

        });

    }

});


// =====================================================
// VOLUNTEER REGISTER
// =====================================================

router.post('/volunteer/register', async (req, res) => {

    try {

        let {
            email,
            fullName,
            password,
            location,
            latitude,
            longitude,
            fcmToken
        } = req.body;


        email = email?.trim().toLowerCase();
        fullName = fullName?.trim();
        password = password?.trim();
        location = location?.trim();
        fcmToken = fcmToken?.trim();


        let parsedLatitude = null;
        let parsedLongitude = null;


        if (
            latitude !== undefined &&
            latitude !== null &&
            latitude !== ""
        ) {

            parsedLatitude =
                Number(latitude);

        }


        if (
            longitude !== undefined &&
            longitude !== null &&
            longitude !== ""
        ) {

            parsedLongitude =
                Number(longitude);

        }


        if (
            !email ||
            !fullName ||
            !password ||
            !location
        ) {

            return res.status(400).json({

                success: false,

                message:
                    "All fields are required"

            });

        }


        if (
            parsedLatitude !== null &&
            (
                Number.isNaN(parsedLatitude) ||
                parsedLatitude < -90 ||
                parsedLatitude > 90
            )
        ) {

            return res.status(400).json({

                success: false,

                message:
                    "Invalid latitude"

            });

        }


        if (
            parsedLongitude !== null &&
            (
                Number.isNaN(parsedLongitude) ||
                parsedLongitude < -180 ||
                parsedLongitude > 180
            )
        ) {

            return res.status(400).json({

                success: false,

                message:
                    "Invalid longitude"

            });

        }


        const existingVolunteer =
            await User.findOne({

                email,

                userType:
                    'volunteer'

            });


        if (existingVolunteer) {

            return res.status(400).json({

                success: false,

                message:
                    "Volunteer already registered with this email"

            });

        }


        const hashedPassword =
            await bcrypt.hash(password, 10);


        const newVolunteer =
            new User({

                email,

                fullName,

                password:
                    hashedPassword,

                location,

                latitude:
                    parsedLatitude,

                longitude:
                    parsedLongitude,

                userType:
                    'volunteer',

                fcmToken:
                    fcmToken || null

            });


        const savedVolunteer =
            await newVolunteer.save();


        console.log(
            "✅ VOLUNTEER REGISTERED:",
            savedVolunteer._id.toString()
        );


        return res.status(201).json({

            success: true,

            message:
                "Volunteer registered successfully",

            user: {

                _id:
                    savedVolunteer._id,

                email:
                    savedVolunteer.email,

                fullName:
                    savedVolunteer.fullName,

                location:
                    savedVolunteer.location,

                latitude:
                    savedVolunteer.latitude,

                longitude:
                    savedVolunteer.longitude,

                userType:
                    savedVolunteer.userType,

                fcmToken:
                    savedVolunteer.fcmToken

            }

        });


    } catch (error) {

        console.error(
            "❌ VOLUNTEER REGISTER ERROR:",
            error
        );


        if (error.code === 11000) {

            return res.status(400).json({

                success: false,

                message:
                    "Volunteer already registered with this email"

            });

        }


        return res.status(500).json({

            success: false,

            message:
                "Internal server error",

            error:
                error.message

        });

    }

});


// =====================================================
// LOGIN USER / VOLUNTEER
// =====================================================

router.post('/login', async (req, res) => {

    try {

        let {
            email,
            password,
            fcmToken
        } = req.body;


        email = email?.trim().toLowerCase();
        password = password?.trim();
        fcmToken = fcmToken?.trim();


        if (!email || !password) {

            return res.status(400).json({

                success: false,

                message:
                    "Email and password required"

            });

        }


        const user =
            await User.findOne({

                email,

                userType: {

                    $in: [
                        'donor',
                        'receiver',
                        'volunteer'
                    ]

                }

            });


        if (!user) {

            return res.status(404).json({

                success: false,

                message:
                    "User not found"

            });

        }


        const isMatch =
            await bcrypt.compare(
                password,
                user.password
            );


        if (!isMatch) {

            return res.status(400).json({

                success: false,

                message:
                    "Invalid password"

            });

        }


        // =================================================
        // UPDATE FCM TOKEN
        // =================================================

        if (fcmToken) {

            user.fcmToken =
                fcmToken;

            await user.save();

            console.log(
                "✅ FCM Token updated on login"
            );

        }


        // =================================================
        // JWT
        // =================================================

        const token =
            jwt.sign(

                {

                    userId:
                        user._id,

                    userType:
                        user.userType

                },

                process.env.SECRET_KEY,

                {

                    expiresIn:
                        "1d"

                }

            );


        return res.status(200).json({

            success: true,

            message:
                "Login successful",

            token,

            user: {

                _id:
                    user._id,

                email:
                    user.email,

                fullName:
                    user.fullName,

                location:
                    user.location,

                latitude:
                    user.latitude,

                longitude:
                    user.longitude,

                userType:
                    user.userType,

                fcmToken:
                    user.fcmToken

            }

        });


    } catch (error) {

        console.error(
            "❌ LOGIN ERROR:",
            error
        );


        return res.status(500).json({

            success: false,

            message:
                "Server error",

            error:
                error.message

        });

    }

});


// =====================================================
// USER / VOLUNTEER FORGOT PASSWORD
// SEND OTP
// =====================================================

router.post(
    '/forgot-password/send-otp',
    async (req, res) => {

        try {

            let { email } =
                req.body;

            email =
                email?.trim().toLowerCase();


            console.log(
                "📩 FORGOT PASSWORD REQUEST:",
                email
            );


            if (!email) {

                return res.status(400).json({

                    success: false,

                    message:
                        "Email is required"

                });

            }


            // =================================================
            // FIND REGISTERED USER
            // DONOR / RECEIVER / VOLUNTEER
            // =================================================

            const user =
                await User.findOne({

                    email,

                    userType: {

                        $in: [
                            'donor',
                            'receiver',
                            'volunteer'
                        ]

                    }

                });


            if (!user) {

                console.log(
                    "❌ EMAIL NOT REGISTERED:",
                    email
                );

                return res.status(404).json({

                    success: false,

                    message:
                        "Email not registered"

                });

            }


            const userType =
                user.userType;


            // =================================================
            // CREATE OTP KEY USING ORIGINAL EMAIL
            // =================================================

            const otpKey =
                createOtpKey(
                    email,
                    userType
                );


            // =================================================
            // GENERATE OTP
            // =================================================

            const otp =
                Math.floor(
                    100000 +
                    Math.random() * 900000
                ).toString();


            const expiryTime =
                Date.now() +
                5 * 60 * 1000;


            // =================================================
            // STORE OTP AGAINST ORIGINAL EMAIL
            // =================================================

            otpStore.set(

                otpKey,

                {

                    otp,

                    expiryTime,

                    verified: false,

                    userType

                }

            );


            console.log(
                "✅ USER/VOLUNTEER OTP:",
                otp,
                "FOR ACCOUNT:",
                email
            );


            // =================================================
            // SEND OTP
            // IMPORTANT:
            // OTP IS ALWAYS SENT TO OTP_RECEIVER_EMAIL
            // =================================================

            try {

                const otpReceiver =
                    getOtpReceiverEmail();


                console.log(
                    "📧 OTP FOR ACCOUNT:",
                    email
                );

                console.log(
                    "📧 OTP SENT TO:",
                    otpReceiver
                );


                await sendOtpEmail(
                    otpReceiver,
                    otp
                );


                console.log(
                    "✅ OTP EMAIL SENT SUCCESSFULLY"
                );


                return res.status(200).json({

                    success: true,

                    message:
                        "OTP sent successfully"

                });


            } catch (emailError) {

                console.error(
                    "❌ OTP EMAIL FAILED:",
                    emailError
                );


                // Delete OTP if email was not sent
                otpStore.delete(
                    otpKey
                );


                return res.status(500).json({

                    success: false,

                    message:
                        "Unable to send OTP email. Please try again.",

                    error:
                        emailError.message

                });

            }


        } catch (error) {

            console.error(
                "❌ SEND OTP ERROR:",
                error
            );


            return res.status(500).json({

                success: false,

                message:
                    "Server error while sending OTP",

                error:
                    error.message

            });

        }

    }
);


// =====================================================
// USER / VOLUNTEER FORGOT PASSWORD
// VERIFY OTP
// =====================================================

router.post(
    '/forgot-password/verify-otp',
    async (req, res) => {

        try {

            let {
                email,
                otp
            } = req.body;


            email =
                email?.trim().toLowerCase();

            otp =
                otp?.trim();


            console.log(
                "🔐 VERIFY OTP REQUEST:",
                email,
                otp
            );


            if (!email || !otp) {

                return res.status(400).json({

                    success: false,

                    message:
                        "Email and OTP are required"

                });

            }


            const {
                otpKey,
                otpData
            } = findOtpByEmail(email);


            if (!otpData) {

                return res.status(404).json({

                    success: false,

                    message:
                        "No OTP found. Please request a new OTP."

                });

            }


            // =================================================
            // CHECK EXPIRY
            // =================================================

            if (
                Date.now() >
                otpData.expiryTime
            ) {

                otpStore.delete(
                    otpKey
                );


                return res.status(400).json({

                    success: false,

                    message:
                        "OTP expired. Please request a new OTP."

                });

            }


            // =================================================
            // CHECK OTP
            // =================================================

            if (
                otpData.otp !== otp
            ) {

                return res.status(400).json({

                    success: false,

                    message:
                        "Invalid OTP"

                });

            }


            // =================================================
            // MARK VERIFIED
            // =================================================

            otpData.verified =
                true;


            otpStore.set(
                otpKey,
                otpData
            );


            console.log(
                "✅ OTP VERIFIED:",
                email
            );


            return res.status(200).json({

                success: true,

                message:
                    "OTP verified successfully"

            });


        } catch (error) {

            console.error(
                "❌ VERIFY OTP ERROR:",
                error
            );


            return res.status(500).json({

                success: false,

                message:
                    "Server error while verifying OTP",

                error:
                    error.message

            });

        }

    }
);


// =====================================================
// USER / VOLUNTEER FORGOT PASSWORD
// RESET PASSWORD
// =====================================================

router.post(
    '/forgot-password/reset-password',
    async (req, res) => {

        try {

            let {
                email,
                newPassword,
                confirmPassword
            } = req.body;


            email =
                email?.trim().toLowerCase();

            newPassword =
                newPassword?.trim();

            confirmPassword =
                confirmPassword?.trim();


            console.log(
                "🔄 RESET PASSWORD REQUEST:",
                email
            );


            if (
                !email ||
                !newPassword ||
                !confirmPassword
            ) {

                return res.status(400).json({

                    success: false,

                    message:
                        "Email, new password and confirm password are required"

                });

            }


            if (
                newPassword !==
                confirmPassword
            ) {

                return res.status(400).json({

                    success: false,

                    message:
                        "Passwords do not match"

                });

            }


            if (
                newPassword.length < 6
            ) {

                return res.status(400).json({

                    success: false,

                    message:
                        "Password must be at least 6 characters"

                });

            }


            // =================================================
            // FIND OTP SESSION USING ORIGINAL EMAIL
            // =================================================

            const {
                otpKey,
                otpData
            } = findOtpByEmail(email);


            if (!otpData) {

                return res.status(400).json({

                    success: false,

                    message:
                        "No OTP session found. Please request OTP again."

                });

            }


            // =================================================
            // CHECK EXPIRY
            // =================================================

            if (
                Date.now() >
                otpData.expiryTime
            ) {

                otpStore.delete(
                    otpKey
                );


                return res.status(400).json({

                    success: false,

                    message:
                        "OTP expired. Please request a new OTP."

                });

            }


            // =================================================
            // CHECK VERIFIED
            // =================================================

            if (!otpData.verified) {

                return res.status(403).json({

                    success: false,

                    message:
                        "OTP not verified"

                });

            }


            // =================================================
            // FIND ORIGINAL USER
            // =================================================

            const user =
                await User.findOne({

                    email,

                    userType:
                        otpData.userType

                });


            if (!user) {

                return res.status(404).json({

                    success: false,

                    message:
                        "User not found. Password update not allowed."

                });

            }


            // =================================================
            // HASH NEW PASSWORD
            // =================================================

            const hashedPassword =
                await bcrypt.hash(
                    newPassword,
                    10
                );


            user.password =
                hashedPassword;


            await user.save();


            // =================================================
            // DELETE USED OTP
            // =================================================

            otpStore.delete(
                otpKey
            );


            console.log(
                "✅ PASSWORD RESET SUCCESS:",
                email
            );


            return res.status(200).json({

                success: true,

                message:
                    "Password reset successful"

            });


        } catch (error) {

            console.error(
                "❌ RESET PASSWORD ERROR:",
                error
            );


            return res.status(500).json({

                success: false,

                message:
                    "Server error while resetting password",

                error:
                    error.message

            });

        }

    }
);


// =====================================================
// ADMIN REGISTER
// =====================================================

router.post(
    '/admin/register',
    async (req, res) => {

        try {

            let {
                email,
                fullName,
                password,
                location,
                latitude,
                longitude,
                fcmToken
            } = req.body;


            email =
                email?.trim().toLowerCase();

            fullName =
                fullName?.trim();

            password =
                password?.trim();

            location =
                location?.trim();

            fcmToken =
                fcmToken?.trim();


            let parsedLatitude = null;
            let parsedLongitude = null;


            if (
                latitude !== undefined &&
                latitude !== null &&
                latitude !== ""
            ) {

                parsedLatitude =
                    Number(latitude);

            }


            if (
                longitude !== undefined &&
                longitude !== null &&
                longitude !== ""
            ) {

                parsedLongitude =
                    Number(longitude);

            }


            if (
                !email ||
                !fullName ||
                !password ||
                !location
            ) {

                return res.status(400).json({

                    success: false,

                    message:
                        "All fields are required"

                });

            }


            const existingAdmin =
                await User.findOne({

                    email,

                    userType:
                        'admin'

                });


            if (existingAdmin) {

                return res.status(400).json({

                    success: false,

                    message:
                        "Admin already registered with this email"

                });

            }


            const hashedPassword =
                await bcrypt.hash(
                    password,
                    10
                );


            const newAdmin =
                new User({

                    email,

                    fullName,

                    password:
                        hashedPassword,

                    location,

                    latitude:
                        parsedLatitude,

                    longitude:
                        parsedLongitude,

                    userType:
                        'admin',

                    fcmToken:
                        fcmToken || null

                });


            const savedAdmin =
                await newAdmin.save();


            return res.status(201).json({

                success: true,

                message:
                    "Admin registered successfully",

                admin: {

                    _id:
                        savedAdmin._id,

                    email:
                        savedAdmin.email,

                    fullName:
                        savedAdmin.fullName,

                    location:
                        savedAdmin.location,

                    latitude:
                        savedAdmin.latitude,

                    longitude:
                        savedAdmin.longitude,

                    userType:
                        savedAdmin.userType,

                    fcmToken:
                        savedAdmin.fcmToken

                }

            });


        } catch (error) {

            console.error(
                "❌ ADMIN REGISTER ERROR:",
                error
            );


            if (
                error.code === 11000
            ) {

                return res.status(400).json({

                    success: false,

                    message:
                        "Admin already registered with this email"

                });

            }


            return res.status(500).json({

                success: false,

                message:
                    "Internal server error",

                error:
                    error.message

            });

        }

    }
);


// =====================================================
// ADMIN LOGIN
// =====================================================

router.post(
    '/admin/login',
    async (req, res) => {

        try {

            let {
                email,
                password,
                fcmToken
            } = req.body;


            email =
                email?.trim().toLowerCase();

            password =
                password?.trim();

            fcmToken =
                fcmToken?.trim();


            if (!email || !password) {

                return res.status(400).json({

                    success: false,

                    message:
                        "Email and password required"

                });

            }


            const admin =
                await User.findOne({

                    email,

                    userType:
                        'admin'

                });


            if (!admin) {

                return res.status(404).json({

                    success: false,

                    message:
                        "Admin not found"

                });

            }


            const isMatch =
                await bcrypt.compare(
                    password,
                    admin.password
                );


            if (!isMatch) {

                return res.status(401).json({

                    success: false,

                    message:
                        "Invalid password"

                });

            }


            if (fcmToken) {

                admin.fcmToken =
                    fcmToken;

                await admin.save();

            }


            const token =
                jwt.sign(

                    {

                        userId:
                            admin._id,

                        userType:
                            admin.userType

                    },

                    process.env.SECRET_KEY,

                    {

                        expiresIn:
                            "1d"

                    }

                );


            return res.status(200).json({

                success: true,

                message:
                    "Admin login successful",

                token,

                admin: {

                    _id:
                        admin._id,

                    email:
                        admin.email,

                    fullName:
                        admin.fullName,

                    location:
                        admin.location,

                    latitude:
                        admin.latitude,

                    longitude:
                        admin.longitude,

                    userType:
                        admin.userType,

                    fcmToken:
                        admin.fcmToken

                }

            });


        } catch (error) {

            console.error(
                "❌ ADMIN LOGIN ERROR:",
                error
            );


            return res.status(500).json({

                success: false,

                message:
                    error.message

            });

        }

    }
);


// =====================================================
// ADMIN SEND OTP
// =====================================================

router.post(
    '/admin/send-otp',
    async (req, res) => {

        try {

            let { email } =
                req.body;


            email =
                email?.trim().toLowerCase();


            console.log(
                "📩 ADMIN FORGOT PASSWORD REQUEST:",
                email
            );


            if (!email) {

                return res.status(400).json({

                    success: false,

                    message:
                        "Email is required"

                });

            }


            // =================================================
            // FIND ADMIN
            // =================================================

            const admin =
                await User.findOne({

                    email,

                    userType:
                        'admin'

                });


            if (!admin) {

                console.log(
                    "❌ ADMIN EMAIL NOT REGISTERED:",
                    email
                );


                return res.status(404).json({

                    success: false,

                    message:
                        "Admin email not registered"

                });

            }


            // =================================================
            // CREATE ADMIN OTP KEY USING ORIGINAL EMAIL
            // =================================================

            const otpKey =
                createOtpKey(
                    email,
                    'admin'
                );


            // =================================================
            // GENERATE OTP
            // =================================================

            const otp =
                Math.floor(
                    100000 +
                    Math.random() * 900000
                ).toString();


            const expiryTime =
                Date.now() +
                5 * 60 * 1000;


            // =================================================
            // STORE OTP
            // =================================================

            otpStore.set(

                otpKey,

                {

                    otp,

                    expiryTime,

                    verified: false,

                    userType:
                        'admin'

                }

            );


            console.log(
                "✅ ADMIN OTP:",
                otp,
                "FOR ACCOUNT:",
                email
            );


            // =================================================
            // SEND ADMIN OTP
            // ALWAYS TO OTP_RECEIVER_EMAIL
            // =================================================

            try {

                const otpReceiver =
                    getOtpReceiverEmail();


                console.log(
                    "📧 ADMIN OTP FOR ACCOUNT:",
                    email
                );

                console.log(
                    "📧 ADMIN OTP SENT TO:",
                    otpReceiver
                );


                await sendOtpEmail(
                    otpReceiver,
                    otp
                );


                console.log(
                    "✅ ADMIN OTP EMAIL SENT SUCCESSFULLY"
                );


                return res.status(200).json({

                    success: true,

                    message:
                        "OTP sent successfully to email"

                });


            } catch (emailError) {

                console.error(
                    "❌ ADMIN OTP EMAIL FAILED:",
                    emailError
                );


                otpStore.delete(
                    otpKey
                );


                return res.status(500).json({

                    success: false,

                    message:
                        "Unable to send OTP email",

                    error:
                        emailError.message

                });

            }


        } catch (error) {

            console.error(
                "❌ ADMIN SEND OTP ERROR:",
                error
            );


            return res.status(500).json({

                success: false,

                message:
                    "Server error while sending OTP",

                error:
                    error.message

            });

        }

    }
);


// =====================================================
// ADMIN VERIFY OTP
// =====================================================

router.post(
    '/admin/verify-otp',
    async (req, res) => {

        try {

            let {
                email,
                otp
            } = req.body;


            email =
                email?.trim().toLowerCase();

            otp =
                otp?.trim();


            console.log(
                "🔐 ADMIN VERIFY OTP REQUEST:",
                email,
                otp
            );


            if (!email || !otp) {

                return res.status(400).json({

                    success: false,

                    message:
                        "Email and OTP are required"

                });

            }


            const otpKey =
                createOtpKey(
                    email,
                    'admin'
                );


            const otpData =
                otpStore.get(
                    otpKey
                );


            if (!otpData) {

                return res.status(404).json({

                    success: false,

                    message:
                        "No OTP found. Please request a new OTP."

                });

            }


            // =================================================
            // CHECK EXPIRY
            // =================================================

            if (
                Date.now() >
                otpData.expiryTime
            ) {

                otpStore.delete(
                    otpKey
                );


                return res.status(400).json({

                    success: false,

                    message:
                        "OTP expired. Please request a new OTP."

                });

            }


            // =================================================
            // CHECK OTP
            // =================================================

            if (
                otpData.otp !== otp
            ) {

                return res.status(400).json({

                    success: false,

                    message:
                        "Invalid OTP"

                });

            }


            // =================================================
            // MARK VERIFIED
            // =================================================

            otpData.verified =
                true;


            otpStore.set(
                otpKey,
                otpData
            );


            console.log(
                "✅ ADMIN OTP VERIFIED:",
                email
            );


            return res.status(200).json({

                success: true,

                message:
                    "OTP verified successfully"

            });


        } catch (error) {

            console.error(
                "❌ ADMIN VERIFY OTP ERROR:",
                error
            );


            return res.status(500).json({

                success: false,

                message:
                    "Server error while verifying OTP",

                error:
                    error.message

            });

        }

    }
);


// =====================================================
// ADMIN RESET PASSWORD
// =====================================================

router.post(
    '/admin/reset-password',
    async (req, res) => {

        try {

            let {
                email,
                newPassword,
                confirmPassword
            } = req.body;


            email =
                email?.trim().toLowerCase();

            newPassword =
                newPassword?.trim();

            confirmPassword =
                confirmPassword?.trim();


            console.log(
                "🔄 ADMIN RESET PASSWORD REQUEST:",
                email
            );


            if (
                !email ||
                !newPassword ||
                !confirmPassword
            ) {

                return res.status(400).json({

                    success: false,

                    message:
                        "Email, new password and confirm password are required"

                });

            }


            if (
                newPassword !==
                confirmPassword
            ) {

                return res.status(400).json({

                    success: false,

                    message:
                        "Passwords do not match"

                });

            }


            if (
                newPassword.length < 6
            ) {

                return res.status(400).json({

                    success: false,

                    message:
                        "Password must be at least 6 characters"

                });

            }


            // =================================================
            // FIND ADMIN OTP SESSION
            // =================================================

            const otpKey =
                createOtpKey(
                    email,
                    'admin'
                );


            const otpData =
                otpStore.get(
                    otpKey
                );


            if (!otpData) {

                return res.status(400).json({

                    success: false,

                    message:
                        "No OTP session found. Please request OTP again."

                });

            }


            // =================================================
            // CHECK EXPIRY
            // =================================================

            if (
                Date.now() >
                otpData.expiryTime
            ) {

                otpStore.delete(
                    otpKey
                );


                return res.status(400).json({

                    success: false,

                    message:
                        "OTP expired. Please request a new OTP."

                });

            }


            // =================================================
            // CHECK VERIFIED
            // =================================================

            if (!otpData.verified) {

                return res.status(403).json({

                    success: false,

                    message:
                        "OTP not verified"

                });

            }


            // =================================================
            // FIND ORIGINAL ADMIN
            // =================================================

            const admin =
                await User.findOne({

                    email,

                    userType:
                        'admin'

                });


            if (!admin) {

                return res.status(404).json({

                    success: false,

                    message:
                        "Email not registered in database. Password update not allowed."

                });

            }


            // =================================================
            // HASH NEW PASSWORD
            // =================================================

            const hashedPassword =
                await bcrypt.hash(
                    newPassword,
                    10
                );


            admin.password =
                hashedPassword;


            await admin.save();


            // =================================================
            // DELETE USED OTP
            // =================================================

            otpStore.delete(
                otpKey
            );


            console.log(
                "✅ ADMIN PASSWORD RESET SUCCESS:",
                email
            );


            return res.status(200).json({

                success: true,

                message:
                    "Password reset successful"

            });


        } catch (error) {

            console.error(
                "❌ ADMIN RESET PASSWORD ERROR:",
                error
            );


            return res.status(500).json({

                success: false,

                message:
                    "Server error while resetting password",

                error:
                    error.message

            });

        }

    }
);


// =====================================================
// GET PROFILE
// =====================================================

router.get(
    '/profile/:id',
    async (req, res) => {

        try {

            const userId =
                req.params.id;


            const user =
                await User.findById(
                    userId
                ).select("-password");


            if (!user) {

                return res.status(404).json({

                    success: false,

                    message:
                        "User not found"

                });

            }


            if (
                user.userType ===
                'volunteer'
            ) {

                const totalAccepted =
                    await Food.countDocuments({

                        handledBy:
                            userId,

                        status:
                            'accepted'

                    });


                const totalRejected =
                    await Food.countDocuments({

                        handledBy:
                            userId,

                        status:
                            'rejected'

                    });


                const totalHandled =
                    totalAccepted +
                    totalRejected;


                return res.status(200).json({

                    success: true,

                    user,

                    totalAccepted,

                    totalRejected,

                    totalHandled

                });

            }


            const totalUploaded =
                await Food.countDocuments({

                    donorId:
                        userId

                });


            return res.status(200).json({

                success: true,

                user,

                totalUploaded

            });


        } catch (error) {

            console.error(
                "PROFILE ERROR:",
                error
            );


            return res.status(500).json({

                success: false,

                message:
                    error.message

            });

        }

    }
);


// =====================================================
// GET ALL USERS
// =====================================================

router.get(
    '/getallusers',
    async (req, res) => {

        try {

            const users =
                await User.find({

                    userType: {

                        $in: [
                            'donor',
                            'receiver'
                        ]

                    }

                }).select("-password");


            return res.status(200).json({

                success: true,

                count:
                    users.length,

                users

            });


        } catch (error) {

            return res.status(500).json({

                success: false,

                message:
                    error.message

            });

        }

    }
);


// =====================================================
// GET ALL VOLUNTEERS
// =====================================================

router.get(
    '/getallvolunteers',
    async (req, res) => {

        try {

            const volunteers =
                await User.find({

                    userType:
                        'volunteer'

                }).select("-password");


            return res.status(200).json({

                success: true,

                count:
                    volunteers.length,

                volunteers

            });


        } catch (error) {

            console.error(
                "GET VOLUNTEERS ERROR:",
                error
            );


            return res.status(500).json({

                success: false,

                message:
                    error.message

            });

        }

    }
);


// =====================================================
// UPDATE USER
// =====================================================

router.patch(
    '/update/:id',
    async (req, res) => {

        try {

            const data = {
                ...req.body
            };


            if (
                data.location !== undefined
            ) {

                data.location =
                    String(
                        data.location
                    ).trim();

            }


            if (
                data.latitude !== undefined &&
                data.latitude !== null &&
                data.latitude !== ""
            ) {

                data.latitude =
                    Number(
                        data.latitude
                    );

            }


            if (
                data.longitude !== undefined &&
                data.longitude !== null &&
                data.longitude !== ""
            ) {

                data.longitude =
                    Number(
                        data.longitude
                    );

            }


            if (data.password) {

                data.password =
                    await bcrypt.hash(
                        data.password,
                        10
                    );

            }


            const updatedUser =
                await User.findByIdAndUpdate(

                    req.params.id,

                    data,

                    {

                        new: true,

                        runValidators: true

                    }

                ).select("-password");


            if (!updatedUser) {

                return res.status(404).json({

                    success: false,

                    message:
                        "User not found"

                });

            }


            return res.status(200).json({

                success: true,

                message:
                    "User updated successfully",

                user:
                    updatedUser

            });


        } catch (error) {

            console.error(
                "UPDATE USER ERROR:",
                error
            );


            return res.status(500).json({

                success: false,

                message:
                    error.message

            });

        }

    }
);


// =====================================================
// DELETE USER
// =====================================================

router.delete(
    '/delete/:id',
    async (req, res) => {

        try {

            const user =
                await User.findOneAndDelete({

                    _id:
                        req.params.id,

                    userType: {

                        $in: [
                            'donor',
                            'receiver'
                        ]

                    }

                });


            if (!user) {

                return res.status(404).json({

                    success: false,

                    message:
                        "User not found"

                });

            }


            return res.status(200).json({

                success: true,

                message:
                    `User ${user.fullName} deleted successfully`

            });


        } catch (error) {

            return res.status(500).json({

                success: false,

                message:
                    error.message

            });

        }

    }
);


// =====================================================
// DELETE VOLUNTEER
// =====================================================

router.delete(
    '/deletevolunteer/:id',
    async (req, res) => {

        try {

            const volunteer =
                await User.findOneAndDelete({

                    _id:
                        req.params.id,

                    userType:
                        'volunteer'

                });


            if (!volunteer) {

                return res.status(404).json({

                    success: false,

                    message:
                        "Volunteer not found"

                });

            }


            return res.status(200).json({

                success: true,

                message:
                    `Volunteer ${volunteer.fullName} deleted successfully`

            });


        } catch (error) {

            console.error(
                "DELETE VOLUNTEER ERROR:",
                error
            );


            return res.status(500).json({

                success: false,

                message:
                    error.message

            });

        }

    }
);


// =====================================================
// SAVE FCM TOKEN
// =====================================================

router.post(
    '/save-token',
    async (req, res) => {

        try {

            let {
                userId,
                fcmToken
            } = req.body;


            userId =
                userId?.trim();

            fcmToken =
                fcmToken?.trim();


            if (!userId || !fcmToken) {

                return res.status(400).json({

                    success: false,

                    message:
                        "userId and fcmToken are required"

                });

            }


            const user =
                await User.findByIdAndUpdate(

                    userId,

                    {

                        fcmToken:
                            fcmToken

                    },

                    {

                        new: true

                    }

                ).select("-password");


            if (!user) {

                return res.status(404).json({

                    success: false,

                    message:
                        "User not found"

                });

            }


            return res.status(200).json({

                success: true,

                message:
                    "FCM token saved successfully",

                user

            });


        } catch (error) {

            console.error(
                "❌ SAVE TOKEN ERROR:",
                error
            );


            return res.status(500).json({

                success: false,

                message:
                    error.message

            });

        }

    }
);


// =====================================================
// EXPORT
// =====================================================

module.exports = router;