const jwt = require('jsonwebtoken');
const rolesPermissions = require('../accessControl/roles');
const { apps } = require('../models/app');

const authenticateJWT = (req, res, next) => {
    const token = req.headers['authorization']?.split(' ')[1];
    
    if (!token) {
        console.warn('Attempt to access without token');
        return res.status(403).json({ error: 'Access denied. No token provided.' });
    }

    jwt.verify(token, process.env.JWT_SECRET, (err, user) => {
        if (err) {
            console.warn('Invalid token attempt');
            return res.status(403).json({ error: 'Invalid token.' });
        }

        req.user = user;
        console.log(`User ${user.appid} authenticated with role ${user.role}`);
        next();
    });
};

const authorize = (permission) => {
  return (req, res, next) => {
    try {
      const userRole = req.user.role;
      const userAppid = req.user.appid;
      const allowedPermissions = rolesPermissions[userRole] || [];

      if (!allowedPermissions.includes(permission)) {
        return res.status(403).json({ error: 'Access denied. Not enough privileges.' });
      }

      const targetAppid = req.params.appid;
      if (userRole !== 'admin' && targetAppid && userAppid !== targetAppid) {
        return res.status(403).json({ error: 'Access denied. Not authorized for this resource.' });
      }

      next();
    } catch (err) {
      console.error('Authorization error:', err);
      return res.status(500).json({ error: 'Internal authorization error.' });
    }
  };
};

module.exports = { authenticateJWT, authorize };