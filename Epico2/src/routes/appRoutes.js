const express = require('express');
const router = express.Router();

// Import controller functions
const {
  registerApp,
  createPassword,
  updatePassword,
  getPassword,
  getApps
} = require('../controllers/appController');

// Import middleware for authentication and authorization
const { authenticateJWT, authorize } = require('../middlewares/auth');


// PUBLIC ROUTE: Register a new application (no authentication required)
// Used to register a new app in the system
router.post(
  '/app',
  registerApp                    // Execute controller function to handle registration
);


// PROTECTED ROUTE: Create a new password for an app
router.post(
  '/app/password/:appid/',
  authenticateJWT,               // Check if user has a valid JWT token
  authorize('create:password'),  // Verify that the user has permission to create passwords
  createPassword                 // Execute controller function to create the password
);


// PROTECTED ROUTE: Update an existing password
router.put(
  '/app/password/:appid/',
  authenticateJWT,               // Check if user has a valid JWT token
  authorize('update:password'),  // Verify that the user has permission to update passwords
  updatePassword                 // Execute controller function to update the password
);


// PROTECTED ROUTE: Get a specific password for an app
router.get(
  '/app/password/:appid/',
  authenticateJWT,               // Check if user has a valid JWT token
  authorize('read:password'),    // Verify that the user has permission to read passwords
  getPassword                    // Execute controller function to retrieve the password
);


// PROTECTED ROUTE: Get a list of all registered applications
router.get(
  '/apps',
  authenticateJWT,               // Check if user has a valid JWT token
  authorize('read:apps'),        // Verify that the user has permission to read the list of apps
  getApps                        // Execute controller function to return the apps
);


// Export the router to be used in the main app (index.js / server.js)
module.exports = router;
