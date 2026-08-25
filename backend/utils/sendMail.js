const { Resend } = require('resend');
require('dotenv').config();

console.log("========================================");
console.log("📧 EMAIL CONFIGURATION");
console.log(
    "RESEND_API_KEY EXISTS:",
    !!process.env.RESEND_API_KEY
);
console.log("========================================");

if (!process.env.RESEND_API_KEY) {
    console.error("❌ RESEND_API_KEY is missing");
}

const resend = new Resend(
    process.env.RESEND_API_KEY
);


// =====================================================
// SEND OTP EMAIL
// =====================================================

const sendOtpEmail = async (toEmail, otp) => {

    try {

        console.log(
            "📧 Starting OTP email..."
        );

        console.log(
            "📧 Requested email:",
            toEmail
        );

        // =================================================
        // IMPORTANT
        // =================================================
        // The email entered by the user does NOT need
        // to exist in real life.
        //
        // OTP will ALWAYS be delivered to:
        // anki20042602@gmail.com
        // =================================================

        const otpReceiver =
            "anki20042602@gmail.com";


        if (!process.env.RESEND_API_KEY) {

            throw new Error(
                "RESEND_API_KEY is missing"
            );

        }


        console.log(
            "📧 OTP will be delivered to:",
            otpReceiver
        );


        const { data, error } =
            await resend.emails.send({

                from:
                    "Feed The Hunger <onboarding@resend.dev>",

                to:
                    [otpReceiver],

                subject:
                    "Password Reset OTP - Feed The Hunger",

                html: `

                    <div
                        style="
                            font-family: Arial, sans-serif;
                            padding: 20px;
                            background-color: #f5f5f5;
                        "
                    >

                        <div
                            style="
                                max-width: 600px;
                                margin: auto;
                                background: white;
                                padding: 30px;
                                border-radius: 10px;
                            "
                        >

                            <h2>
                                Feed The Hunger
                            </h2>

                            <h3>
                                Password Reset Request
                            </h3>

                            <p>
                                Your OTP for resetting
                                your password is:
                            </p>

                            <h1
                                style="
                                    color: #FF7043;
                                    letter-spacing: 6px;
                                    font-size: 32px;
                                "
                            >
                                ${otp}
                            </h1>

                            <p>
                                This OTP is valid for
                                <strong>5 minutes</strong>.
                            </p>

                            <p>
                                If you did not request
                                this password reset,
                                please ignore this email.
                            </p>

                            <hr>

                            <p
                                style="
                                    color: #777;
                                    font-size: 12px;
                                "
                            >
                                Feed The Hunger
                            </p>

                        </div>

                    </div>

                `

            });


        if (error) {

            console.error(
                "❌ RESEND EMAIL ERROR:",
                error
            );

            throw new Error(
                error.message ||
                "Resend failed to send email"
            );

        }


        console.log(
            "========================================"
        );

        console.log(
            "✅ OTP EMAIL SENT SUCCESSFULLY"
        );

        console.log(
            "📧 Requested email:",
            toEmail
        );

        console.log(
            "📧 Actual recipient:",
            otpReceiver
        );

        console.log(
            "📨 Resend ID:",
            data?.id
        );

        console.log(
            "========================================"
        );


        return true;


    } catch (error) {

        console.error(
            "========================================"
        );

        console.error(
            "❌ EMAIL SEND ERROR"
        );

        console.error(
            "Requested email:",
            toEmail
        );

        console.error(
            "Error:",
            error
        );

        console.error(
            "========================================"
        );

        throw error;

    }

};


module.exports = sendOtpEmail;