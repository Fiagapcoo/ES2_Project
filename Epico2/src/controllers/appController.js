const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const { App, apps } = require('../models/app');
const externalStub = require('../stubs/externalApps');

// Lista de senhas (como exemplo, substitua por uma base de dados real)
const passwords = [];

// Controller to register a new application
const registerApp = async (req, res) => {
  try {
    const { appid, name, secret, role, password } = req.body;

    // Validate required fields
    if (!appid || !name || !secret || !password) {
      console.warn('[REGISTER] Missing fields in request body.');
      return res.status(400).json({ error: 'All fields are mandatory.' });
    }

    // Check if app already exists
    const exists = apps.find(a => a.appid === appid);
    if (exists) {
      console.warn(`[REGISTER] Attempt to register already existing app: '${appid}'`);
      return res.status(409).json({ error: 'App already registered.' });
    }

    // Hash the secret before saving
    const hashedSecret = await bcrypt.hash(secret, 10);
    const hashedPassword = await bcrypt.hash(password, 10);
    const newApp = new App(appid, name, hashedSecret, [role || 'client'], hashedPassword);
    apps.push(newApp); // Save to in-memory list

    // Create JWT token valid for 1 hour
    // Structure of functrion: jwt.sign(payload, secret, [options, callback])
    const token = jwt.sign(
      { appid, role: newApp.roles[0] },
      process.env.JWT_SECRET,
      { expiresIn: '1h' }
    );

    console.log(`[REGISTER] App '${appid}' registered with role '${newApp.roles[0]}'`);
    res.status(201).json({ message: 'App registered with success!', token });

  } catch (error) {
    console.error('[REGISTER] Internal error during app registration:', error);
    res.status(500).json({ error: 'Internal server error during registration.' });
  }
};


// Controller to create a hashed password for the authenticated app
const createPassword = async (req, res) => {
  try {
    const appid = req.params.appid;
    const { password } = req.body;

    // Ensure password is provided
    if (!password) {
      return res.status(400).json({ error: 'Password is mandatory.' });
    }

    // Validate app existence
    const appExists = apps.find(a => a.appid === appid);
    if (!appExists) {
      return res.status(404).json({ error: 'Resource not found or inaccessible.' });
    }

    // Prevent duplicate password creation
    if (appExists.password) {
      return res.status(409).json({ error: 'Password for this app already exists.' });
    }

    // Hash and store password
    const hashedPassword = await bcrypt.hash(password, 10);
    appExists.password = hashedPassword;

    res.status(201).json({ message: 'Password created successfully!' });
  } catch (error) {
    res.status(500).json({ error: 'Internal server error during password creation.' });
  }
};

// Controller to update the password of the authenticated app
const updatePassword = async (req, res) => {
  try {
    const appid = req.params.appid;
    const { password } = req.body;

    if (!password) {
      return res.status(400).json({ error: 'Password is mandatory.' });
    }

    const appExists = apps.find(a => a.appid === appid);
    if (!appExists) {
      return res.status(404).json({ error: 'Resource not found or inaccessible.' });
    }

    // Locate existing password by appid
    const passwordIndex = apps.findIndex(a => a.appid === appid);
    if (passwordIndex === -1) {
      return res.status(404).json({ error: 'Password for this app not found.' });
    }

    // Hash new password and update
    const hashedPassword = await bcrypt.hash(password, 10);
    apps[passwordIndex].password = hashedPassword;

    res.status(200).json({ message: 'Password updated successfully!' });

  } catch (error) {
    res.status(500).json({ error: 'Internal server error during password update.' });
  }
};


// Controller to retrieve the hashed password for the authenticated app
const getPassword = (req, res) => {
  try {
    const appid = req.params.appid;

    // Confirm app is registered
    const appExists = apps.find(a => a.appid === appid);
    if (!appExists) {
      console.warn(`[READ] App '${appid}' not found or unauthorized access attempt.`);
      return res.status(404).json({ error: 'Resource not found or inaccessible.' });
    }

    // Locate the password by appid
    const password = apps.find(a => a.appid === appid);
    if (!password) {
      console.warn(`[READ] Password not found for appid '${appid}'`);
      return res.status(404).json({ error: 'Password for this app not found.' });
    }

    console.log(`[READ] Password accessed for appid '${appid}' by '${req.user.appid}'`);

    res.status(200).json({ password: password.password });
  } catch (error) {
    console.error(`[READ] Internal error retrieving password for appid '${req.user.appid}':`, error);
    res.status(500).json({ error: 'Internal server error during password retrieval.' });
  }
};

// Controller to return the list of all registered apps
const getApps = (req, res) => {
  res.status(200).json({ apps });
};

const importApps = (req, res) => {
  try {
    const importedApps = externalStub.fetchApps();

    importedApps.forEach(newApp => {
      if (!apps.find(app => app.appid === newApp.appid)) {
        apps.push({ ...newApp, roles: ['client'], password: 'stubbed', secret: 'stubbed' });
      }
    });

    return res.status(200).json({ message: 'Import completed', imported: importedApps.length });
  } catch (err) {
    return res.status(500).json({ error: 'Internal server error during apps importing.' });
  }
};

module.exports = { 
  registerApp, 
  createPassword, 
  updatePassword, 
  getPassword, 
  getApps,
  importApps
};
