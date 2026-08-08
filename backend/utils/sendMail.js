const nodemailer = require('nodemailer');
require('dotenv').config();

// Create transporter
const transporter = nodemailer.createTransport({
    service: 'gmail',
    auth: {
        user: process.env.MAIL_USER,   // your gmail
        pass: process.env.MAIL_PASS    // 16-digit app password
    }
});

// Send OTP Email
const sendOtpEmail = async (toEmail, otp) => {
    try {
        const mailOptions = {
            from: `"Feed The Hunger" <${process.env.MAIL_USER}>`,
            to: toEmail,
            subject: 'Password Reset OTP - Feed The Hunger',
            html: `
                <div style="font-family: Arial, sans-serif; padding: 20px;">
                    <h2>Password Reset Request</h2>
                    <p>Your OTP for password reset is:</p>
                    <h1 style="color: #FF7043; letter-spacing: 4px;">${otp}</h1>
                    <p>This OTP is valid for 5 minutes.</p>
                    <p>If you did not request this, ignore this email.</p>
                </div>
            `
        };

        const info = await transporter.sendMail(mailOptions);

        console.log("✅ Email sent:", info.response);

    } catch (error) {
        console.error("❌ Email send error:", error);
        throw error; // important → so your controller catches it
    }
};

module.exports = sendOtpEmail;