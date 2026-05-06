const multer = require('multer');
const path = require('path');

// 📁 Storage configuration
const storage = multer.diskStorage({
  destination: function (req, file, cb) {
    cb(null, 'uploads/'); // make sure this folder exists
  },
  filename: function (req, file, cb) {
    cb(null, Date.now() + path.extname(file.originalname)); // unique file name
  },
});

// 🔥 FINAL FIXED FILE FILTER (accept all image types)
const fileFilter = (req, file, cb) => {

  console.log("📂 File received:", file.originalname);
  console.log("📦 MIME type:", file.mimetype);

  // ✅ Accept ANY image type
  if (file.mimetype && file.mimetype.startsWith('image/')) {
    cb(null, true);
  } else {
    cb(new Error('Only image files are allowed!'));
  }
};

// 🚀 Multer config
const upload = multer({
  storage,
  fileFilter,
  limits: { fileSize: 5 * 1024 * 1024 }, // Max 5MB
});

module.exports = upload;