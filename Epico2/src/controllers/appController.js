const bcrypt = require('bcryptjs');
const jwt = require('jsonwebtoken');
const { App, apps } = require('../models/app');

// Lista de senhas (como exemplo, substitua por uma base de dados real)
const passwords = [];

const registerApp = async (req, res) => {
  const { appid, name, secret } = req.body;

  if (!appid || !name || !secret) {
    return res.status(400).json({ error: 'All fields are mandatory.' });
  }

  const exists = apps.find(a => a.appid === appid);
  if (exists) {
    return res.status(409).json({ error: 'App already registered.' });
  }

  const hashedSecret = await bcrypt.hash(secret, 10);
  const newApp = new App(appid, name, hashedSecret, ['admin']);
  apps.push(newApp);

  const token = jwt.sign({ appid, role: newApp.roles[0] }, process.env.JWT_SECRET, { expiresIn: '1h' });

  res.status(201).json({ message: 'App registered with success!', token });
};

const createPassword = async (req, res) => {
    try {
      const { appid } = req.params;
      const { password } = req.body;
  
      if (!password) {
        return res.status(400).json({ error: 'Password is mandatory.' });
      }
  
      const appExists = apps.find(a => a.appid === appid);
      if (!appExists) {
        return res.status(404).json({ error: 'App not found.' });
      }
  
      const hashedPassword = await bcrypt.hash(password, 10);
      const newPassword = { appid, password: hashedPassword };
      passwords.push(newPassword);
  
      res.status(201).json({ message: 'Password created successfully!' });
    } catch (error) {
      console.error(error); 
      res.status(500).json({ error: 'Internal server error' });  // Retorna erro genérico
    }
  };
  

const updatePassword = async (req, res) => {
  const { appid } = req.params;
  const { password } = req.body;

  if (!password) {
    return res.status(400).json({ error: 'Password is mandatory.' });
  }

  // Verifica se o app existe
  const appExists = apps.find(a => a.appid === appid);
  if (!appExists) {
    return res.status(404).json({ error: 'App not found.' });
  }

  // Atualiza a senha do app
  const hashedPassword = await bcrypt.hash(password, 10);
  const passwordIndex = passwords.findIndex(p => p.appid === appid);
  if (passwordIndex === -1) {
    return res.status(404).json({ error: 'Password for this app not found.' });
  }

  passwords[passwordIndex].password = hashedPassword; // Atualiza a senha
  res.status(200).json({ message: 'Password updated successfully!' });
  
};

const getPassword = (req, res) => {
  const { appid } = req.params;

  // Verifica se o app existe
  const appExists = apps.find(a => a.appid === appid);
  if (!appExists) {
    return res.status(404).json({ error: 'App not found.' });
  }

  // Recupera a senha associada ao app
  const password = passwords.find(p => p.appid === appid);
  if (!password) {
    return res.status(404).json({ error: 'Password for this app not found.' });
  }

  res.status(200).json({ password: password.password });
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
