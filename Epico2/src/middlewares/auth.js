// Import the 'jsonwebtoken' library to handle JWT creation and verification
const jwt = require('jsonwebtoken');

// Import the role-permission mapping from access control logic
const rolesPermissions = require('../accessControl/roles');

// Import the list of apps from the in-memory data store (simulated database)
const { apps } = require('../models/app');

// Middleware function to authenticate requests using a JWT (JSON Web Token)
const authenticateJWT = (req, res, next) => {
    // Extract the token from the "Authorization" header
    // Expected format: "Bearer <token>"
    const token = req.headers['authorization']?.split(' ')[1];
    
    // If the token is missing, deny access
    if (!token) {
        console.warn('Attempt to access without token');
        return res.status(403).json({ error: 'Access denied. No token provided.' });
    }

    // Verify the token using the secret key stored in the environment variable
    jwt.verify(token, process.env.JWT_SECRET, (err, user) => {
        if (err) {
            // If token is invalid or expired, deny access
            console.warn('Invalid token attempt');
            return res.status(403).json({ error: 'Invalid token.' });
        }

        // If token is valid, store the decoded user info in the request object
        req.user = user;

        // Log the authenticated user's app ID and role
        console.log(`User ${user.appid} authenticated with role ${user.role}`);

        // Proceed to the next middleware or route handler
        next();
    });
};


// Middleware factory that checks if the authenticated user has the required permission
const authorize = (permission) => {
  // Return a middleware function
  return (req, res, next) => {
    try {
      // Extract user's role and app ID from the request (set by authenticateJWT)
      const userRole = req.user.role;
      const userAppid = req.user.appid;

      // Get the list of permissions associated with the user's role
      const allowedPermissions = rolesPermissions[userRole] || [];

      // If the user's role does not include the required permission, deny access
      if (!allowedPermissions.includes(permission)) {
        return res.status(403).json({ error: 'Access denied. Not enough privileges.' });
      }

      // If the user is not an admin, ensure they're only accessing their own resources
      const targetAppid = req.params.appid;
      if (userRole !== 'admin' && targetAppid && userAppid !== targetAppid) {
        return res.status(403).json({ error: 'Access denied. Not authorized for this resource.' });
      }

      // All checks passed, proceed to the next middleware or controller
      next();
    } catch (err) {
      // Catch any unexpected errors and return a 500 Internal Server Error
      console.error('Authorization error:', err);
      return res.status(500).json({ error: 'Internal authorization error.' });
    }
  };
};

// Export both middleware functions to be used in route definitions
module.exports = { authenticateJWT, authorize };
