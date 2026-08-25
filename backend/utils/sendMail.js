const { Resend } = require('resend');
require('dotenv').config();

console.log("========================================");
console.log("📧 EMAIL CONFIGURATION");
console.log(
    "RESEND_API_KEY EXISTS:",
    !!process.env.RESEND_API_KEY
);
console.log(
    "OTP_RECEIVER_EMAIL:",
    process.env.OTP_RECEIVER_EMAIL || "NOT SET"
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

        console.log("========================================");
        console.log("📧 STARTING OTP EMAIL");
        console.log("========================================");


        // =================================================
        // REQUESTED ACCOUNT EMAIL
        // =================================================

        console.log(
            "📧 Requested account email:",
            toEmail
        );


        // =================================================
        // CHECK RESEND API KEY
        // =================================================

        if (!process.env.RESEND_API_KEY) {

            throw new Error(
                "RESEND_API_KEY is missing"
            );

        }


        // =================================================
        // FIXED OTP RECEIVER
        // =================================================
        //
        // The OTP is NOT sent to the account email.
        //
        // It is ALWAYS sent to the email configured in:
        //
        // OTP_RECEIVER_EMAIL
        //
        // Example:
        //
        // OTP_RECEIVER_EMAIL=ankitmandal2602@gmail.com
        //
        // =================================================

        const otpReceiver =
            process.env.OTP_RECEIVER_EMAIL
                ?.trim()
                .toLowerCase();


        if (!otpReceiver) {

            throw new Error(
                "OTP_RECEIVER_EMAIL is missing in environment variables"
            );

        }


        console.log(
            "📧 OTP will be delivered to:",
            otpReceiver
        );


        // =================================================
        // VALIDATE OTP
        // =================================================

        if (!otp) {

            throw new Error(
                "OTP is missing"
            );

        }


        // =================================================
        // SEND EMAIL THROUGH RESEND
        // =================================================

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
                                A password reset was requested
                                for the account:
                            </p>

                            <p>
                                <strong>
                                    ${toEmail}
                                </strong>
                            </p>

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


        // =================================================
        // RESEND ERROR
        // =================================================

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


        // =================================================
        // SUCCESS LOG
        // =================================================

        console.log(
            "========================================"
        );

        console.log(
            "✅ OTP EMAIL SENT SUCCESSFULLY"
        );

        console.log(
            "📧 Requested account:",
            toEmail
        );

        console.log(
            "📧 Actual OTP recipient:",
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

        // =================================================
        // ERROR LOG
        // =================================================

        console.error(
            "========================================"
        );

        console.error(
            "❌ OTP EMAIL SEND ERROR"
        );

        console.error(
            "📧 Requested account:",
            toEmail
        );

        console.error(
            "📧 OTP receiver:",
            process.env.OTP_RECEIVER_EMAIL
        );

        console.error(
            "❌ Error:",
            error
        );

        console.error(
            "========================================"
        );


        throw error;

    }

};


// =====================================================
// EXPORT
// =====================================================

module.exports = sendOtpEmail;