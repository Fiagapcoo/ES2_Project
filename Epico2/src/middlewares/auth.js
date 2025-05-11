const jwt = require('jsonwebtoken');
const rolesPermissions = require('../accessControl/roles');

const authenticateJWT = (req, res, next) => {
    const token = req.headers['authorization']?.split(' ')[1];
  console.log('Token Received:', token);

  if (!token) {
    return res.status(403).json({ error: 'Access denied.' });
  }

  jwt.verify(token, process.env.JWT_SECRET, (err, user) => {
    if (err) {
      return res.status(403).json({ error: 'Invalid token.' });
    }

    console.log('Authenticated User:', user);
    req.user = user;
    next();
  });
};

const authorize = (permission) => {
  return (req, res, next) => {
    try {
      const userRole = req.user.role;
      const allowedPermissions = rolesPermissions[userRole] || [];

      if (!allowedPermissions.includes(permission)) {
        console.warn(`Access denied for role '${userRole}' to perform '${permission}'`);
        return res.status(403).json({ error: 'Access denied. Not enough privileges.' });
      }

      next();
    } catch (err) {
      console.error('Authorization error:', err);
      return res.status(500).json({ error: 'Internal authorization error.' });
    }
  };
};


module.exports = { authenticateJWT, authorize };