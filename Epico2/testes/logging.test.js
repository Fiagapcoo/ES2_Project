// testes/logging.test.js
jest.mock('../src/services/logging', () => ({
  getNumberLogs: jest.fn(() => 3),
  logAccess: jest.fn()
}));

const request = require('supertest');
const express = require('express');
const jwt = require('jsonwebtoken');
const { authenticateJWT } = require('../src/middlewares/auth');
const logging = require('../src/services/logging');
const dotenv = require('dotenv');

dotenv.config(); 

const JWT_SECRET = process.env.JWT_SECRET;

// Criar uma app mínima só para testar o middleware
const app = express();
app.use(express.json());

// Endpoint de teste protegido com o middleware
app.get('/protegido', authenticateJWT, (req, res) => {
  res.status(200).json({ message: 'Autenticado com sucesso!' });
});

describe('Middleware authenticateJWT com mock do logging', () => {
  it('Deve chamar logging.logAccess quando o token é válido', async () => {
    // Testa: 
    // if (!token) [F]
    // if (err) [F]
    // logging.logAccess(user.appid) [V]
    const token = jwt.sign({ appid: 'app123', role: 'client' }, JWT_SECRET, { expiresIn: '1h' });

    const res = await request(app)
      .get('/protegido')
      .set("Authorization", `Bearer ${token}`)

    expect(res.status).toBe(200);
    expect(logging.logAccess).toHaveBeenCalledWith('app123');
  });

  it('Deve retornar número de logs simulado', () => {
    // Testa: getNumberLogs() simulado via mock [V]
    expect(logging.getNumberLogs()).toBe(3);
  });
});
