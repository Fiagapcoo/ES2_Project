const express = require('express');
const router = express.Router();
const { registerApp, createPassword, updatePassword, getPassword, getApps } = require('../controllers/appController');
const { authenticateJWT, authorize } = require('../middlewares/auth');

router.post('/app', registerApp);

router.post('/app/password/:appid/', authenticateJWT, authorize('create:password'), createPassword);

router.put('/app/password/:appid/', authenticateJWT, authorize('update:password'), updatePassword);

router.get('/app/password/:appid/', authenticateJWT, authorize('read:password'), getPassword);

router.get('/apps', authenticateJWT, authorize('read:apps'), getApps);

module.exports = router;