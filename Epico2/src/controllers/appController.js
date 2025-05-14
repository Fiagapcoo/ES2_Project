const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const { App, apps } = require('../models/app');

// Lista de senhas (como exemplo, substitua por uma base de dados real)
const passwords = [];

const registerApp = async (req, res) => {
  try {
    const { appid, name, secret } = req.body;

    if (!appid || !name || !secret) {
      console.warn('[REGISTER] Missing fields in request body.');
      return res.status(400).json({ error: 'All fields are mandatory.' });
    }

    const exists = apps.find(a => a.appid === appid);
    if (exists) {
      console.warn(`[REGISTER] Attempt to register already existing app: '${appid}'`);
      return res.status(409).json({ error: 'App already registered.' });
    }

    const hashedSecret = await bcrypt.hash(secret, 10);
    const newApp = new App(appid, name, hashedSecret, ['admin']);
    apps.push(newApp);

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

const createPassword = async (req, res) => {
  try {
    const appid = req.user.appid;
    const { password } = req.body;

    if (!password) {
      console.warn(`[CREATE] Missing password for appid '${appid}'`);
      return res.status(400).json({ error: 'Password is mandatory.' });
    }

    const appExists = apps.find(a => a.appid === appid);
    if (!appExists) {
      console.warn(`[CREATE] App '${appid}' not found or unauthorized access attempt.`);
      return res.status(404).json({ error: 'Resource not found or inaccessible.' });
    }

    const existingPassword = passwords.find(p => p.appid === appid);
    if (existingPassword) {
      console.warn(`[CREATE] Password already exists for appid '${appid}'`);
      return res.status(409).json({ error: 'Password for this app already exists.' });
    }

    const hashedPassword = await bcrypt.hash(password, 10);
    const newPassword = { appid, password: hashedPassword };
    passwords.push(newPassword);

    console.log(`[CREATE] Password created for appid '${appid}' by '${req.user.appid}'`);

    res.status(201).json({ message: 'Password created successfully!' });
  } catch (error) {
    console.error(`[CREATE] Internal error creating password for appid '${req.user.appid}':`, error);
    res.status(500).json({ error: 'Internal server error during password creation.' });
  }
};

  

const updatePassword = async (req, res) => {
  try {
    const appid = req.user.appid;
    const { password } = req.body;

    if (!password) {
      console.warn(`[UPDATE] Missing password for appid '${appid}'`);
      return res.status(400).json({ error: 'Password is mandatory.' });
    }

    const appExists = apps.find(a => a.appid === appid);
    if (!appExists) {
      console.warn(`[UPDATE] App '${appid}' not found or unauthorized access attempt.`);
      return res.status(404).json({ error: 'Resource not found or inaccessible.' });
    }

    const hashedPassword = await bcrypt.hash(password, 10);
    const passwordIndex = passwords.findIndex(p => p.appid === appid);
    if (passwordIndex === -1) {
      console.warn(`[UPDATE] Password record not found for appid '${appid}'`);
      return res.status(404).json({ error: 'Password for this app not found.' });
    }

    passwords[passwordIndex].password = hashedPassword;

    console.log(`[UPDATE] Password updated for appid '${appid}' by '${req.user.appid}'`);

    res.status(200).json({ message: 'Password updated successfully!' });

  } catch (error) {
    console.error(`[UPDATE] Internal error updating password for appid '${req.user.appid}':`, error);
    res.status(500).json({ error: 'Internal server error during password update.' });
  }
};


const getPassword = (req, res) => {
  try {
    const appid = req.user.appid;

    // Verifica se o app existe
    const appExists = apps.find(a => a.appid === appid);
    if (!appExists) {
      console.warn(`[READ] App '${appid}' not found or unauthorized access attempt.`);
      return res.status(404).json({ error: 'Resource not found or inaccessible.' });
    }

    // Recupera a senha associada ao app
    const password = passwords.find(p => p.appid === appid);
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


const getApps = (req, res) => {
  res.status(200).json({ apps });
};

module.exports = { 
  registerApp, 
  createPassword, 
  updatePassword, 
  getPassword, 
  getApps 
};
